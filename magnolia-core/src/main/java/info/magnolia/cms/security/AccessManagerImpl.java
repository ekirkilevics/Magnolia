/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.security;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * @author Sameer Charles
 * @version $Id$
 */
public class AccessManagerImpl implements AccessManager, Serializable {

    private static final long serialVersionUID = 222L;

    private List userPermissions;

    /**
     * Check if the given path has specified permissions
     * @param path
     * @param permissions
     * @return true if the given path has this permissions
     */
    public boolean isGranted(String path, long permissions) {
        if (StringUtils.isEmpty(path)) {
            path = "/"; //$NON-NLS-1$
        }

        long currentPermission = getPermissions(path);
        boolean granted = (currentPermission & permissions) == permissions;

        return granted;
    }

    /**
     * Set list of permissions for this access manager
     * @param permissions list of values assigned to this access manager
     */
    public void setPermissionList(List permissions) {
        this.userPermissions = permissions;
    }

    /**
     * Get permision list assigned to this access manager
     */
    public List getPermissionList() {
        return this.userPermissions;
    }

    /**
     * Get permissions assigned to the given path.
     * @param path
     * @see info.magnolia.cms.security.Permission
     * @return highest permission assigned to this path
     */
    public long getPermissions(String path) {
        if (userPermissions == null) {
            return Permission.NONE;
        }
        long permission = 0;
        int patternLength = 0;
        for (int i = 0; i < userPermissions.size(); i++) {
            Permission p = (Permission) userPermissions.get(i);
            if (p.match(path)) {
                int l = p.getPattern().getLength();
                if (patternLength == l && (permission < p.getPermissions())) {
                    permission = p.getPermissions();
                }
                else if (patternLength < l) {
                    patternLength = l;
                    permission = p.getPermissions();
                }
            }
        }
        return permission;
    }
}
