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
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.NodeDataUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;


/**
 * This class wrapps a user content object to provide some nice methods
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class MgnlUser implements User, Serializable {

    private static final long serialVersionUID = 222L;

    /**
     * instead of defining each field transient, we explicitly says what needs to be
     * serialized
     * */
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("uuid", String.class)
    };

    private static final Logger log = LoggerFactory.getLogger(MgnlUser.class);

    /**
     * Under this subnodes the assigned roles are saved
     */
    private static final String NODE_ROLES = "roles"; //$NON-NLS-1$

    private static final String NODE_GROUPS = "groups"; //$NON-NLS-1$

    private Content userNode;

    /**
     * Used to refetch the user node after deserializing
     */
    private String uuid;

    /**
     * @param userNode the Content object representing this user
     */
    protected MgnlUser(Content userNode) {
        init(userNode);
    }

    protected void init(Content userNode) {
        this.userNode = userNode;
        this.uuid = userNode.getUUID();
    }

    /**
     * Reinitialize itself with the partial deserialized data
     * */
    private void reInitialize() {
        try {
            HierarchyManager usersHm = MgnlSecurityUtil.getSystemHierarchyManager(ContentRepository.USERS);
            init(usersHm.getContentByUUID(uuid));
        } catch (RepositoryException re) {
            log.error("Failed to load MgnlUser from persistent storage", re);
        }
    }

    /**
     * Is this user in a specified role?
     * @param groupName the name of the role
     * @return true if in role
     */
    public boolean inGroup(String groupName) {
        return this.hasAny(groupName, NODE_GROUPS);
    }

    /**
     * Remove a group. Implementation is optional
     * @param groupName
     */
    public void removeGroup(String groupName) throws UnsupportedOperationException {
        this.remove(groupName, NODE_GROUPS);
    }

    /**
     * Adds this user to a group. Implementation is optional
     * @param groupName
     */
    public void addGroup(String groupName) throws UnsupportedOperationException {
        this.add(groupName, NODE_GROUPS);
    }

    public boolean isEnabled() {
        return NodeDataUtil.getBoolean(getUserNode(), "enabled", true);
    }

    public void setEnabled(boolean enabled) {
        try {
            NodeDataUtil.getOrCreateAndSet(getUserNode(), "enabled", enabled);
            getUserNode().save();
        } catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    /**
     * Is this user in a specified role?
     * @param roleName the name of the role
     * @return true if in role
     */
    public boolean hasRole(String roleName) {
        return this.hasAny(roleName, NODE_ROLES);
    }

    public void removeRole(String roleName) {
        this.remove(roleName, NODE_ROLES);
    }

    /**
     * Adds a role to this user
     * @param roleName the name of the role
     */
    public void addRole(String roleName) {
        this.add(roleName, NODE_ROLES);
    }

    /**
     * checks is any object exist with the given name under this node
     * @param name
     * @param nodeName
     */
    private boolean hasAny(String name, String nodeName) {
        try {
            HierarchyManager hm;
            if (StringUtils.equalsIgnoreCase(nodeName, NODE_ROLES)) {
                hm = MgnlSecurityUtil.getSystemHierarchyManager(ContentRepository.USER_ROLES);
            }
            else {
                hm = MgnlSecurityUtil.getSystemHierarchyManager(ContentRepository.USER_GROUPS);
            }

            Content node = this.getUserNode().getContent(nodeName);
            for (Iterator iter = node.getNodeDataCollection().iterator(); iter.hasNext();) {
                NodeData nodeData = (NodeData) iter.next();
                // check for the existence of this ID
                try {
                    if (hm.getContentByUUID(nodeData.getString()).getName().equalsIgnoreCase(name)) {
                        return true;
                    }
                }
                catch (ItemNotFoundException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Role [ " + name + " ] does not exist in the ROLES repository");
                    }
                }
                catch (IllegalArgumentException e) {
                    if (log.isDebugEnabled()) {
                        log.debug(nodeData.getHandle() + " has invalid value");
                    }
                }
            }
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
        }
        return false;
    }

    /**
     * removed node
     * @param name
     * @param nodeName
     */
    private void remove(String name, String nodeName) {
        try {
            HierarchyManager hm;
            if (StringUtils.equalsIgnoreCase(nodeName, NODE_ROLES)) {
                hm = MgnlSecurityUtil.getContextHierarchyManager(ContentRepository.USER_ROLES);
            }
            else {
                hm = MgnlSecurityUtil.getContextHierarchyManager(ContentRepository.USER_GROUPS);
            }
            Content node = this.getUserNode().getContent(nodeName);

            for (Iterator iter = node.getNodeDataCollection().iterator(); iter.hasNext();) {
                NodeData nodeData = (NodeData) iter.next();
                // check for the existence of this ID
                try {
                    if (hm.getContentByUUID(nodeData.getString()).getName().equalsIgnoreCase(name)) {
                        nodeData.delete();
                    }
                }
                catch (ItemNotFoundException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Role [ " + name + " ] does not exist in the ROLES repository");
                    }
                }
                catch (IllegalArgumentException e) {
                    if (log.isDebugEnabled()) {
                        log.debug(nodeData.getHandle() + " has invalid value");
                    }
                }
            }
            this.getUserNode().save();
        }
        catch (RepositoryException e) {
            log.error("failed to remove " + name + " from user [" + this.getName() + "]", e);
        }
    }

    /**
     * adds a new node under specified node collection
     */
    private void add(String name, String nodeName) {
        try {
            HierarchyManager hm;
            if (StringUtils.equalsIgnoreCase(nodeName, NODE_ROLES)) {
                hm = MgnlSecurityUtil.getContextHierarchyManager(ContentRepository.USER_ROLES);
            }
            else {
                hm = MgnlSecurityUtil.getContextHierarchyManager(ContentRepository.USER_GROUPS);
            }

            if (!this.hasAny(name, nodeName)) {
               if (!this.getUserNode().hasContent(nodeName)) {
                    this.getUserNode().createContent(nodeName, ItemType.CONTENTNODE);
               }
                Content node = this.getUserNode().getContent(nodeName);
                // add corresponding ID
                try {
                    String value = hm.getContent("/" + name).getUUID(); // assuming that there is a flat hierarchy
                    // used only to get the unique label
                    HierarchyManager usersHM = MgnlSecurityUtil.getSystemHierarchyManager(ContentRepository.USERS);
                    String newName = Path.getUniqueLabel(usersHM, node.getHandle(), "0");
                    node.createNodeData(newName).setValue(value);
                    this.getUserNode().save();
                }
                catch (PathNotFoundException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Role [ " + name + " ] does not exist in the ROLES repository");
                    }
                }
            }
        }
        catch (RepositoryException e) {
            log.error("failed to add " + name + " to user [" + this.getName() + "]", e);
        }
    }

    /**
     * The name of the user
     * @return the name of the user
     */
    public String getName() {
        return this.getUserNode().getName();
    }

    /**
     * get user password
     * @return password string
     */
    public String getPassword() {
        final String encodedPassword = this.getUserNode().getNodeData("pswd").getString().trim();
        return decodePassword(encodedPassword);
    }

    protected String decodePassword(String encodedPassword) {
        return new String(Base64.decodeBase64(encodedPassword.getBytes()));
    }

    /**
     * the language of the current user
     */
    public String getLanguage() {
        return this.getUserNode().getNodeData("language").getString(); //$NON-NLS-1$
    }

    public String getProperty(String propertyName) {
        return NodeDataUtil.getString(getUserNode(), propertyName, null);
    }

    public void setProperty(String propertyName, String value) {
        try {
            NodeDataUtil.getOrCreateAndSet(getUserNode(), propertyName, value);
            getUserNode().save();
        } catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public Collection getGroups() {
        return MgnlSecurityUtil.collectPropertyNames(getUserNode(), "groups", ContentRepository.USER_GROUPS, false);
    }

    public Collection getAllGroups() {
        final Set allGroups = new TreeSet(String.CASE_INSENSITIVE_ORDER);
        try {
            // add the user's direct groups
            final Collection groups = getGroups();
            allGroups.addAll(groups);

            // add all groups from direct groups
            final GroupManager gm = SecuritySupport.Factory.getInstance().getGroupManager();
            final Iterator it = groups.iterator();
            while (it.hasNext()) {
                final String groupName = (String) it.next();
                final Group g = gm.getGroup(groupName);
                allGroups.addAll(g.getAllGroups());
            }

            return allGroups;
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public Collection getRoles() {
        return MgnlSecurityUtil.collectPropertyNames(getUserNode(), "roles", ContentRepository.USER_ROLES, false);
    }

    public Collection getAllRoles() {
        final Set allRoles = new TreeSet(String.CASE_INSENSITIVE_ORDER);
        try {
            // add the user's direct roles
            allRoles.addAll(getRoles());

            // add roles from all groups
            final GroupManager gm = SecuritySupport.Factory.getInstance().getGroupManager();
            final Collection allGroups = getAllGroups();
            final Iterator it = allGroups.iterator();
            while (it.hasNext()) {
                final String groupName = (String) it.next();
                final Group g = gm.getGroup(groupName);
                allRoles.addAll(g.getRoles());
            }

            return allRoles;
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    /**
     * Update the "last access" timestamp.
     */
    public void setLastAccess() {

        NodeData lastaccess;
        try {
            lastaccess = NodeDataUtil.getOrCreate(this.getUserNode(), "lastaccess", PropertyType.DATE);
            synchronized (lastaccess) {
                lastaccess.setValue(new GregorianCalendar());
                lastaccess.save();
            }
        }
        catch (RepositoryException e) {
            log.debug("Unable to set the last access date due to a " + e.getClass().getName() + " - " + e.getMessage(), e);
        }

    }

    public Content getUserNode() {
        if (null == userNode) {
            reInitialize();
        }
        return userNode;
    }
}