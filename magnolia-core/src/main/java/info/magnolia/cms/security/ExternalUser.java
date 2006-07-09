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

import info.magnolia.cms.security.auth.Entity;
import info.magnolia.cms.security.auth.GroupList;
import info.magnolia.cms.security.auth.RoleList;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class wraps a user content object to provide some nice methods
 * @author philipp
 * @author Sameer Charles
 * @version $Revision:2558 $ ($Author:scharles $)
 */
public class ExternalUser implements User {

    public static Logger log = LoggerFactory.getLogger(ExternalUser.class);

    /**
     * user properties
     */
    private Entity userDetails;

    /**
     * user roles
     */
    private RoleList roleList;

    /**
     * user groups
     */
    private GroupList groupList;

    /**
     * @param subject as created by login module
     */
    protected ExternalUser(Subject subject) {
        Set principalSet = subject.getPrincipals(Entity.class);
        Iterator entityIterator = principalSet.iterator();
        this.userDetails = (Entity) entityIterator.next();
        principalSet = subject.getPrincipals(RoleList.class);
        Iterator roleListIterator = principalSet.iterator();
        this.roleList = (RoleList) roleListIterator.next();
        principalSet = subject.getPrincipals(GroupList.class);
        Iterator groupListIterator = principalSet.iterator();
        this.groupList = (GroupList) groupListIterator.next();
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserInterface#hasRole(java.lang.String)
     */
    public boolean hasRole(String roleName) {
        return this.roleList.has(roleName);
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserInterface#removeRole(java.lang.String)
     */
    public void removeRole(String roleName) {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserInterface#addRole(java.lang.String)
     */
    public void addRole(String roleName) {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    /**
     * Is this user in a specified group?
     * @param groupName
     * @return true if in group
     */
    public boolean inGroup(String groupName) {
        return this.groupList.has(groupName);
    }

    /**
     * Remove a group. Implementation is optional
     * @param groupName
     */
    public void removeGroup(String groupName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    /**
     * Adds this user to a group. Implementation is optional
     * @param groupName
     */
    public void addGroup(String groupName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserInterface#getLanguage()
     */

    public String getLanguage() {
        return (String) this.userDetails.getProperty(Entity.LANGUAGE); 
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserInterface#getName()
     */
    public String getName() {
        return (String) this.userDetails.getProperty(Entity.NAME); 
    }

    /**
     * get user password
     * @return password string
     */
    public String getPassword() {
        return (String) this.userDetails.getProperty(Entity.PASSWORD);
    }

    /**
     * @see info.magnolia.cms.security.User#getGroups()
     */
    public Collection getGroups() {
        return this.groupList.getList();
    }

    /**
     * @see info.magnolia.cms.security.User#getRoles()
     */
    public Collection getRoles() {
        return this.roleList.getList();
    }
}