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
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.NodeDataUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.Collection;


/**
 * A group stored in the {@link ContentRepository#USER_GROUPS} workspace.
 * @author Sameer Charles $Id$
 */
public class MgnlGroup implements Group {
    private static final Logger log = LoggerFactory.getLogger(MgnlGroup.class);

    /**
     * Name of the subnode under which the assigned roles get saved.
     */
    private static final String NODE_ROLES = "roles"; //$NON-NLS-1$

    /**
     * Name of the subnode under which the assigned groups get saved.
     */
    private static final String NODE_GROUPS = "groups"; //$NON-NLS-1$

    /**
     * The node in the workspace.
     */
    private final Content groupNode;

    /**
     * @param groupNode the Content object representing this group
     */
    protected MgnlGroup(Content groupNode) {
        this.groupNode = groupNode;
    }

    public String getName() {
        return getGroupNode().getName();
    }

    public void addRole(String roleName) throws UnsupportedOperationException, AccessDeniedException {
        this.add(roleName, NODE_ROLES);
    }

    /**
     * Add a subgroup to this group.
     */
    public void addGroup(String groupName) throws UnsupportedOperationException, AccessDeniedException {
        this.add(groupName, NODE_GROUPS);
    }

    public void removeRole(String roleName) throws UnsupportedOperationException, AccessDeniedException {
        this.remove(roleName, NODE_ROLES);
    }

    /**
     * Remove a subgroup from this group.
     */
    public void removeGroup(String groupName) throws UnsupportedOperationException, AccessDeniedException {
        this.remove(groupName, NODE_GROUPS);
    }

    public boolean hasRole(String roleName) throws UnsupportedOperationException, AccessDeniedException {
        return this.hasAny(roleName, NODE_ROLES);
    }

    public String getProperty(String propertyName) {
        return NodeDataUtil.getString(getGroupNode(), propertyName, null);
    }

    public void setProperty(String propertyName, String value) {
        try {
            NodeDataUtil.getOrCreateAndSet(getGroupNode(), propertyName, value);
            getGroupNode().save();
        } catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public Collection<String> getRoles() {
        return MgnlSecurityUtil.collectPropertyNames(getGroupNode(), "roles", ContentRepository.USER_ROLES, false);
    }

    public Collection<String> getGroups() {
        return MgnlSecurityUtil.collectPropertyNames(getGroupNode(), "groups", ContentRepository.USER_GROUPS, false);
    }

    public Collection<String> getAllGroups() {
        return MgnlSecurityUtil.collectPropertyNames(getGroupNode(), "groups", ContentRepository.USER_GROUPS, true);
    }

    // TODO the following methods need a complete rewrite. They work on the group or role subnode collection
    // and perform the uuid to group/role name transformation. I rename the parameters to hopefully give a
    // better hint.

    private boolean hasAny(String groupOrRoleName, String collectionName) {
        try {
            final String hmName;
            if (StringUtils.equalsIgnoreCase(collectionName, NODE_ROLES)) {
                hmName = ContentRepository.USER_ROLES;
            }
            else {
                hmName = ContentRepository.USER_GROUPS;
            }
            final HierarchyManager hm = MgnlSecurityUtil.getSystemHierarchyManager(hmName);

            Content node = getGroupNode().getContent(collectionName);
            for (NodeData nodeData : node.getNodeDataCollection()) {
                // check for the existence of this ID
                try {
                    if (hm.getContentByUUID(nodeData.getString()).getName().equalsIgnoreCase(groupOrRoleName)) {
                        return true;
                    }
                }
                catch (ItemNotFoundException e) {
                    log.debug("[{}] does not exist in the {} repository", groupOrRoleName, hmName);
                }
                catch (IllegalArgumentException e) {
                    log.debug(nodeData.getHandle() + " has invalid value");
                }
            }
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
        }
        return false;
    }

    private void remove(String groupOrRoleName, String collectionName) {
        try {
            final String hmName;
            if (StringUtils.equalsIgnoreCase(collectionName, NODE_ROLES)) {
                hmName = ContentRepository.USER_ROLES;
            }
            else {
                hmName = ContentRepository.USER_GROUPS;
            }
            final HierarchyManager hm = MgnlSecurityUtil.getContextHierarchyManager(hmName);
            Content node = getGroupNode().getContent(collectionName);

            for (NodeData nodeData: node.getNodeDataCollection()) {
                // check for the existence of this ID
                try {
                    if (hm.getContentByUUID(nodeData.getString()).getName().equalsIgnoreCase(groupOrRoleName)) {
                        nodeData.delete();
                    }
                }
                catch (ItemNotFoundException e) {
                    log.debug("[{}] does not exist in the {} repository", groupOrRoleName, hmName);
                }
                catch (IllegalArgumentException e) {
                    log.debug(nodeData.getHandle() + " has invalid value");
                }
            }
            getGroupNode().save();
        }
        catch (RepositoryException e) {
            log.error("failed to remove " + groupOrRoleName + " from group [" + this.getName() + "]", e);
        }
    }

    private void add(String groupOrRoleName, String collectionName) {
        try {
            final Content groupNode = getGroupNode();
            final String hmName;
            if (StringUtils.equalsIgnoreCase(collectionName, NODE_ROLES)) {
                hmName = ContentRepository.USER_ROLES;
            } else {
                hmName = ContentRepository.USER_GROUPS;
            }

            final HierarchyManager hm = MgnlSecurityUtil.getContextHierarchyManager(hmName);

            if (!this.hasAny(groupOrRoleName, collectionName)) {
                if (!groupNode.hasContent(collectionName)) {
                    groupNode.createContent(collectionName, ItemType.CONTENTNODE);
                }
                Content node = groupNode.getContent(collectionName);
                // add corresponding ID
                try {
                    String value = hm.getContent("/" + groupOrRoleName).getUUID(); // assuming that there is a flat hierarchy
                    // used only to get the unique label
                    final HierarchyManager sysHM = MgnlSecurityUtil.getSystemHierarchyManager(ContentRepository.USER_GROUPS);
                    final String newName = Path.getUniqueLabel(sysHM, node.getHandle(), "0");
                    node.createNodeData(newName).setValue(value);
                    groupNode.save();
                }
                catch (PathNotFoundException e) {
                    log.debug("[{}] does not exist in the {} repository", groupOrRoleName, hmName);
                }
            }
        }
        catch (RepositoryException e) {
            log.error("failed to add " + groupOrRoleName + " to group [" + this.getName() + "]", e);
        }
    }

    public Content getGroupNode() {
        return groupNode;
    }
}
