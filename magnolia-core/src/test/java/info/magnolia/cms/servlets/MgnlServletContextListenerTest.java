/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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
        propInit = new MgnlServletContextListener();
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
