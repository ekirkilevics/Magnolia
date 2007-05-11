/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import java.io.File;

/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class PathTest extends TestCase {

    /**
     * Test method for {@link info.magnolia.cms.core.Path#isAbsolute(java.lang.String)}.
     */
    public void testIsAbsolute() {
        assertTrue(Path.isAbsolute("/test"));
        assertTrue(Path.isAbsolute("d:/test"));
        assertTrue(Path.isAbsolute(File.separator + "test"));
        assertFalse(Path.isAbsolute("test"));
    }

    public void testUriDecodingShouldStripCtxPath() {
        WebContext webCtx = createMock(WebContext.class);
        expect(webCtx.getContextPath()).andReturn("/foo");
//        final AggregationState aggState = new AggregationState();
//        aggState.setCharacterEncoding("UTF-8");
//        expect(webCtx.getAggregationState()).andReturn(aggState);
        expect(webCtx.getAttribute(WebContext.ATTRIBUTE_REQUEST_CHARACTER_ENCODING)).andReturn("UTF-8");
        MgnlContext.setInstance(webCtx);
        replay(webCtx);

        assertEquals("/pouet", Path.decodedURI("/foo/pouet"));
        verify(webCtx);
    }

    public void testUriDecodingShouldReturnPassedURIDoesntContainCtxPath() {
        WebContext webCtx = createMock(WebContext.class);
        expect(webCtx.getContextPath()).andReturn("/foo");
//        final AggregationState aggState = new AggregationState();
//        aggState.setCharacterEncoding("UTF-8");
//        expect(webCtx.getAggregationState()).andReturn(aggState);
        expect(webCtx.getAttribute(WebContext.ATTRIBUTE_REQUEST_CHARACTER_ENCODING)).andReturn("UTF-8");
        MgnlContext.setInstance(webCtx);
        replay(webCtx);

        assertEquals("/pouet", Path.decodedURI("/pouet"));
        verify(webCtx);
    }

}
