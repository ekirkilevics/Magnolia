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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.auth.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.security.auth.Subject;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages the users stored in magnolia itself.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class MgnlUserManager implements UserManager {

    /**
     * Logger
     */
    public static Logger log = LoggerFactory.getLogger(MgnlUserManager.class);

    /**
     * Do not instantiate it!
     */
    public MgnlUserManager() {
    }

    /**
     * Get the user object
     * @param name
     * @return the user object
     */
    public User getUser(String name) {
        try {
            Content node = getHierarchyManager().getContent(name);
            MgnlUser user = new MgnlUser(node);
            user.setLastAccess();
            return user;
        }
        catch (PathNotFoundException e) {
            log.debug("User not found: [{}]", name);
            return null;
        }
        catch (RepositoryException e) {
            log.info("Unable to load user [" + name + "] due to: " //
                + e.getClass().getName()
                + " - "
                + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get system user, this user must always exist in magnolia repository.
     * @return system user
     */
    public User getSystemUser() {
        try {
            return new MgnlUser(getHierarchyManager().getContent(UserManager.SYSTEM_USER));
        }
        catch (Exception e) {
            log.error("failed to get System user", e);
            log.info("Try to create new system user with default password");
            return this.createUser(UserManager.SYSTEM_USER, UserManager.SYSTEM_PSWD);
        }
    }

    /**
     * Get Anonymous user, this user must always exist in magnolia repository.
     * @return anonymous user
     */
    public User getAnonymousUser() {
        try {
            return new MgnlUser(getHierarchyManager().getContent(UserManager.ANONYMOUS_USER));
        }
        catch (Exception e) {
            log.error("failed to get Anonymous user", e);
            log.info("Try to create new system user with default password");
            return this.createUser(UserManager.ANONYMOUS_USER, "");
        }
    }

    /**
     * All users
     */
    public Collection getAllUsers() {
        Collection users = new ArrayList();
        try {
            Collection nodes = getHierarchyManager().getRoot().getChildren(ItemType.CONTENT);
            for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                users.add(new MgnlUser((Content) iter.next()));
            }
        }
        catch (Exception e) {
            log.error("can't find user");
        }
        return users;
    }

    /**
     * @param name
     * @param pw
     * @return the created User
     */
    public User createUser(String name, String pw) {
        try {
            Content node;
            node = getHierarchyManager().createContent("/", name, ItemType.CONTENT.getSystemName());
            node.createNodeData("name").setValue(name);
            node.createNodeData("pswd").setValue(new String(Base64.encodeBase64(pw.getBytes())));
            node.createNodeData("language").setValue("en");
            node.save();
            return new MgnlUser(node);
        }
        catch (Exception e) {
            log.info("can't create user [" + name + "]", e);
            return null;
        }
    }

    /**
     * Initialize new user using JAAS authenticated/authorized subject
     * @param subject
     * @throws UnsupportedOperationException
     */
    public User getUser(Subject subject) throws UnsupportedOperationException {
        User user = null;
        // this could be the case if no one is logged in yet
        if (subject == null) {
            return new DummyUser();
        }

        Set principalSet = subject.getPrincipals(Entity.class);
        Iterator entityIterator = principalSet.iterator();
        Entity userDetails = (Entity) entityIterator.next();
        String name = (String) userDetails.getProperty(Entity.NAME);
        try {
            Content node = getHierarchyManager().getContent(name);
            user = new MgnlUser(node);
            ((MgnlUser) user).setLastAccess();
        }
        catch (PathNotFoundException e) {
            log.error("user not registered in magnolia itself [" + name + "]");
        }
        catch (Exception e) {
            log.error("can't get jcr-node of current user", e);
        }
        if (user == null) {
            user = new DummyUser();
        }

        return user;
    }

    /**
     * return the user HierarchyManager
     */
    protected HierarchyManager getHierarchyManager() {
        return ContentRepository.getHierarchyManager(ContentRepository.USERS);
    }

}
