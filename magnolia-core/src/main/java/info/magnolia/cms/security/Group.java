/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
import java.util.Collection;

/**
 * A user group. Groups are assigned to {@link User users} and can have sub groups.
 * @author Sameer Charles $Id:Group.java 9391 2007-05-11 15:48:02Z scharles $
 */
public interface Group extends Serializable {

    public String getName();

    /**
     * @deprecated use manager instead
     */
    @Deprecated
    public void addRole(String roleName) throws UnsupportedOperationException, AccessDeniedException;

    /**
     * @deprecated use manager instead
     */
    @Deprecated
    public void addGroup(String groupName) throws UnsupportedOperationException, AccessDeniedException;

    /**
     * @deprecated use manager instead
     */
    @Deprecated
    public void removeRole(String roleName) throws UnsupportedOperationException, AccessDeniedException;

    /**
     * @deprecated use manager instead
     */
    @Deprecated
    public void removeGroup(String groupName) throws UnsupportedOperationException, AccessDeniedException;

    public boolean hasRole(String roleName) throws UnsupportedOperationException, AccessDeniedException;

    /**
     * Gets an arbitrary property from this group.
     */
    String getProperty(String propertyName);

    /**
     * Sets an arbitrary property for this group.
     */
    void setProperty(String propertyName, String value);

    /**
     * Get groups that are directly assigned to group.
     */
    public Collection<String> getGroups();

    /**
     * Get all groups assigned to this group, collected recursively from subgroups.
     * */
    public Collection<String> getAllGroups();

    /**
     * Get roles that are directly assigned to group.
     */
    public Collection<String> getRoles();

    /**
     * Gets identifier of the group.
     */
    public String getId();

}
