/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.jcr.wrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.LocaleDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockAggregationState;
import info.magnolia.test.mock.jcr.MockNode;

import java.util.Locale;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class I18nNodeWrapperTest extends MgnlTestCase {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        WebContext ctx = mock(WebContext.class);
        when(ctx.getAggregationState()).thenReturn(new MockAggregationState());
        MgnlContext.setInstance(ctx);

        DefaultI18nContentSupport defSupport = new DefaultI18nContentSupport();
        defSupport.setEnabled(true);
        defSupport.setFallbackLocale(new Locale("en"));
        defSupport.addLocale(LocaleDefinition.make("de", "CH", true));
        defSupport.addLocale(LocaleDefinition.make("it", null, false));
        ComponentsTestUtil.setInstance(I18nContentSupport.class, defSupport);
    }

    @Test
    public void testGetPropertyReturnsLocalizedValue() throws Exception {

        final I18nContentSupport defSupport = Components.getComponent(I18nContentSupport.class);

        final MockNode node = new MockNode("boo");
        final I18nNodeWrapper wrappedNode = new I18nNodeWrapper(node);

        // no language
        Property defaultblah = node.setProperty("blah", "val_blah");
        Property localized = wrappedNode.getProperty("blah");
        assertEquals(defaultblah, localized);

        // exact match doesn't exist, country_lang match doesn't exist, locale is country only
        Property defaultFoo = node.setProperty("foo", "val_foo");
        defSupport.setLocale(new Locale("de"));
        localized = wrappedNode.getProperty("foo");
        assertEquals(defaultFoo, localized);

        // exact match on the lang/country
        Property swissBlah = node.setProperty("blah_de_CH", "val_de_ch_blah");
        defSupport.setLocale(new Locale("de", "CH"));
        localized = wrappedNode.getProperty("blah");
        assertEquals(swissBlah, localized);

        // supported lang, but not country - should default to the same lang but with diff country
        defSupport.setLocale(new Locale("de", "AT"));
        localized = wrappedNode.getProperty("blah");
        assertEquals(swissBlah, localized);

        // supported lang, but not w/o country code - should default to the same lang but with different country code
        defSupport.setLocale(new Locale("de"));
        localized = wrappedNode.getProperty("blah");
        assertEquals(swissBlah, localized);

        // not supported language
        defSupport.setLocale(new Locale("fr"));
        localized = wrappedNode.getProperty("blah");
        assertEquals(defaultblah, localized);

        // disabled language
        defSupport.setLocale(new Locale("it"));
        localized = wrappedNode.getProperty("blah");
        assertEquals(defaultblah, localized);

    }

    @Test
    public void testGetPropertyDoesntReturnResourceNode() throws Exception {
        // GIVEN
        final MockNode node = new MockNode("boo");
        final I18nNodeWrapper wrappedNode = new I18nNodeWrapper(node);

        assertFalse(wrappedNode.hasProperty("blah"));
        // WHEN
        node.addNode("blah", "mgnl:resource");
        // THEN
        assertFalse(wrappedNode.hasProperty("blah"));

    }

    @Test
    public void testHasPropertyReturnsTrueWhenOnlyLocaleVariantIsAvailable() throws Exception {
        //GIVEN
        final I18nContentSupport defSupport = Components.getComponent(I18nContentSupport.class);
        defSupport.setLocale(new Locale("de"));
        final MockNode node = new MockNode("boo");
        final I18nNodeWrapper wrappedNode = new I18nNodeWrapper(node);

        // no default property (the one without _de appended) only German variation
        node.setProperty("foo_de", "deutsches Foo");
        //WHEN
        boolean propertyExists = wrappedNode.hasProperty("foo");

        //THEN
        assertTrue(propertyExists);
    }

    @Test
    public void testHasPropertyReturnsTrueWhenOnlyDefaultIsAvailable() throws Exception {
        //GIVEN
        final I18nContentSupport defSupport = Components.getComponent(I18nContentSupport.class);
        defSupport.setLocale(new Locale("de"));
        final MockNode node = new MockNode("boo");
        final I18nNodeWrapper wrappedNode = new I18nNodeWrapper(node);

        // only default property
        node.setProperty("foo", "english foo");
        //WHEN
        boolean propertyExists = wrappedNode.hasProperty("foo");

        //THEN
        assertTrue(propertyExists);
    }

    @Test
    public void testHasPropertyReturnsFalseWhenNoSuchPropertyExists() throws Exception {
        //GIVEN
        final I18nContentSupport defSupport = Components.getComponent(I18nContentSupport.class);
        defSupport.setLocale(new Locale("de"));
        final MockNode node = new MockNode("boo");
        final I18nNodeWrapper wrappedNode = new I18nNodeWrapper(node);
        //WHEN
        boolean propertyExists = wrappedNode.hasProperty("foo");

        //THEN
        assertFalse(propertyExists);
    }

    @Test
    public void testWrapNode() throws Exception {
        //GIVEN
        final Node root = new MockNode();
        root.addNode("bar");

        //WHEN
        final I18nNodeWrapper wrappedRoot = new I18nNodeWrapper(root);

        //THEN
        assertTrue(wrappedRoot.getNode("bar") instanceof I18nNodeWrapper);

    }

    @Test
    public void testGetParentReturnsWrappedNode() throws Exception {
        //GIVEN
        final Node root = new MockNode();
        root.addNode("bar").addNode("foo");

        //WHEN
        final I18nNodeWrapper wrappedRoot = new I18nNodeWrapper(root);
        final Node parent = wrappedRoot.getNode("bar/foo").getParent();

        //THEN
        assertTrue(parent instanceof I18nNodeWrapper);

    }

    @Test
    public void testGetAncestorReturnsWrappedNode() throws Exception {
        //GIVEN
        final Node root = new MockNode();
        root.addNode("bar").addNode("foo").addNode("baz");

        //WHEN
        final I18nNodeWrapper wrappedNode = new I18nNodeWrapper(root.getNode("bar/foo/baz"));

        //THEN
        for (int i = 1; i < wrappedNode.getDepth(); ++i) {
            assertTrue(wrappedNode.getAncestor(i) instanceof I18nNodeWrapper);
        }
    }

    @Test(expected=ItemNotFoundException.class)
    public void testGetRootParentThrowsItemNotFoundException() throws Exception {
        //GIVEN
        final Node root = new MockNode();

        //WHEN
        final I18nNodeWrapper wrappedRoot = new I18nNodeWrapper(root);

        //THEN BOOOOM!
        wrappedRoot.getParent();

    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }
}
