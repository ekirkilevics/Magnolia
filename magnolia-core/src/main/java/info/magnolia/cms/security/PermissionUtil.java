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
package info.magnolia.cms.security;

import info.magnolia.cms.security.auth.ACL;
import info.magnolia.context.MgnlContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of methods for handling permission related processing.
 * 
 * @version $Id$
 * 
 */
public class PermissionUtil {

    private static final Logger log = LoggerFactory.getLogger(PermissionUtil.class);

    /**
     * Retrieves permissions for current user.
     * 
     * @return
     */
    public static List<Permission> getPermissions(Subject subject, String name) {
        if (subject == null) {
            // FIXME: this needs to be cached if we really run anonymous w/o session
            log.warn("no session == running as anonymous");
            SecuritySupport secSupport = SecuritySupport.Factory.getInstance();
            Collection<String> roles = secSupport.getUserManager().getAnonymousUser().getAllRoles();
            RoleManager roleMan = secSupport.getRoleManager();
            List<Permission> permissions = new ArrayList<Permission>();
            for (String role : roles) {
                for (ACL acl : roleMan.getACLs(role).values()) {
                    if (name.equals(acl.getName())) {
                        // merge URI permissions from all roles
                        permissions.addAll(acl.getList());
                    }
                }
            }
            return permissions;
        }

        ACL acl = PrincipalUtil.findAccessControlList(subject, name);
        return acl != null ? acl.getList() : null;
    }

    /**
     * Convenience call hiding all ugly details of permission conversions.
     * 
     */
    public static boolean isGranted(String workspace, String path, String requiredPermissions) {
        AccessManager ami = MgnlContext.getAccessManager(workspace);
        return ami.isGranted(path, PermissionUtil.convertPermissions(requiredPermissions));
    }

    /**
     * Return whether given session has requested permission on provided path.
     */
    public static boolean isGranted(Session jcrSession, String path, String action) {
        // FIXME: treat custom permission that don't exist on Session.
        if (StringUtils.isBlank(action)) {
            return false;
        }
        try {
            return jcrSession.hasPermission( path, action);
        } catch (RepositoryException e) {
            return false;
        }
    }

    /**
     * Return String-representation of permissions convert from provided long-permission (old).
     */
    public static long convertPermissions(String newPermissions) {
        String[] perms = newPermissions.split(", ");
        long oldPerms = 0;
        for (String perm : perms) {
            if (Session.ACTION_ADD_NODE.equals(perm)) {
                oldPerms += Permission.WRITE;
            } else if (Session.ACTION_READ.equals(perm)) {
                oldPerms += Permission.READ;
            } else if (Session.ACTION_REMOVE.equals(perm)) {
                oldPerms += Permission.REMOVE;
            } else if (Session.ACTION_SET_PROPERTY.equals(perm)) {
                oldPerms += Permission.SET;
            }
        }
        return oldPerms;
    }

    /**
     * Return String-representation of permissions convert from provided long-permission (old).
     */
    public static String convertPermissions(long oldPermissions) {
        StringBuilder permissions = new StringBuilder();
        if ((oldPermissions & Permission.ALL) == Permission.ALL) {
            permissions.append(Session.ACTION_ADD_NODE).append(",").append(Session.ACTION_READ).append(",").append(Session.ACTION_REMOVE + ",").append(Session.ACTION_SET_PROPERTY);
            // skip the rest to be sure we don't introduce duplicates.
            return permissions.toString();
        }
        if ((oldPermissions & Permission.WRITE) == Permission.WRITE) {
            if (permissions.length() > 0) {
                permissions.append(",");
            }
            permissions.append(Session.ACTION_ADD_NODE);
        }
        if ((oldPermissions & Permission.READ) == Permission.READ) {
            if (permissions.length() > 0) {
                permissions.append(",");
            }
            permissions.append(Session.ACTION_READ);
        }
        if ((oldPermissions & Permission.REMOVE) == Permission.REMOVE) {
            if (permissions.length() > 0) {
                permissions.append(",");
            }
            permissions.append(Session.ACTION_REMOVE);
        }
        if ((oldPermissions & Permission.SET) == Permission.SET) {
            if (permissions.length() > 0) {
                permissions.append(",");
            }
            permissions.append(Session.ACTION_SET_PROPERTY);
        }
        return permissions.toString();
    }

    /**
     * Checks whether given session has requested permission on provided path. Throws an exception if permission is not granted on given path.
     * @throws AccessDeniedException when permission is not granted.
     */
    public static void verifyIsGrantedOrThrowException(Session jcrSession, String path, String action) throws AccessDeniedException {
        try {
            if (!jcrSession.hasPermission( path, action)) {
                throw new AccessDeniedException("Not allowed to access " + path + " with permission " + action);
            }
        } catch (RepositoryException e) {
            throw new AccessDeniedException("Exception occured while checking permissions for " + path + " with permission " + action, e);
        }
    }

}
