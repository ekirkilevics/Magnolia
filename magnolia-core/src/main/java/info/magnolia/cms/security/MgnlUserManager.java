/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.auth.Entity;
import info.magnolia.context.MgnlContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.security.auth.Subject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages the users stored in magnolia itself.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class MgnlUserManager implements UserManager {

    private static final Logger log = LoggerFactory.getLogger(MgnlUserManager.class);

    public static final String PROPERTY_EMAIL = "email";
    public static final String PROPERTY_LANGUAGE = "language";
    public static final String PROPERTY_LASTACCESS = "lastaccess";
    public static final String PROPERTY_PASSWORD = "pswd";
    public static final String PROPERTY_TITLE = "title";

    public static final String NODE_ACLUSERS = "acl_users";

    private String realmName;

    /**
     * Do not instantiate it!
     */
    public MgnlUserManager() {
    }

    // TODO : rename to getRealmName and setRealmName (and make sure Content2Bean still sets realmName using the
    // parent's node name)
    public String getName() {
        return getRealmName();
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
            log.debug("subject not set.");
            return new DummyUser();
        }

        Set<Entity> principalSet = subject.getPrincipals(Entity.class);
        Iterator<Entity> entityIterator = principalSet.iterator();
        if (!entityIterator.hasNext()) {
            // happens when JCR authentication module set to optional and user doesn't exist in magnolia
            log.debug("user name not contained in principal set.");
            return new DummyUser();
        }
        Entity userDetails = entityIterator.next();
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

        return newInstance(node);
    }

    /**
     * Helper method to find a user in a certain realm. Uses JCR Query.
     */
    protected Content findUserNode(String realm, String name) throws RepositoryException {
        String where = "where jcr:path = '/" + realm + "/" + name + "'";
        where += " or jcr:path like '/" + realm + "/%/" + name + "'";

        // the all realm searches the repository
        if (Realm.REALM_ALL.equals(realm)) {
            where = "where jcr:path like '%/" + name + "'";
        }

        String statement = "select * from " + ItemType.USER + " " + where;

        QueryManager qm = getHierarchyManager().getQueryManager();
        Query query = qm.createQuery(statement, Query.SQL);
        Collection<Content> users = query.execute().getContent(ItemType.USER.getSystemName());
        if (users.size() == 1) {
            return users.iterator().next();
        }
        else if (users.size() > 1) {
            log.error("More than one user found with name [{}] in realm [{}]");
        }
        return null;
    }

    /**
     * SystemUserManager does this.
     */
    public User getSystemUser() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * SystemUserManager does this.
     */
    public User getAnonymousUser() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * All users
     */
    public Collection<User> getAllUsers() {
        Collection<User> users = new ArrayList<User>();
        try {
            Collection<Content> nodes = getHierarchyManager().getRoot().getChildren(ItemType.USER);
            for (Content node : nodes) {
                users.add(newInstance(node));
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
        validateUsername(name);
        try {
            final Content node = createUserNode(name);
            node.createNodeData("name").setValue(name);
            setPasswordProperty(node, pw);
            node.createNodeData("language").setValue("en");

            final String handle = node.getHandle();
            final Content acls = node.createContent(NODE_ACLUSERS, ItemType.CONTENTNODE);
            // read only access to the node itself
            Content acl = acls.createContent(Path.getUniqueLabel(acls.getHierarchyManager(), acls.getHandle(), "0"), ItemType.CONTENTNODE);
            acl.setNodeData("path", handle);
            acl.setNodeData("permissions", new Long(Permission.READ));
            // those who had access to their nodes should get access to their own props
            addWrite(handle, PROPERTY_EMAIL, acls);
            addWrite(handle, PROPERTY_LANGUAGE, acls);
            addWrite(handle, PROPERTY_LASTACCESS, acls);
            addWrite(handle, PROPERTY_PASSWORD, acls);
            addWrite(handle, PROPERTY_TITLE, acls);
            // and of course the meta data
            addWrite(handle, MetaData.DEFAULT_META_NODE, acls);

            getHierarchyManager().save();
            return newInstance(node);
        }
        catch (Exception e) {
            log.info("can't create user [" + name + "]", e);
            return null;
        }
    }

    public void changePassword(User user, String newPassword) {
        final Content userNode = ((MgnlUser) user).getUserNode();
        try {
            setPasswordProperty(userNode, newPassword);
            userNode.save();
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    protected void setPasswordProperty(Content userNode, String clearPassword) throws RepositoryException {
        userNode.createNodeData(PROPERTY_PASSWORD).setValue(encodePassword(clearPassword));
    }

    protected String encodePassword(String clearPassword) {
        return new String(Base64.encodeBase64(clearPassword.getBytes()));
    }

    protected void validateUsername(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException(name + " is not a valid username.");
        }
    }

    protected Content createUserNode(String name) throws RepositoryException {
        final String path = "/" + getRealmName();
        return getHierarchyManager().createContent(path, name, ItemType.USER.getSystemName());
    }

    /**
     * return the user HierarchyManager (through the system context)
     */
    protected HierarchyManager getHierarchyManager() {
        return MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.USERS);
    }

    /**
     * @deprecated since 4.3.1 - use {@link #newInstance(info.magnolia.cms.core.Content)}
     */
    protected MgnlUser userInstance(Content node) {
        return new MgnlUser(node);
    }

    /**
     * Creates a {@link MgnlUser} out of a jcr node. Can be overridden in order to provide a different implementation.
     * @since 4.3.1
     */
    protected User newInstance(Content node) {
        return userInstance(node);
    }

    private Content addWrite(String parentPath, String property, Content acls) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content acl = acls.createContent(Path.getUniqueLabel(acls.getHierarchyManager(), acls.getHandle(), "0"), ItemType.CONTENTNODE);
        acl.setNodeData("path", parentPath + "/" + property);
        acl.setNodeData("permissions", new Long(Permission.ALL));
        return acl;
    }
}
