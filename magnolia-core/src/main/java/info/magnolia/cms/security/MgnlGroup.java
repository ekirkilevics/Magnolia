/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
import java.util.Iterator;


/**
 * @author Sameer Charles $Id$
 */
public class MgnlGroup implements Group {
    private static final Logger log = LoggerFactory.getLogger(MgnlGroup.class);

    /**
     * Under this subnodes the assigned roles are saved
     */
    private static final String NODE_ROLES = "roles"; //$NON-NLS-1$

    private static final String NODE_GROUPS = "groups"; //$NON-NLS-1$

    /**
     * group node
     */
    private Content groupNode;

    /**
     * @param groupNode the Content object representing this group
     */
    MgnlGroup(Content groupNode) {
        this.groupNode = groupNode;
    }

    /**
     * get name of this node
     * @return group name
     */
    public String getName() {
        return getGroupNode().getName();
    }

    /**
     * add role to this group
     * @param roleName
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if loggen in repository user does not sufficient rights
     */
    public void addRole(String roleName) throws UnsupportedOperationException, AccessDeniedException {
        this.add(roleName, NODE_ROLES);
    }

    /**
     * add subgroup to this group
     * @param groupName
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if loggen in repository user does not sufficient rights
     */
    public void addGroup(String groupName) throws UnsupportedOperationException, AccessDeniedException {
        this.add(groupName, NODE_GROUPS);
    }

    /**
     * remove role from this group
     * @param roleName
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if loggen in repository user does not sufficient rights
     */
    public void removeRole(String roleName) throws UnsupportedOperationException, AccessDeniedException {
        this.remove(roleName, NODE_ROLES);
    }

    /**
     * remove subgroup from this group
     * @param groupName
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if loggen in repository user does not sufficient rights
     */
    public void removeGroup(String groupName) throws UnsupportedOperationException, AccessDeniedException {
        this.remove(groupName, NODE_GROUPS);
    }

    /**
     * returns true if role exist in this group
     * @param roleName
     * @throws UnsupportedOperationException if the implementation does not exist
     * @throws AccessDeniedException if loggen in repository user does not sufficient rights
     */
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

    public Collection getRoles() {
        return MgnlSecurityUtil.collectPropertyNames(getGroupNode(), "roles", ContentRepository.USER_ROLES, false);
    }

    public Collection getGroups() {
        return MgnlSecurityUtil.collectPropertyNames(getGroupNode(), "groups", ContentRepository.USER_GROUPS, false);
    }

    public Collection getAllGroups() {
        return MgnlSecurityUtil.collectPropertyNames(getGroupNode(), "groups", ContentRepository.USER_GROUPS, true);
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

            Content node = getGroupNode().getContent(nodeName);
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
            Content node = getGroupNode().getContent(nodeName);

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
            getGroupNode().save();
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
            final Content groupNode = getGroupNode();
            final String repoId;
            if (StringUtils.equalsIgnoreCase(nodeName, NODE_ROLES)) {
                repoId = ContentRepository.USER_ROLES;
            } else {
                repoId = ContentRepository.USER_GROUPS;
            }

            final HierarchyManager hm = MgnlSecurityUtil.getContextHierarchyManager(repoId);

            if (!this.hasAny(name, nodeName)) {
               if (!groupNode.hasContent(nodeName)) {
                    groupNode.createContent(nodeName, ItemType.CONTENTNODE);
               }
                Content node = groupNode.getContent(nodeName);
                // add corresponding ID
                try {
                    String value = hm.getContent("/" + name).getUUID(); // assuming that there is a flat hierarchy
                    // used only to get the unique label
                    final HierarchyManager sysHM = MgnlSecurityUtil.getSystemHierarchyManager(repoId);
                    final String newName = Path.getUniqueLabel(sysHM, node.getHandle(), "0");
                    node.createNodeData(newName).setValue(value);
                    groupNode.save();
                }
                catch (PathNotFoundException e) {
                    log.debug("[{}] does not exist in the {} repository", name, repoId);
                }
            }
        }
        catch (RepositoryException e) {
            log.error("failed to add " + name + " to user [" + this.getName() + "]", e);
        }
    }

    public Content getGroupNode() {
        return groupNode;
    }
}
