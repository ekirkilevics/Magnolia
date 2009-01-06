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
package info.magnolia.context;

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.security.SystemUserManager;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.cms.util.WorkspaceAccessUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses a single full access AccessManager. Jcr sessions are only released if no event listener were registered.
 */
public class SystemRepositoryStrategy extends AbstractRepositoryStrategy {
    private static final long serialVersionUID = 222L;

    private AccessManager accessManager;

    public SystemRepositoryStrategy(SystemContext context) {
    }

    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        if (accessManager == null) {
            accessManager = WorkspaceAccessUtil.getInstance().createAccessManager(getSystemPermissions());
        }

        return accessManager;
    }

    protected List getSystemPermissions() {
        List acl = new ArrayList();
        UrlPattern p = UrlPattern.MATCH_ALL;
        Permission permission = new PermissionImpl();
        permission.setPattern(p);
        permission.setPermissions(Permission.ALL);
        acl.add(permission);
        return acl;
    }

    protected String getUserId() {
        return SystemUserManager.SYSTEM_USER;
    }

    public void release() {
        super.release(true);
    }

}
