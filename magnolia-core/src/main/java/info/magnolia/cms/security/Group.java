/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
 * @author Sameer Charles $Id:Group.java 9391 2007-05-11 15:48:02Z scharles $
 */
public interface Group extends Serializable {

    /**
     * get name of this node
     * @return group name
     */
    public String getName();

    /**
     * add role to this group
     * @param roleName
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if loggen in repository user does not sufficient rights
     */
    public void addRole(String roleName) throws UnsupportedOperationException, AccessDeniedException;

    /**
     * add subgroup to this group
     * @param groupName
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if loggen in repository user does not sufficient rights
     */
    public void addGroup(String groupName) throws UnsupportedOperationException, AccessDeniedException;

    /**
     * remove role from this group
     * @param roleName
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if loggen in repository user does not sufficient rights
     */
    public void removeRole(String roleName) throws UnsupportedOperationException, AccessDeniedException;

    /**
     * remove subgroup from this group
     * @param groupName
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if loggen in repository user does not sufficient rights
     */
    public void removeGroup(String groupName) throws UnsupportedOperationException, AccessDeniedException;

    /**
     * returns true if role exist in this group
     * @param roleName
     * @throws UnsupportedOperationException if the implementation does not exist
     * @throws AccessDeniedException if loggen in repository user does not sufficient rights
     */
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
     * get groups that are directly assigned to group
     */
    public Collection<String> getGroups();

    /**
     * get all groups assigned to this group, collected recursively from subgroups
     * */
    public Collection<String> getAllGroups();

    /**
     * get roles that are directly assigned to group
     */
    public Collection<String> getRoles();

}
