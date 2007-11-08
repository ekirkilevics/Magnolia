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
package info.magnolia.cms.servlets;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import javax.servlet.ServletContext;

/**
 * TODO : we should also handle the cases when servletContext.getRealPath() returns null - see javadoc
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MgnlServletContextListenerTest extends TestCase {
    private ServletContext servletContext;
    private MgnlServletContextListener propInit;

    protected void setUp() throws Exception {
        super.setUp();
        servletContext = createStrictMock(ServletContext.class);
        propInit = new PropertyInitializer();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        verify(servletContext);
    }

    public void testInitRootPathJustWorks() {
        expectServletContextRealPath("/foo/bar");
        assertEquals("/foo/bar", propInit.initRootPath(servletContext));
    }

    public void testInitRootPathStripsTrailingSlash() {
        expectServletContextRealPath("/foo/bar/");
        assertEquals("/foo/bar", propInit.initRootPath(servletContext));
    }

    public void testInitRootPathTranslatesBackslashes() {
        expectServletContextRealPath("\\foo\\bar");
        assertEquals("/foo/bar", propInit.initRootPath(servletContext));
    }

    public void testInitRootPathTranslatesBackslashesAndStripsTrailingSlash() {
        expectServletContextRealPath("\\foo\\bar\\");
        assertEquals("/foo/bar", propInit.initRootPath(servletContext));
    }

    public void testInitWebappNameJustWorks() {
        expectServletContextRealPath("/foo/bar");
        final String rootPath = propInit.initRootPath(servletContext);
        assertEquals("bar", propInit.initWebappName(rootPath));
    }

    public void testInitWebappNameWorksWithTrailingSlashes() {
        expectServletContextRealPath("/foo/bar/");
        final String rootPath = propInit.initRootPath(servletContext);
        assertEquals("bar", propInit.initWebappName(rootPath));
    }

    public void testInitWebappNameWorksWithBackslashes() {
        expectServletContextRealPath("\\foo\\bar");
        final String rootPath = propInit.initRootPath(servletContext);
        assertEquals("bar", propInit.initWebappName(rootPath));
    }

    public void testInitWebappNameWorksWithTrailingSlashesAndBackslashes() {
        expectServletContextRealPath("\\foo\\bar\\");
        final String rootPath = propInit.initRootPath(servletContext);
        assertEquals("bar", propInit.initWebappName(rootPath));
    }

    private void expectServletContextRealPath(String returnedPath) {
        expect(servletContext.getRealPath("")).andReturn(returnedPath);
        replay(servletContext);
    }
}
