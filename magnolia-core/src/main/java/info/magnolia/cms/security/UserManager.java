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

import info.magnolia.cms.security.auth.ACL;

import java.util.Collection;
import java.util.Map;

import javax.jcr.Value;
import javax.security.auth.Subject;


/**
 * Manages users.
 */
public interface UserManager {

    /**
     * Magnolia system user name.
     */
    public static final String SYSTEM_USER = "superuser";

    /**
     * Magnolia system default password.
     */
    public static final String SYSTEM_PSWD = "superuser";

    /**
     * Anonymous user name.
     */
    public static final String ANONYMOUS_USER = "anonymous";

    /**
     * Find a specific user. Not all implementations will support this method.
     * @param name the name of the user
     * @return the user object
     */
    public User getUser(String name) throws UnsupportedOperationException;

    /**
     * Find a specific user. Not all implementations will support this method.
     * @param id user identifier
     * @return the user object
     */
    public User getUserById(String id) throws UnsupportedOperationException;

    /**
     * Initialize new user using JAAS authenticated/authorized subject.
     * @throws UnsupportedOperationException if the current implementation doesn't support this operation
     * @deprecated jaas login module should just request the user, not pass the subject around to the user manager
     */
    @Deprecated
    public User getUser(Subject subject) throws UnsupportedOperationException;

    /**
     * Get system user, this user must always exist in magnolia repository.
     * @throws UnsupportedOperationException if the current implementation doesn't support this operation
     */
    public User getSystemUser() throws UnsupportedOperationException;

    /**
     * Get Anonymous user, this user must always exist in magnolia repository.
     * @throws UnsupportedOperationException if the current implementation doesn't support this operation
     */
    public User getAnonymousUser() throws UnsupportedOperationException;

    /**
     * Get all users.
     * @return collection of User objects
     * @throws UnsupportedOperationException if the current implementation doesn't support this operation
     */
    public Collection<User> getAllUsers() throws UnsupportedOperationException;

    /**
     * Creates a user without security restrictions.
     * @throws UnsupportedOperationException if the current implementation doesn't support this operation
     */
    public User createUser(String name, String pw) throws UnsupportedOperationException;

    /**
     * Creates a user on given path.
     * @throws UnsupportedOperationException if the current implementation doesn't support this operation
     */
    public User createUser(String path, String name, String pw) throws UnsupportedOperationException;

    /**
     * Sets a new password.
     * @return user object with updated password.
     * @throws UnsupportedOperationException if the current implementation doesn't support this operation
     */
    public User changePassword(User user, String newPassword) throws UnsupportedOperationException;

    /**
     * Sets given property for the user.
     *
     * @deprecated since 4.5.7 - use {@link UserManager#setProperty(User, String, String)}
     *
     * @param user
     *            User to be updated. If property doesn't exist yet, it will be created. If the value is null, property will be removed if existing.
     * @param propertyName
     *            Name of the property.
     * @param propertyValue
     *            Value of the property. Use org.apache.jackrabbit.value.ValueFactoryImpl to convert type to Value.
     * @return updated user object with new value of the property.
     */
    @Deprecated
    public User setProperty(User user, String propertyName, Value propertyValue);

    /**
     * Sets given property for the user and returns updated user object with new value of the property.
     */
    public User setProperty(User user, String propertyName, String propertyValue);

    /* ---------- User Manager configuration ----------- */

    /**
     * Sets a time period for account lock.
     * @throws UnsupportedOperationException if the current implementation doesn't support this operation
     */
    public void setLockTimePeriod(int lockTimePeriod) throws UnsupportedOperationException;

    /**
     * Gets a time period for account lock.
     * @throws UnsupportedOperationException if the current implementation doesn't support this operation
     */
    public int getLockTimePeriod() throws UnsupportedOperationException;

    /**
     * Sets a number of failed attempts before locking account.
     * @throws UnsupportedOperationException if the current implementation doesn't support this operation
     */
    public void setMaxFailedLoginAttempts(int maxFailedLoginAttempts) throws UnsupportedOperationException;

    /**
     * Gets a number of failed attempts before locking account.
     * @throws UnsupportedOperationException if the current implementation doesn't support this operation
     */
    public int getMaxFailedLoginAttempts() throws UnsupportedOperationException;

    /**
     * Grants user role.
     * @return user object with the role already granted.
     */
    public User addRole(User user, String roleName);

    /**
     * Adds user to a group.
     *
     * @return user object with the group already assigned.
     */
    public User addGroup(User user, String groupName);

    /**
     * Updates last access timestamp for the user.
     *
     * @throws UnsupportedOperationException
     *             if the current implementation doesn't support this operation
     */
    public void updateLastAccessTimestamp(User user) throws UnsupportedOperationException;

    /**
     * @return whether principal belongs to the named resource.
     * @param principal name of the principal
     * @param resourceName either group or role name
     * @param resourceType either group or role see
     *
     */
    public boolean hasAny(String principal, String resourceName, String resourceType);

    /**
     * @return all ACLs assigned to the given user.
     */
    public Map<String, ACL> getACLs(User user);

    /**
     * Removes user from a group.
     *
     * @return user object with the group assignment removed.
     */
    public User removeGroup(User user, String groupName);

    /**
     * Removes role from a user.
     *
     * @return user object without removed role.
     */
    public User removeRole(User user, String roleName);

}
