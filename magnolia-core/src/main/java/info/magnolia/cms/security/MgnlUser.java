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
import info.magnolia.cms.core.Path;

import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class wrapps a user content object to provide some nice methods
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class MgnlUser implements User {

    public static Logger log = LoggerFactory.getLogger(User.class);

    /**
     * Under this subnodes the assigned roles are saved
     */
    private static final String NODE_ROLES = "roles"; //$NON-NLS-1$

    private static final String NODE_GROUPS = "groups"; //$NON-NLS-1$

    /**
     * the content object
     */
    private Content userNode;

    /**
     * @param userNode the Content object representing this user
     */
    protected MgnlUser(Content userNode) {
        this.userNode = userNode;
    }

    /**
     * Is this user in a specified role?
     * @param groupName the name of the role
     * @return true if in role
     */
    public boolean inGroup(String groupName) {
        try {
            Content rolesNode = userNode.getContent(NODE_GROUPS);

            for (Iterator iter = rolesNode.getChildren().iterator(); iter.hasNext();) {
                Content node = (Content) iter.next();
                if (node.getNodeData("path").getString().equals("/" + groupName)) { //$NON-NLS-1$ //$NON-NLS-2$
                    return true;
                }
            }
            if (rolesNode.hasContent(groupName)) {
                return true;
            }

        }
        catch (RepositoryException e) {
            // nothing
        }

        return false;
    }

    /**
     * Is this user in a specified role?
     * @param roleName the name of the role
     * @return true if in role
     */
    public boolean hasRole(String roleName) {
        try {
            Content rolesNode = userNode.getContent(NODE_ROLES);

            for (Iterator iter = rolesNode.getChildren().iterator(); iter.hasNext();) {
                Content node = (Content) iter.next();
                if (node.getNodeData("path").getString().equals("/" + roleName)) { //$NON-NLS-1$ //$NON-NLS-2$
                    return true;
                }
            }
            if (rolesNode.hasContent(roleName)) {
                return true;
            }

        }
        catch (RepositoryException e) {
            // nothing
        }

        return false;
    }

    public void removeRole(String roleName) {
        try {
            Content rolesNode = userNode.getContent(NODE_ROLES);

            for (Iterator iter = rolesNode.getChildren().iterator(); iter.hasNext();) {
                Content node = (Content) iter.next();
                if (node.getNodeData("path").getString().equals("/" + roleName)) { //$NON-NLS-1$ //$NON-NLS-2$
                    node.delete();
                }
            }
            if (rolesNode.hasContent(roleName)) {
                rolesNode.delete(roleName);
            }
            userNode.save();
        }
        catch (RepositoryException e) {
            log.error("can't remove role from user [" + this.getName() + "]", e);
        }
    }

    /**
     * Adds a role to this user
     * @param roleName the name of the role
     */
    public void addRole(String roleName) {
        try {
            if (!this.hasRole(roleName)) {
                Content rolesNode = userNode.getContent(NODE_ROLES);

                // used only to get the unique label
                HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
                if (!rolesNode.hasContent(roleName)) {
                    String nodename = Path.getUniqueLabel(hm, rolesNode.getHandle(), "0");
                    Content node = rolesNode.createContent(nodename, ItemType.CONTENTNODE);
                    node.createNodeData("path").setValue("/" + roleName);
                    userNode.save();
                }
            }
        }
        catch (RepositoryException e) {
            log.error("can't add role to user [" + this.getName() + "]", e);
        }
    }

    /**
     * The name of the user
     * @return the name of the user
     */
    public String getName() {
        return this.userNode.getName();
    }

    /**
     * get user password
     * @return password string
     */
    public String getPassword() {
        String pswd = this.userNode.getNodeData("pswd").getString().trim();
        return new String(Base64.decodeBase64(pswd.getBytes()));
    }

    /**
     * the language of the current user
     */
    public String getLanguage() {
        return userNode.getNodeData("language").getString(); //$NON-NLS-1$
    }
}