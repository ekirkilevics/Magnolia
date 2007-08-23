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
     * @param groupName
     * @return true if in group
     */
    boolean inGroup(String groupName);

    /**
     * Remove a group. Implementation is optional
     * @param groupName
     */
    void removeGroup(String groupName) throws UnsupportedOperationException;

    /**
     * Adds this user to a group. Implementation is optional
     * @param groupName
     */
    void addGroup(String groupName) throws UnsupportedOperationException;

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
    Collection getGroups();

    /**
     * get all groups to which this user belongs to, collected recursively including
     * */
    public abstract Collection getAllGroups();

    /**
     * get roles that are directly assigned to user
     */
    Collection getRoles();

    /**
     * get all roles assigned to this user, collected recursively including groups/subgroups
     * */
    public abstract Collection getAllRoles();
}