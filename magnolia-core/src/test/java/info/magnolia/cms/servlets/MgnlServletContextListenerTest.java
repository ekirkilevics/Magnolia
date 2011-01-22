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
    private MgnlServletContextListener mscl;

    protected void setUp() throws Exception {
        super.setUp();
        servletContext = createStrictMock(ServletContext.class);
        mscl = new MgnlServletContextListener();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        verify(servletContext);
    }

    public void testDetermineRootPathJustWorks() {
        expectServletContextRealPath("/foo/bar");
        assertEquals("/foo/bar", mscl.determineRootPath(servletContext));
    }

    public void testDetermineRootPathStripsTrailingSlash() {
        expectServletContextRealPath("/foo/bar/");
        assertEquals("/foo/bar", mscl.determineRootPath(servletContext));
    }

    public void testDetermineRootPathTranslatesBackslashes() {
        expectServletContextRealPath("\\foo\\bar");
        assertEquals("/foo/bar", mscl.determineRootPath(servletContext));
    }

    public void testDetermineRootPathTranslatesBackslashesAndStripsTrailingSlash() {
        expectServletContextRealPath("\\foo\\bar\\");
        assertEquals("/foo/bar", mscl.determineRootPath(servletContext));
    }

    public void testDetermineWebappFolderNameJustWorks() {
        expectServletContextRealPath("/foo/bar");
        final String rootPath = mscl.determineRootPath(servletContext);
        assertEquals("bar", mscl.determineWebappFolderName(rootPath, servletContext));
    }

    public void testDetermineWebappFolderNameWorksWithTrailingSlashes() {
        expectServletContextRealPath("/foo/bar/");
        final String rootPath = mscl.determineRootPath(servletContext);
        assertEquals("bar", mscl.determineWebappFolderName(rootPath, servletContext));
    }

    public void testDetermineWebappFolderNameWorksWithBackslashes() {
        expectServletContextRealPath("\\foo\\bar");
        final String rootPath = mscl.determineRootPath(servletContext);
        assertEquals("bar", mscl.determineWebappFolderName(rootPath, servletContext));
    }

    public void testDetermineWebappFolderNameWorksWithTrailingSlashesAndBackslashes() {
        expectServletContextRealPath("\\foo\\bar\\");
        final String rootPath = mscl.determineRootPath(servletContext);
        assertEquals("bar", mscl.determineWebappFolderName(rootPath, servletContext));
    }

    private void expectServletContextRealPath(String returnedPath) {
        expect(servletContext.getRealPath("")).andReturn(returnedPath);
        replay(servletContext);
    }
}
