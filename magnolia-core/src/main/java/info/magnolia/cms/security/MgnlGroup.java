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
            HierarchyManager hm;
            if (StringUtils.equalsIgnoreCase(nodeName, NODE_ROLES)) {
                hm = MgnlSecurityUtil.getContextHierarchyManager(ContentRepository.USER_ROLES);
            }
            else {
                hm = MgnlSecurityUtil.getContextHierarchyManager(ContentRepository.USER_GROUPS);
            }

            if (!this.hasAny(name, nodeName)) {
               if (!groupNode.hasContent(nodeName)) {
                    groupNode.createContent(nodeName, ItemType.CONTENTNODE);
               }
                Content node = groupNode.getContent(nodeName);
                // add corresponding ID
                try {
                    String value = hm.getContent("/" + name).getUUID(); // assuming that there is a flat hierarchy
                    // used only to get the unique label
                    HierarchyManager usersHM = MgnlSecurityUtil.getSystemHierarchyManager(ContentRepository.USERS);
                    String newName = Path.getUniqueLabel(usersHM, node.getHandle(), "0");
                    node.createNodeData(newName).setValue(value);
                    groupNode.save();
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

    public Content getGroupNode() {
        return groupNode;
    }
}
