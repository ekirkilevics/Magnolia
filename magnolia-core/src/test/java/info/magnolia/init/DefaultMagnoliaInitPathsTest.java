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
package info.magnolia.init;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import junit.framework.TestCase;

import javax.servlet.ServletContext;

import static org.easymock.EasyMock.*;

/**
 * TODO : we should also handle the cases when servletContext.getRealPath() returns null - see javadoc
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DefaultMagnoliaInitPathsTest extends TestCase {
    private ServletContext servletContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        servletContext = createStrictMock(ServletContext.class);
    }

    @Override
    protected void tearDown() throws Exception {
        verify(servletContext);
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    // TODO : test methods for retro-compat

    public void testDetermineRootPathJustWorks() {
        MagnoliaInitPaths paths = expectServletContextRealPath("/foo/bar");
        assertEquals("/foo/bar", paths.getRootPath());
    }

    public void testDetermineRootPathStripsTrailingSlash() {
        MagnoliaInitPaths paths = expectServletContextRealPath("/foo/bar/");
        assertEquals("/foo/bar", paths.getRootPath());
    }

    public void testDetermineRootPathTranslatesBackslashes() {
        MagnoliaInitPaths paths = expectServletContextRealPath("\\foo\\bar");
        assertEquals("/foo/bar", paths.getRootPath());
    }

    public void testDetermineRootPathTranslatesBackslashesAndStripsTrailingSlash() {
        MagnoliaInitPaths paths = expectServletContextRealPath("\\foo\\bar\\");
        assertEquals("/foo/bar", paths.getRootPath());
    }

    public void testDetermineWebappFolderNameJustWorks() {
        MagnoliaInitPaths paths = expectServletContextRealPath("/foo/bar");
        assertEquals("bar", paths.getWebappFolderName());
    }

    public void testDetermineWebappFolderNameWorksWithTrailingSlashes() {
        MagnoliaInitPaths paths = expectServletContextRealPath("/foo/bar/");
        assertEquals("bar", paths.getWebappFolderName());
    }

    public void testDetermineWebappFolderNameWorksWithBackslashes() {
        MagnoliaInitPaths paths = expectServletContextRealPath("\\foo\\bar");
        assertEquals("bar", paths.getWebappFolderName());
    }

    public void testDetermineWebappFolderNameWorksWithTrailingSlashesAndBackslashes() {
        MagnoliaInitPaths paths = expectServletContextRealPath("\\foo\\bar\\");
        assertEquals("bar", paths.getWebappFolderName());
    }

    private MagnoliaInitPaths expectServletContextRealPath(String returnedPath) {
        expect(servletContext.getInitParameter("magnolia.unqualified.server.name")).andReturn(null).once();
        expect(servletContext.getRealPath("")).andReturn(returnedPath).once();
        expect(servletContext.getContextPath()).andReturn("/mycontext");
        replay(servletContext);
        return new DefaultMagnoliaInitPaths(new MagnoliaServletContextListener(), servletContext);
    }
}
