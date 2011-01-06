/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.i18n;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockHierarchyManager;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class DefaultI18NContentSupportTest extends MgnlTestCase {

    /**
     *
     */
    protected static final Locale DEFAULT_LOCALE = new Locale("en");
    /**
     * Logger.
     */
    protected static Logger log = LoggerFactory.getLogger(DefaultI18NContentSupportTest.class);

    public void testDetermineLocale(){
        DefaultI18nContentSupport defSupport = new DefaultI18nContentSupport();
        defSupport.setFallbackLocale(DEFAULT_LOCALE);
        defSupport.addLocale(new LocaleDefinition("de", null, true));
        defSupport.addLocale(new LocaleDefinition("de", "CH", true));
        defSupport.addLocale(new LocaleDefinition("it", null, false));

        // no language
        setCurrentURI("/home.html");
        Locale  locale = defSupport.determineLocale();
        assertEquals(DEFAULT_LOCALE, locale);

        setCurrentURI("/de/home.html");
        locale = defSupport.determineLocale();
        assertEquals(new Locale("de"), locale);

        setCurrentURI("/de_ch/home.html");
        locale = defSupport.determineLocale();
        assertEquals(new Locale("de", "ch"), locale);

        // not supported language
        setCurrentURI("/fr/home.html");
        locale = defSupport.determineLocale();
        assertEquals(DEFAULT_LOCALE, locale);

        // disabled language
        setCurrentURI("/it/home.html");
        locale = defSupport.determineLocale();
        assertEquals(DEFAULT_LOCALE, locale);
    }

    public void testDetermineLocaleEndlessLoop () {
        DefaultI18nContentSupport defSupport = new DefaultI18nContentSupport();
        defSupport.setFallbackLocale(DEFAULT_LOCALE);
        defSupport.addLocale(new LocaleDefinition("de", "CH", true));
        defSupport.addLocale(new LocaleDefinition("it", null, true));

        // no language
        setCurrentURI("/home.html");
        Locale  locale = defSupport.determineLocale();
        assertEquals(DEFAULT_LOCALE, locale);

        // exact match on the lang/country
        setCurrentURI("/de_ch/home.html");
        locale = defSupport.determineLocale();
        assertEquals(new Locale("de", "ch"), locale);

        // supported lang, but not country - should default to the same lang but with diff country
        setCurrentURI("/de_at/home.html");
        locale = defSupport.determineLocale();
        assertEquals(new Locale("de", "ch"), locale);

        // supported lang, but not w/o country code - should default to the same lang but with different country code
        setCurrentURI("/de/home.html");
        locale = defSupport.determineLocale();
        assertEquals(new Locale("de", "ch"), locale);

        // not supported language
        setCurrentURI("/fr/home.html");
        locale = defSupport.determineLocale();
        assertEquals(DEFAULT_LOCALE, locale);

        setCurrentURI("/it/home.html");
        locale = defSupport.determineLocale();
        assertEquals(new Locale("it"), locale);

    }

    public void testGetNodeDataEndlessLoop() throws Exception {
        DefaultI18nContentSupport defSupport = new DefaultI18nContentSupport();
        defSupport.setEnabled(true);
        defSupport.setFallbackLocale(DEFAULT_LOCALE);
        defSupport.addLocale(new LocaleDefinition("de", "CH", true));
        defSupport.addLocale(new LocaleDefinition("it", null, false));
        MockContent content = new MockContent("boo");
        content.setHierarchyManager(new MockHierarchyManager());

        // no language
        NodeData defaultblah = content.setNodeData("blah", "val_blah");
        NodeData localized = defSupport.getNodeData(content, "blah");
        assertEquals(defaultblah, localized);

        // exact match doesn't exist, country_lang match doesn't exist, locale is country only
        NodeData defaultFoo = content.setNodeData("foo", "val_foo");
        defSupport.setLocale(new Locale("de"));
        localized = defSupport.getNodeData(content, "foo");
        assertEquals(defaultFoo, localized);

        // exact match on the lang/country
        NodeData swissBlah = content.setNodeData("blah_de_CH", "val_de_ch_blah");
        defSupport.setLocale(new Locale("de", "CH"));
        localized = defSupport.getNodeData(content, "blah");
        assertEquals(swissBlah, localized);

        // supported lang, but not country - should default to the same lang but with diff country
        defSupport.setLocale(new Locale("de", "AT"));
        localized = defSupport.getNodeData(content, "blah");
        assertEquals(swissBlah, localized);

        // supported lang, but not w/o country code - should default to the same lang but with different country code
        defSupport.setLocale(new Locale("de"));
        localized = defSupport.getNodeData(content, "blah");
        assertEquals(swissBlah, localized);

        // not supported language
        defSupport.setLocale(new Locale("fr"));
        localized = defSupport.getNodeData(content, "blah");
        assertEquals(defaultblah, localized);

        // disabled language
        defSupport.setLocale(new Locale("it"));
        localized = defSupport.getNodeData(content, "blah");
        assertEquals(defaultblah, localized);

    }

    protected void setCurrentURI(String uri) {
        WebContext ctx = createMock(WebContext.class);
        AggregationState state = createMock(AggregationState.class);
        expect(state.getCurrentURI()).andReturn(uri);
        expect(ctx.getAggregationState()).andReturn(state);
        replay(ctx, state);
        MgnlContext.setInstance(ctx);
    }
}
