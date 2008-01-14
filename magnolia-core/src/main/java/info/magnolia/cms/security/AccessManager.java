/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.security;

import java.io.Serializable;
import java.util.List;


/**
 * @author Sameer Charles
 * @version $Revision:2558 $ ($Author:scharles $)
 */
public interface AccessManager extends Serializable {

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
     * Get permision list assigned to this access manager
     */
    List getPermissionList();

    /**
     * Get permissions assigned to the given path.
     * @see Permission all possible permissions
     * @param path for which permissions are requested
     * @return permission mask
     */
    long getPermissions(String path);
}
