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

import java.util.Collection;

import javax.security.auth.Subject;


/**
 * Used to get the current or other User objects.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public interface UserManager {

    /**
     * Magnolia system user.
     */
    public static final String SYSTEM_USER = "superuser";

    /**
     * Magnolia system default password
     */
    public static final String SYSTEM_PSWD = "superuser";

    /**
     * Anonymous user
     */
    public static final String ANONYMOUS_USER = "anonymous";

    /**
     * Find a specific user. Not all implementations will support this method.
     * @param name the name of the user
     * @return the user object
     */
    public User getUser(String name) throws UnsupportedOperationException;

    /**
     * Initialize new user using JAAS authenticated/authorized subject
     * @param subject
     * @throws UnsupportedOperationException
     */
    public User getUser(Subject subject) throws UnsupportedOperationException;

    /**
     * Get system user, this user must always exist in magnolia repository.
     * @return system user
     */
    public User getSystemUser() throws UnsupportedOperationException;

    /**
     * Get Anonymous user, this user must always exist in magnolia repository.
     * @return anonymous user
     */
    public User getAnonymousUser() throws UnsupportedOperationException;

    /**
     * Get all users.
     * @return collection of User objects
     * @throws UnsupportedOperationException
     */
    public Collection getAllUsers() throws UnsupportedOperationException;

    /**
     * Creates a user without security restrictions
     * @param name user name
     * @param pw password
     * @return the new user object
     * @throws UnsupportedOperationException
     */
    public User createUser(String name, String pw) throws UnsupportedOperationException;

    public void changePassword(User user, String newPassword) throws UnsupportedOperationException;

}
