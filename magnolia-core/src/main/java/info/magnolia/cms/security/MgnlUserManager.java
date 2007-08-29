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
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.auth.Entity;
import info.magnolia.context.MgnlContext;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.security.auth.Subject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Manages the users stored in magnolia itself.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class MgnlUserManager implements UserManager {
    private static final Logger log = LoggerFactory.getLogger(MgnlUserManager.class);

    private String realmName;

    /**
     * Do not instantiate it!
     */
    public MgnlUserManager() {
    }

    // TODO : rename to getRealmName and setRealmName (and make sure Content2Bean still sets realmName using the parent's node name)
    public String getName() {
        return realmName;
    }

    public void setName(String name) {
        this.realmName = name;
    }

    public String getRealmName() {
        return realmName;
    }

    /**
     * Get the user object. Uses a search
     * @param name
     * @return the user object
     */
    public User getUser(String name) {
        try {
            return getFromRepository(name);
        }
        catch (RepositoryException e) {
            log.info("Unable to load user [" + name + "] due to: " + e.toString(), e);
            return null;
        }
    }

    public User getUser(Subject subject) throws UnsupportedOperationException {
        // this could be the case if no one is logged in yet
        if (subject == null) {
            return new DummyUser();
        }

        Set principalSet = subject.getPrincipals(Entity.class);
        Iterator entityIterator = principalSet.iterator();
        Entity userDetails = (Entity) entityIterator.next();
        String name = (String) userDetails.getProperty(Entity.NAME);
        try {
            return getFromRepository(name);
        }
        catch (PathNotFoundException e) {
            log.error("user not registered in magnolia itself [" + name + "]");
        }
        catch (Exception e) {
            log.error("can't get jcr-node of current user", e);
        }

        return new DummyUser();
    }

    protected User getFromRepository(String name) throws RepositoryException {
        final Content node = findUserNode(this.realmName, name);
        if (node == null) {
            log.debug("User not found: [{}]", name);
            return null;
        }
        final MgnlUser user = new MgnlUser(node);
        if (!user.getName().equals(ANONYMOUS_USER)) {
            user.setLastAccess();
        }
        return user;
    }

    /**
     * Helper method to find a user in a certain realm. Uses JCR Query.
     */
    protected Content findUserNode(String realm, String name) throws RepositoryException {
        String jcrPath = "%/" + name;

        if (Realm.REALM_ADMIN.equals(realm)) {
            log.warn("TEMPORARY SOLUTION WILL RESET THE REALM TO ALL");
            realm = Realm.REALM_ALL;
        }

        if (!Realm.REALM_ALL.equals(realm)) {
            jcrPath = "/" + realm + "/" + jcrPath;
        }

        String statement = "select * from " + ItemType.USER + " where jcr:path like '" + jcrPath + "'";

        QueryManager qm = MgnlContext.getSystemContext().getQueryManager(ContentRepository.USERS);
        Query query = qm.createQuery(statement, Query.SQL);
        Collection users = query.execute().getContent(ItemType.USER.getSystemName());
        if (users.size() == 1) {
            return (Content) users.iterator().next();
        } else if (users.size() > 1) {
            log.error("More than one user found with name [{}] in realm [{}]");
        }
        return null;
    }

    /**
     * Get system user, this user must always exist in magnolia repository.
     * @return system user
     */
    public User getSystemUser() {
        return getOrCreateUser(UserManager.SYSTEM_USER, UserManager.SYSTEM_PSWD);
    }

    /**
     * Get Anonymous user, this user must always exist in magnolia repository.
     * @return anonymous user
     */
    public User getAnonymousUser() {
        return getOrCreateUser(UserManager.ANONYMOUS_USER, "");
    }

    protected User getOrCreateUser(String userName, String password) {
        User user = getUser(userName);
        if (user == null) {
            log.error("failed to get system or anonymous user [{}]", userName);
            log.info("Try to create new system user with default password");
            user = this.createUser(userName, password);
        }
        return user;
    }


    /**
     * All users
     */
    public Collection getAllUsers() {
        Collection users = new ArrayList();
        try {
            Collection nodes = getHierarchyManager().getRoot().getChildren(ItemType.USER);
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
            final Content node = createUserNode(name);
            node.createNodeData("name").setValue(name);
            node.createNodeData("pswd").setValue(new String(Base64.encodeBase64(pw.getBytes())));
            node.createNodeData("language").setValue("en");
            getHierarchyManager().save();
            return new MgnlUser(node);
        }
        catch (Exception e) {
            log.info("can't create user [" + name + "]", e);
            return null;
        }
    }

    protected Content createUserNode(String name) throws RepositoryException {
        final String path = "/" + getRealmName();
        return getHierarchyManager().createContent(path, name, ItemType.USER.getSystemName());
    }

    /**
     * return the user HierarchyManager
     */
    protected HierarchyManager getHierarchyManager() {
        return ContentRepository.getHierarchyManager(ContentRepository.USERS);
    }

}
