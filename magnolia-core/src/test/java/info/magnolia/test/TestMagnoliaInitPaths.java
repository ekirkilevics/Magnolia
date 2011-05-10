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
package info.magnolia.test;

import info.magnolia.init.MagnoliaInitPaths;
import org.junit.Ignore;

/**
 * An implementation {@link MagnoliaInitPaths} which doesn't have any dependency and can easily be constructed in tests.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
@Ignore
public class TestMagnoliaInitPaths implements MagnoliaInitPaths {
    private final String serverName;
    private final String rootPath;
    private final String webapp;
    private final String contextPath;

    public TestMagnoliaInitPaths() {
        this("test-host-name", "/tmp/magnoliaTests", "magnoliaTests", "/test");
    }

    public TestMagnoliaInitPaths(String serverName, String rootPath, String webappFolderName, String contextPath) {
        this.serverName = serverName;
        this.rootPath = rootPath;
        this.webapp = webappFolderName;
        this.contextPath = contextPath;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public String getRootPath() {
        return rootPath;
    }

    @Override
    public String getWebappFolderName() {
        return webapp;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }
}
