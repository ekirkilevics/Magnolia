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
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.SystemContentWrapper;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TreeSet;


/**
 * This class wraps a user content object.
 * @version $Revision$ ($Author$)
 */
public class MgnlUser extends AbstractUser implements Serializable {

    private static final long serialVersionUID = 222L;

    private static final Logger log = LoggerFactory.getLogger(MgnlUser.class);

    /**
     * Under this subnodes the assigned roles are saved.
     */
    private static final String NODE_ROLES = "roles"; //$NON-NLS-1$

    private static final String NODE_GROUPS = "groups"; //$NON-NLS-1$

    /**
     * Used to to synchronize write operations in {@link MgnlUser#setLastAccess().
     */
    private static final Object mutex = new Object();

    // serialized
    private final SystemContentWrapper userNode;

    /**
     * @param userNode the Content object representing this user
     */
    protected MgnlUser(Content userNode) {
        this.userNode = new SystemContentWrapper(userNode);
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

    public void addRole(String roleName) {
        this.add(roleName, NODE_ROLES);
    }

    // TODO this are the same methods as in {@link MgnlGroup}. A rewrite is needed.

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
            for (NodeData nodeData : node.getNodeDataCollection()) {
                // check for the existence of this ID
                try {
                    if (hm.getContentByUUID(nodeData.getString()).getName().equalsIgnoreCase(name)) {
                        return true;
                    }
                }
                catch (ItemNotFoundException e) {
                    log.debug("Role [{}] does not exist in the ROLES repository", name);
                }
                catch (IllegalArgumentException e) {
                    log.debug("{} has invalid value", nodeData.getHandle());
                }
            }
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
        }
        return false;
    }

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

            for (NodeData nodeData : node.getNodeDataCollection()) {
                // check for the existence of this ID
                try {
                    if (hm.getContentByUUID(nodeData.getString()).getName().equalsIgnoreCase(name)) {
                        nodeData.delete();
                    }
                }
                catch (ItemNotFoundException e) {
                    log.debug("Role [{}] does not exist in the ROLES repository", name);
                }
                catch (IllegalArgumentException e) {
                    log.debug("{} has invalid value", nodeData.getHandle());
                }
            }
            this.getUserNode().save();
        }
        catch (RepositoryException e) {
            log.error("failed to remove " + name + " from user [" + this.getName() + "]", e);
        }
    }

    private void add(String name, String nodeName) {
        try {
            final String hmName;
            if (StringUtils.equalsIgnoreCase(nodeName, NODE_ROLES)) {
                hmName = ContentRepository.USER_ROLES;
            }
            else {
                hmName = ContentRepository.USER_GROUPS;
            }
            final HierarchyManager hm = MgnlSecurityUtil.getContextHierarchyManager(hmName);

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
                    log.debug("[{}] does not exist in the {} repository", name, hmName);
                }
            }
        }
        catch (RepositoryException e) {
            log.error("failed to add " + name + " to user [" + this.getName() + "]", e);
        }
    }

    public String getName() {
        return this.getUserNode().getName();
    }

    public String getPassword() {
        final String encodedPassword = this.getUserNode().getNodeData("pswd").getString().trim();
        return decodePassword(encodedPassword);
    }

    protected String decodePassword(String encodedPassword) {
        return new String(Base64.decodeBase64(encodedPassword.getBytes()));
    }

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

    public Collection<String> getGroups() {
        return MgnlSecurityUtil.collectPropertyNames(getUserNode(), "groups", ContentRepository.USER_GROUPS, false);
    }

    public Collection<String> getAllGroups() {
        final Set<String> allGroups = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        try {
            // add the user's direct groups
            final Collection<String> groups = getGroups();
            allGroups.addAll(groups);

            // add all groups from direct groups
            final GroupManager gm = SecuritySupport.Factory.getInstance().getGroupManager();
            for (String groupName : groups) {
                final Group g = gm.getGroup(groupName);
                allGroups.addAll(g.getAllGroups());
            }

            return allGroups;
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public Collection<String> getRoles() {
        return MgnlSecurityUtil.collectPropertyNames(getUserNode(), "roles", ContentRepository.USER_ROLES, false);
    }

    public Collection<String> getAllRoles() {
        final Set<String> allRoles = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        try {
            // add the user's direct roles
            allRoles.addAll(getRoles());

            // add roles from all groups
            final GroupManager gm = SecuritySupport.Factory.getInstance().getGroupManager();
            final Collection<String> allGroups = getAllGroups();
            for (String groupName : allGroups) {
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
        Exception finalException = null;
        boolean success = false;
        // try three times to save the lastaccess property
        for(int i= 1; !success && i <=3; i++){
            finalException = null;
            try {
                // synchronize on a static mutex
                synchronized (mutex) {
                    // refresh the session on retries
                    if(i>1){
                        getUserNode().refresh(false);
                    }
                    lastaccess = NodeDataUtil.getOrCreate(this.getUserNode(), "lastaccess", PropertyType.DATE);
                    lastaccess.setValue(new GregorianCalendar());
                    getUserNode().save();
                    success = true;
                }
            }
            catch (RepositoryException e) {
                finalException = e;
                log.debug("Unable to set the last access", e);
            }
        }
        if(finalException != null){
            log.warn("Unable to set the last access date due to a " + ExceptionUtils.getMessage(finalException));
        }
    }

    public Content getUserNode() {
        return userNode;
    }
}
