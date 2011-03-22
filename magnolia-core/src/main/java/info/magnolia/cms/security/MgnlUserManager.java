/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.query.Query;
import javax.security.auth.Subject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Manages the users stored in Magnolia itself.
 * @version $Revision$ ($Author$)
 */
public class MgnlUserManager extends RepositoryBackedSecurityManager implements UserManager {

    private static final Logger log = LoggerFactory.getLogger(MgnlUserManager.class);

    public static final String PROPERTY_EMAIL = "email";
    public static final String PROPERTY_LANGUAGE = "language";
    public static final String PROPERTY_LASTACCESS = "lastaccess";
    public static final String PROPERTY_PASSWORD = "pswd";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_ENABLED = "enabled";

    public static final String NODE_ACLUSERS = "acl_users";

    private String realmName;

    /**
     * There should be no need to instantiate this class except maybe for testing. Manual instantiation might cause manager not to be initialized properly.
     */
    public MgnlUserManager() {
    }

    /**
     * TODO : rename to getRealmName and setRealmName (and make sure Content2Bean still sets realmName using the parent's node name).
     * @deprecated since 5.0 use realmName instead
     */
    @Deprecated
    public String getName() {
        return getRealmName();
    }

    /**
     * @deprecated since 5.0 use realmName instead
     */
    @Deprecated
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
    public User getUser(final String name) {
        try {
            return MgnlContext.doInSystemContext(new SessionOp<User, RepositoryException>(getRepositoryName()) {
                @Override
                public User exec(Session session) throws RepositoryException {
                    Node priviledgedUserNode = findPrincipalNode(name, session);
                    return newUserInstance(priviledgedUserNode);
                }
                @Override
                public String toString() {
                    return "retrieve user " + name;
                }
            });
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUser(Subject subject) throws UnsupportedOperationException {
        // this could be the case if no one is logged in yet
        if (subject == null) {
            log.debug("subject not set.");
            return new DummyUser();
        }

        Set<User> principalSet = subject.getPrincipals(User.class);
        Iterator<User> entityIterator = principalSet.iterator();
        if (!entityIterator.hasNext()) {
            // happens when JCR authentication module set to optional and user doesn't exist in magnolia
            log.debug("user name not contained in principal set.");
            return new DummyUser();
        }
        return entityIterator.next();
    }

    /**
     * Helper method to find a user in a certain realm. Uses JCR Query.
     * @deprecated since 5.0 use findUserNode(java.lang.String, java.lang.String, javax.jcr.Session) instead
     */
    @Deprecated
    protected Content findUserNode(String realm, String name) throws RepositoryException {
        // while we could call the other findUserNode method and wrap the output it would be inappropriate as session is not valid outside of the call
        throw new UnsupportedOperationException("Admin session is no longer kept open for unlimited duration of the time, therefore it is not possible to expose node outside of admin session.");
    }

    /**
     * Helper method to find a user in a certain realm. Uses JCR Query.
     */
    @Override
    protected Node findPrincipalNode(String name, Session session) throws RepositoryException {
        String realm = getRealmName();
        final String where;
        // the all realm searches the repository
        if (Realm.REALM_ALL.equals(realm)) {
            where = "where name() = '" + name + "'";
        } else {
            // FIXME: DOUBLE CHECK THE QUERY FOR REALMS ... ISDESCENDANTNODE and NAME ....
            where = "where name() = '" + name + "' and isdescendantnode(['/" + realm + "'])";
            //            where = "where [jcr:path] = '/" + realm + "/" + name + "'"
            //            + " or [jcr:path] like '/" + realm + "/%/" + name + "'";
        }

        final String statement = "select * from [" + ItemType.USER + "] " + where;

        Query query = session.getWorkspace().getQueryManager().createQuery(statement, Query.JCR_SQL2);
        NodeIterator iter = query.execute().getNodes();
        Node user = null;
        while (iter.hasNext()) {
            Node node = iter.nextNode();
            if (node.isNodeType(ItemType.USER.getSystemName())) {
                user = node;
                break;
            }
        }
        if (iter.hasNext()) {
            log.error("More than one user found with name [{}] in realm [{}]");
        }
        return user;
    }

    protected User getFromRepository(String name) throws RepositoryException {
        final Content node = findUserNode(this.realmName, name);
        if (node == null) {
            log.debug("User not found: [{}]", name);
            return null;
        }

        return newUserInstance(node);
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
     * Get all users managed by this user manager.
     */
    public Collection<User> getAllUsers() {
        return MgnlContext.doInSystemContext(new SilentSessionOp<Collection<User>>(getRepositoryName()) {

            @Override
            public Collection<User> doExec(Session session) throws RepositoryException {
                List<User> users = new ArrayList<User>();
                for (NodeIterator iter = session.getNode("/" + realmName).getNodes(); iter.hasNext();) {
                    Node node = iter.nextNode();
                    if (!node.isNodeType(ItemType.USER.getSystemName())) {
                        continue;
                    }
                    users.add(newUserInstance(node));
                }
                return users;
            }

            @Override
            public String toString() {
                return "get all users";
            }

        });
    }

    public User createUser(final String name, final String pw) {
        validateUsername(name);
        return MgnlContext.doInSystemContext(new SilentSessionOp<MgnlUser>(getRepositoryName()) {

            @Override
            public MgnlUser doExec(Session session) throws RepositoryException {
                Node userNode = session.getNode("/").addNode(name,ItemType.USER.getSystemName());
                userNode.setProperty("name", name);
                setPasswordProperty(userNode, pw);
                userNode.setProperty("language", "en");

                final String handle = userNode.getPath();
                final Node acls = userNode.addNode(NODE_ACLUSERS, ItemType.CONTENTNODE.getSystemName());
                // read only access to the node itself
                Node acl = acls.addNode(Path.getUniqueLabel(session, acls.getPath(), "0"), ItemType.CONTENTNODE.getSystemName());
                acl.setProperty("path", handle);
                acl.setProperty("permissions", new Long(Permission.READ));
                // those who had access to their nodes should get access to their own props
                addWrite(handle, PROPERTY_EMAIL, acls);
                addWrite(handle, PROPERTY_LANGUAGE, acls);
                addWrite(handle, PROPERTY_LASTACCESS, acls);
                addWrite(handle, PROPERTY_PASSWORD, acls);
                addWrite(handle, PROPERTY_TITLE, acls);
                // and of course the meta data
                addWrite(handle, MetaData.DEFAULT_META_NODE, acls);
                session.save();
                return new MgnlUser(userNode.getName(), getRealmName(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_MAP);
            }

            @Override
            public String toString() {
                return "create user " + name;
            }
        });
    }

    public User changePassword(final User user, final String newPassword) {
        return MgnlContext.doInSystemContext(new SilentSessionOp<User>(getRepositoryName()) {

            @Override
            public User doExec(Session session) throws RepositoryException {
                Node userNode = session.getNode("/" + realmName + "/" + user.getName());
                setPasswordProperty(userNode, newPassword);

                session.save();
                return newUserInstance(userNode);
            }

            @Override
            public String toString() {
                return "change password of user " + user.getName();
            }
        });
    }

    /**
     * @deprecated since 5.0 use {@link #setPasswordProperty(Node, String)} instead
     */
    @Deprecated
    protected void setPasswordProperty(Content userNode, String clearPassword) throws RepositoryException {
        setPasswordProperty(userNode.getJCRNode(), clearPassword);
    }


    protected void setPasswordProperty(Node userNode, String clearPassword) throws RepositoryException {
        userNode.setProperty(PROPERTY_PASSWORD, encodePassword(clearPassword));
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
     * Return the HierarchyManager for the user workspace (through the system context).
     */
    protected HierarchyManager getHierarchyManager() {
        return MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.USERS);
    }

    /**
     * @deprecated since 4.3.1 - use {@link #newUserInstance(javax.jcr.Node)}
     */
    @Deprecated
    protected MgnlUser userInstance(Content node) {
        try {
            return (MgnlUser) newUserInstance(node.getJCRNode());
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Creates a {@link MgnlUser} out of a jcr node. Can be overridden in order to provide a different implementation.
     * @since 4.3.1
     * @deprecated since 5.0 use newUSerInstance(javax.jcr.Node) instead
     */
    @Deprecated
    protected User newUserInstance(Content node) {
        try {
            return newUserInstance(node.getJCRNode());
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private Node addWrite(String parentPath, String property, Node acls) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Node acl = acls.addNode(Path.getUniqueLabel(acls.getSession(), acls.getPath(), "0"), ItemType.CONTENTNODE.getSystemName());
        acl.setProperty("path", parentPath + "/" + property);
        acl.setProperty("permissions", new Long(Permission.ALL));
        return acl;
    }

    public void updateLastAccessTimestamp(final User user) throws UnsupportedOperationException {
        MgnlContext.doInSystemContext(new SilentSessionOp<Void>(getRepositoryName()) {

            @Override
            public Void doExec(Session session) throws RepositoryException {
                String path = ((MgnlUser) user).getPath();
                log.info("update access timestamp for {}", user.getName());
                try {
                    Node userNode = session.getNode(path);
                    NodeDataUtil.updateOrCreate(userNode, "lastaccess", new GregorianCalendar());
                    session.save();
                }
                catch (RepositoryException e) {
                    session.refresh(false);
                }
                return null;
            }
            @Override
            public String toString() {
                return "update user "+user.getName()+" last access time stamp";
            }
        });
    }

    protected User newUserInstance(Node privilegedUserNode) throws ValueFormatException, PathNotFoundException, RepositoryException {
        if (privilegedUserNode == null) {
            return null;
        }
        Set<String> roles = JCRUtil.collectUniquePropertyNames(privilegedUserNode, "roles", ContentRepository.USER_ROLES, false);
        Set<String> groups = JCRUtil.collectUniquePropertyNames(privilegedUserNode, "groups", ContentRepository.USER_GROUPS, false);

        Map<String, String> properties = new HashMap<String, String>();
        for (PropertyIterator iter = privilegedUserNode.getProperties(); iter.hasNext(); ) {
            Property prop = iter.nextProperty();
            if (prop.getName().startsWith("jcr:") || prop.getName().startsWith("mgnl:")) {
                // skip special props
                continue;
            }
            //TODO: should we check and skip binary props in case someone adds image to the user?
            properties.put(prop.getName(), prop.getString());
        }

        MgnlUser user = new MgnlUser(privilegedUserNode.getName(), getRealmName(), groups, roles, properties);
        // keep just a token to user, not the whole node
        // TODO: would it be better to keep around UUID?
        user.setPath(privilegedUserNode.getPath());

        return user;
    }

    @Override
    protected String getRepositoryName() {
        return ContentRepository.USERS;
    }

    /**
     * Sets access control list from a list of roles under the provided content object.
     */
    public Map<String, ACL> getACLs(final User user) {
        if (!(user instanceof MgnlUser)) {
            return null;
        }
        return super.getACLs(user.getName());
    }
}
