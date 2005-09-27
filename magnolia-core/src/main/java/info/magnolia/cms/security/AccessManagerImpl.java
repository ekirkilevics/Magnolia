/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.security;

import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * @author Sameer Charles
 * @version 2.01
 */
public class AccessManagerImpl implements AccessManager {

    private List userPermissions;

    public boolean isGranted(String path, long permissions) {
        if (StringUtils.isEmpty(path)) {
            return (getPermissions("/") & permissions) == permissions; //$NON-NLS-1$
        }
        return (getPermissions(path) & permissions) == permissions;
    }

    public void setPermissionList(List permissions) {
        this.userPermissions = permissions;
    }

    public long getPermissions(String path) {
        if (userPermissions == null) {
            return Permission.ALL;
        }
        long permission = 0;
        int patternLength = 0;
        for (int i = 0; i < userPermissions.size(); i++) {
            info.magnolia.cms.security.Permission p = (info.magnolia.cms.security.Permission) userPermissions.get(i);
            if (p.match(path)) {
                int l = p.getPattern().getLength();
                if (patternLength == l && (permission > p.getPermissions())) {
                    permission = p.getPermissions();
                }
                else if (patternLength <= l) {
                    patternLength = l;
                    permission = p.getPermissions();
                }
            }
        }
        return permission;
    }
}
