/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.servlet.ServletContext;

import static org.easymock.EasyMock.*;

/**
 * TODO : we should also handle the cases when servletContext.getRealPath() returns null - see javadoc
 *
 * @version $Id$
 */
public class DefaultMagnoliaInitPathsTest {
    private ServletContext servletContext;

    @Before
    public void setUp() throws Exception {
        servletContext = createStrictMock(ServletContext.class);
    }

    @After
    public void tearDown() throws Exception {
        verify(servletContext);
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    // TODO : test methods for retro-compat

    @Test
    public void testDetermineRootPathJustWorks() {
        MagnoliaInitPaths paths = expectServletContextRealPath("/foo/bar");
        assertEquals("/foo/bar", paths.getRootPath());
    }

    @Test
    public void testDetermineRootPathStripsTrailingSlash() {
        MagnoliaInitPaths paths = expectServletContextRealPath("/foo/bar/");
        assertEquals("/foo/bar", paths.getRootPath());
    }

    @Test
    public void testDetermineRootPathTranslatesBackslashes() {
        MagnoliaInitPaths paths = expectServletContextRealPath("\\foo\\bar");
        assertEquals("/foo/bar", paths.getRootPath());
    }

    @Test
    public void testDetermineRootPathTranslatesBackslashesAndStripsTrailingSlash() {
        MagnoliaInitPaths paths = expectServletContextRealPath("\\foo\\bar\\");
        assertEquals("/foo/bar", paths.getRootPath());
    }

    @Test
    public void testDetermineWebappFolderNameJustWorks() {
        MagnoliaInitPaths paths = expectServletContextRealPath("/foo/bar");
        assertEquals("bar", paths.getWebappFolderName());
    }

    @Test
    public void testDetermineWebappFolderNameWorksWithTrailingSlashes() {
        MagnoliaInitPaths paths = expectServletContextRealPath("/foo/bar/");
        assertEquals("bar", paths.getWebappFolderName());
    }

    @Test
    public void testDetermineWebappFolderNameWorksWithBackslashes() {
        MagnoliaInitPaths paths = expectServletContextRealPath("\\foo\\bar");
        assertEquals("bar", paths.getWebappFolderName());
    }

    @Test
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
