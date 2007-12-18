/**
 * This file Copyright (c) 2003-2007 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.security;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.HierarchyManager;
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

    private static final String PASSWORD_PROPERTY = "pswd";

    private String realmName;

    /**
     * Do not instantiate it!
     */
    public MgnlUserManager() {
    }

    // TODO : rename to getRealmName and setRealmName (and make sure Content2Bean still sets realmName using the parent's node name)
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
        if (node == null){
            log.debug("User not found: [{}]", name);
            return null;
        }
        final MgnlUser user = new MgnlUser(node);
        if(!user.getName().equals(ANONYMOUS_USER)){
            user.setLastAccess();
        }
        return user;
    }

    /**
     * Helper method to find a user in a certain realm. Uses JCR Query.
     */
    protected Content findUserNode(String realm, String name) throws RepositoryException {
        String where = "where jcr:path = '/" + realm + "/" + name + "'";
        where += " or jcr:path like '/" + realm + "/%/" + name + "'";

        // the all realm searches the repository
        if(Realm.REALM_ALL.equals(realm)){
            where = "where jcr:path like '%/" + name + "'";
        }

        String statement = "select * from " + ItemType.USER + " " + where ;

        QueryManager qm = getHierarchyManager().getQueryManager();
        Query query = qm.createQuery(statement, Query.SQL);
        Collection users = query.execute().getContent(ItemType.USER.getSystemName());
        if(users.size() == 1){
            return (Content) users.iterator().next();
        }
        else if(users.size() >1){
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
        validateUsername(name);
        try {
            final Content node = createUserNode(name);
            node.createNodeData("name").setValue(name);
            setPasswordProperty(node, pw);
            node.createNodeData("language").setValue("en");
            getHierarchyManager().save();
            return new MgnlUser(node);
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
        } catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    protected void setPasswordProperty(Content userNode, String clearPassword) throws RepositoryException {
        userNode.createNodeData(PASSWORD_PROPERTY).setValue(encodePassword(clearPassword));
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

}
