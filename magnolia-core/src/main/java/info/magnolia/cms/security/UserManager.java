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
package info.magnolia.cms.security;

import info.magnolia.cms.security.auth.ACL;

import java.util.Collection;
import java.util.Map;

import javax.security.auth.Subject;


/**
 * Manages users.
 * @version $Revision$ ($Author$)
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
     * Sets a new password.
     * @return user object with updated password.
     * @throws UnsupportedOperationException if the current implementation doesn't support this operation
     */
    public User changePassword(User user, String newPassword) throws UnsupportedOperationException;

    /**
     * Grants user role.
     * @return user object with the role already granted.
     */
    public User addRole(User user, String roleName);

    /**
     * Updates last access timestamp for the user.
     * @throws UnsupportedOperationException if the current implementation doesn't support this operation
     */
    public void updateLastAccessTimestamp(User user) throws UnsupportedOperationException;

    /**
     * Checks whether principal belongs to the named resource.
     * @param name principal name
     * @param resourceName either group or role name
     * @param resourceType either group or role see
     * @return
     */
    public boolean hasAny(String principal, String resourceName, String resourceType);

    /**
     * Returns all ACLs assigned to the given user.
     * @return
     */
    public Map<String, ACL> getACLs(User user);


}
