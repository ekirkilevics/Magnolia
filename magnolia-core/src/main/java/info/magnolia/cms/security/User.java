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

import javax.security.auth.Subject;

/**
 * Represents a magnolia user.
 *
 * @author philipp
 * @version $Revision:2558 $ ($Author:scharles $)
 */
public interface User extends Serializable {

    /**
     * Is this user in a specified role?
     * @param roleName the name of the role
     * @return true if in role
     */
    boolean hasRole(String roleName);

    /**
     * Remove a role. Implementation is optional
     */
    void removeRole(String roleName) throws UnsupportedOperationException;

    /**
     * Adds a role to this user. Implementation is optional
     * @param roleName the name of the role
     */
    void addRole(String roleName) throws UnsupportedOperationException;

    /**
     * Is this user in a specified group?
     * @return true if in group
     */
    boolean inGroup(String groupName);

    /**
     * Remove a group. Implementation is optional
     */
    void removeGroup(String groupName) throws UnsupportedOperationException;

    /**
     * Adds this user to a group. Implementation is optional
     */
    void addGroup(String groupName) throws UnsupportedOperationException;

    /**
     * Returns false if the user was explicitly disabled. Implementations should return
     * true by default if the status is unknown.
     */
    boolean isEnabled();

    void setEnabled(boolean enabled);

    /**
     * get user language
     * @return language string
     */
    String getLanguage();

    /**
     * get user name
     * @return name string
     */
    String getName();

    /**
     * get user password
     * @return password string
     */
    String getPassword();

    /**
     * Gets an arbitrary property from this user.
     */
    String getProperty(String propertyName);

    /**
     * Sets an arbitrary property for this user.
     * Values are currently Strings; we'd need some kind of abstract encoding mechanism to allow other types if needed.
     */
    void setProperty(String propertyName, String value);

    /**
     * get groups that user is in
     */
    Collection<String> getGroups();

    /**
     * get all groups to which this user belongs to, collected recursively including
     * */
    Collection<String> getAllGroups();

    /**
     * get roles that are directly assigned to user
     */
    Collection<String> getRoles();

    /**
     * get all roles assigned to this user, collected recursively including groups/subgroups
     * */
    Collection<String> getAllRoles();

    /**
     * Returns the jaas subject if available.
     */
    Subject getSubject();

    /**
     * The jass login handler will set the subject
     *
     */
    void setSubject(Subject subject);
}
