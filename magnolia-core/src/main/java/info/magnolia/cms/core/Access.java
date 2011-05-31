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
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;

import java.text.MessageFormat;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Simply utility class for AccessManager.
 *
 * @version $Id$
 */
public final class Access {

    /**
     * Utility class, don't instantiate.
     */
    private Access() {
        // unused
    }

    /**
     * Checks if the given AccessManager allows specified permission on the given path.
     * If no AccessManager is passed, the permission is assumed granted, no exception is thrown.
     *
     * @throws AccessDeniedException if the permission isn't granted.
     * @deprecated AccessManager is no longer supported and used. Use JCR Session based security instead.
     */
    @Deprecated
    public static void isGranted(AccessManager manager, String path, long permissions) throws AccessDeniedException {
        if (manager != null && !manager.isGranted(path, permissions)) {
            throw new AccessDeniedException(MessageFormat.format("User not allowed to {0} path [{1}]", PermissionImpl.getPermissionAsName(permissions), path));
        }
    }

    /**
     * Checks whether given session has requested permission on provided path. Throws an exception if permission is not granted on given path.
     * @throws AccessDeniedException when permission is not granted.
     */
    public static void tryPermission(Session jcrSession, String path, String action) throws AccessDeniedException {
        try {
            if (!jcrSession.hasPermission( path, action)) {
                throw new AccessDeniedException("Not allowed to access " + path + " with permission " + action);
            }
        } catch (RepositoryException e) {
            throw new AccessDeniedException("Exception occured while checking permissions for " + path + " with permission " + action, e);
        }
    }

    /**
     * Return whether given session has requested permission on provided path.
     */
    public static boolean isGranted(Session jcrSession, String path, String action) {
        try {
            return jcrSession.hasPermission( path, action);
        } catch (RepositoryException e) {
            return false;
        }
    }

    /**
     * Return String-representation of permissions convert from provided long-permission (old).
     */
    public static String convertPermissions(long oldPermissions) {
        String permissions = "";
        //TODO: review && convert all the permissions properly
        if ((oldPermissions & Permission.ALL) == Permission.ALL) {
            permissions = Session.ACTION_ADD_NODE + "," + Session.ACTION_READ + "," + Session.ACTION_REMOVE + "," + Session.ACTION_SET_PROPERTY;
        } else if ((oldPermissions & Permission.WRITE) == Permission.WRITE) {
            permissions = Session.ACTION_ADD_NODE;
        }
        else if ((oldPermissions & Permission.READ) == Permission.READ) {
            permissions = Session.ACTION_READ;
        }
        return permissions;
    }
}
