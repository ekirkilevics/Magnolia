/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.importexport;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @version $Id$
 */
public class BootstrapUtilTest {

    protected static final String MGNL_BOOTSTRAP_FILE = "/mgnl-bootstrap/foo/config.server.i18n.xml";
    protected static final String MGNL_BOOTSTRAP_FILE_NEW_STYLE = "/mgnl-bootstrap/foo/config.server.foo..i18n.xml";

    @Test
    public void testGetWorkspaceNameFromResource() throws Exception {
        String workspace = BootstrapUtil.getWorkspaceNameFromResource(MGNL_BOOTSTRAP_FILE);
        assertEquals("config", workspace);

        workspace = BootstrapUtil.getWorkspaceNameFromResource(MGNL_BOOTSTRAP_FILE_NEW_STYLE);
        assertEquals("config", workspace);
    }

    @Test
    public void testGetPathnameFromResource() throws Exception {
        String pathname = BootstrapUtil.getPathnameFromResource(MGNL_BOOTSTRAP_FILE);
        assertEquals("/server", pathname);

        pathname = BootstrapUtil.getPathnameFromResource(MGNL_BOOTSTRAP_FILE_NEW_STYLE);
        assertEquals("/server", pathname);
    }

    @Test
    public void testGetFullpathFromResource() throws Exception {
        String fullpath = BootstrapUtil.getFullpathFromResource(MGNL_BOOTSTRAP_FILE);
        assertEquals("/server/i18n", fullpath);

        fullpath = BootstrapUtil.getFullpathFromResource(MGNL_BOOTSTRAP_FILE_NEW_STYLE);
        assertEquals("/server/foo.i18n", fullpath);
    }

    @Test
    public void testGetFilenameFromResource() throws Exception {
        String fileName = BootstrapUtil.getFilenameFromResource(MGNL_BOOTSTRAP_FILE, null);
        assertEquals("config.server.i18n", fileName);

        //no dot in the extension
        fileName = BootstrapUtil.getFilenameFromResource(MGNL_BOOTSTRAP_FILE, "xml");
        assertEquals("config.server.i18n", fileName);

        fileName = BootstrapUtil.getFilenameFromResource(MGNL_BOOTSTRAP_FILE_NEW_STYLE, null);
        assertEquals("config.server.foo..i18n", fileName);

        //a fancy extension
        fileName = BootstrapUtil.getFilenameFromResource("/mgnl-bootstrap/foo/bar..baz.qux", ".qux");
        assertEquals("bar..baz", fileName);
    }
}
