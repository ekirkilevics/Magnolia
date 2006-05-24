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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.context.MgnlContext;

import javax.jcr.RepositoryException;
import java.util.Iterator;

/**
 * @author Sameer Charles
 * $Id$
 */
public class MgnlGroup implements Group {

    /**
     * Logger
     * */
    public static Logger log = LoggerFactory.getLogger(MgnlGroup.class);

    /**
     * Under this subnodes the assigned roles are saved
     */
    private static final String NODE_ROLES = "roles"; //$NON-NLS-1$

    /**
     * group node
     * */
    private Content groupNode;

    /**
     * @param groupNode the Content object representing this group
     */
    MgnlGroup(Content groupNode) {
        this.groupNode = groupNode;
    }

    /**
     * get name of this node
     *
     * @return group name
     */
    public String getName() {
        return this.groupNode.getName();
    }

    /**
     * add role to this group
     *
     * @param roleName
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if loggen in repository user does not sufficient rights
     */
    public void addRole(String roleName) throws UnsupportedOperationException, AccessDeniedException {
        try {
            if (!this.hasRole(roleName)) {
                Content rolesNode = this.groupNode.getContent(NODE_ROLES);

                // used only to get the unique label
                HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
                if (!rolesNode.hasContent(roleName)) {
                    String nodename = Path.getUniqueLabel(hm, rolesNode.getHandle(), "0");
                    Content node = rolesNode.createContent(nodename, ItemType.CONTENTNODE);
                    node.createNodeData("path").setValue("/" + roleName);
                    this.groupNode.save();
                }
            }
        }
        catch (RepositoryException e) {
            log.error("can't add role to group [" + this.getName() + "]", e);
        }
    }

    /**
     * remove role from this group
     *
     * @param roleName
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if loggen in repository user does not sufficient rights
     */
    public void removeRole(String roleName) throws UnsupportedOperationException, AccessDeniedException {
        try {
            Content rolesNode = this.groupNode.getContent(NODE_ROLES);

            for (Iterator iter = rolesNode.getChildren().iterator(); iter.hasNext();) {
                Content node = (Content) iter.next();
                if (node.getNodeData("path").getString().equals("/" + roleName)) { //$NON-NLS-1$ //$NON-NLS-2$
                    node.delete();
                }
            }
            if (rolesNode.hasContent(roleName)) {
                rolesNode.delete(roleName);
            }
            this.groupNode.save();
        }
        catch (RepositoryException e) {
            log.error("can't remove role from group [" + this.getName() + "]", e);
        }
    }

    /**
     * returns true if role exist in this group
     *
     * @param roleName
     * @throws UnsupportedOperationException if the implementation does not exist
     * @throws AccessDeniedException if loggen in repository user does not sufficient rights
     */
    public boolean hasRole(String roleName) throws UnsupportedOperationException, AccessDeniedException {
        try {
            Content rolesNode = this.groupNode.getContent(NODE_ROLES);

            for (Iterator iter = rolesNode.getChildren().iterator(); iter.hasNext();) {
                Content node = (Content) iter.next();
                if (node.getNodeData("path").getString().equals("/" + roleName)) { //$NON-NLS-1$ //$NON-NLS-2$
                    return true;
                }
            }
        }
        catch (RepositoryException e) {
            // nothing
        }
        return false;
    }

    /**
     * return the role HierarchyManager
     */
    protected HierarchyManager getHierarchyManager() {
        return MgnlContext.getHierarchyManager(ContentRepository.USER_GROUPS);
    }
}
