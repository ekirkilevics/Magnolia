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

import java.util.Collection;


/**
 * Used to get the current or other User objects.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public interface UserManager {

    /**
     * Returns the current user
     * @return the current user
     */
    public User getCurrent();

    /**
     * Set the current user
     */
    public void setCurrent(User user);
    
    /**
     * Find a specific user. Not all implementations will support this method.
     * @param name the name of the user
     * @return the user object
     */
    public User getUser(String name) throws UnsupportedOperationException;

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