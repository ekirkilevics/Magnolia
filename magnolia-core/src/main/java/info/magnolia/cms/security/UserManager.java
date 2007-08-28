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
    public User getSystemUser();

    /**
     * Get Anonymous user, this user must always exist in magnolia repository.
     * @return anonymous user
     */
    public User getAnonymousUser();

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

}