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


/**
 * @author Sameer Charles
 * @version 2.01
 */
public interface AccessManager {

    /**
     * Determines wether the specified permissions are granted to the given path.
     * @param path path for which permissions are checked
     * @param permissions permission mask
     * @return true if this accessmanager has permissions to the specified path
     */
    boolean isGranted(String path, long permissions);

    /**
     * Sets the list of permissions this manager will use to determine access, implementation is free to define the
     * structure of this list.
     * @param permissions
     */
    void setPermissionList(List permissions);

    /**
     * Get permissions assigned to the given path.
     * @see Permission all possible permissions
     * @param path for which permissions are requested
     * @return permission mask
     */
    long getPermissions(String path);
}
