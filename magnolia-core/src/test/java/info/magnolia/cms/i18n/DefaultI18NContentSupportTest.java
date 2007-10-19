/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.i18n;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.MgnlTestCase;

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
    private static final Locale DEFAULT_LOCALE = new Locale("en");
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DefaultI18NContentSupportTest.class);
    
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

        // desabled language
        setCurrentURI("/it/home.html");
        locale = defSupport.determineLocale();
        assertEquals(DEFAULT_LOCALE, locale);

    }

    private void setCurrentURI(String uri) {
        WebContext ctx = createMock(WebContext.class);
        AggregationState state = createMock(AggregationState.class);
        expect(state.getCurrentURI()).andReturn(uri);
        expect(ctx.getAggregationState()).andReturn(state);
        replay(ctx, state);
        MgnlContext.setInstance(ctx);
    }
}
