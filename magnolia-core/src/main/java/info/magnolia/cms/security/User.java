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

/**
 * Represents an magnolia user.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public interface User {

    /**
     * Is this user in a specified role?
     * @param roleName the name of the role
     * @return true if in role
     */
    public abstract boolean hasRole(String roleName);

    /**
     * Remove a role. Not all implementations will support this methods.
     */
    public abstract void removeRole(String roleName) throws UnsupportedOperationException;

    /**
     * Adds a role to this user. Not all implementations will support this method
     * @param roleName the name of the role
     */
    public abstract void addRole(String roleName) throws UnsupportedOperationException;

    /**
     * get user language
     * @return language string
     */

    public abstract String getLanguage();

    /**
     * get user name
     * @return name string
     */
    public abstract String getName();

}