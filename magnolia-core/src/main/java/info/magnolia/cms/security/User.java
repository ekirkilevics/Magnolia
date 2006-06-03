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


/**
 * Represents a magnolia user.
 * @author philipp
 * @version $Revision:2558 $ ($Author:scharles $)
 */
public interface User {

    /**
     * Is this user in a specified role?
     * @param roleName the name of the role
     * @return true if in role
     */
    public boolean hasRole(String roleName);

    /**
     * Remove a role. Implementation is optional
     */
    public void removeRole(String roleName) throws UnsupportedOperationException;

    /**
     * Adds a role to this user. Implementation is optional
     * @param roleName the name of the role
     */
    public void addRole(String roleName) throws UnsupportedOperationException;

    /**
     * Is this user in a specified group?
     * @param groupName
     * @return true if in group
     */
    public boolean inGroup(String groupName);

    /**
     * Remove a group. Implementation is optional
     * @param groupName
     */
    public void removeGroup(String groupName) throws UnsupportedOperationException;

    /**
     * Adds this user to a group. Implementation is optional
     * @param groupName
     */
    public void addGroup(String groupName) throws UnsupportedOperationException;

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

    /**
     * get user password
     * @return password string
     */
    public abstract String getPassword();

    /**
     * get groups that user is in
     * @return
     */
    public abstract Collection getGroups();

    /**
     * get roles tha are assigned to user
     * @return
     */
    public abstract Collection getRoles();
}