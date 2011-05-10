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
package info.magnolia.cms.security;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * Default implementation for {@link AccessManager}.
 * @version $Id$
 */
public class AccessManagerImpl implements AccessManager, Serializable {

    private static final long serialVersionUID = 222L;

    private List<Permission> userPermissions;

    @Override
    public boolean isGranted(String path, long permissions) {
        if (StringUtils.isEmpty(path)) {
            path = "/"; //$NON-NLS-1$
        }

        long currentPermission = getPermissions(path);
        boolean granted = (currentPermission & permissions) == permissions;

        return granted;
    }

    @Override
    public void setPermissionList(List<Permission> permissions) {
        this.userPermissions = permissions;
    }

    @Override
    public List<Permission> getPermissionList() {
        return this.userPermissions;
    }

    @Override
    public long getPermissions(String path) {
        if (userPermissions == null) {
            return Permission.NONE;
        }
        long permission = 0;
        int patternLength = 0;
        for (Permission p : userPermissions) {
            if (p.match(path)) {
                int l = p.getPattern().getLength();
                if (patternLength == l && (permission < p.getPermissions())) {
                    permission = p.getPermissions();
                } else if (patternLength < l) {
                    patternLength = l;
                    permission = p.getPermissions();
                }
            }
        }
        return permission;
    }
}
