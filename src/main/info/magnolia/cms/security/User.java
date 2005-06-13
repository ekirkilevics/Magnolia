/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.security;

import java.util.Iterator;

import info.magnolia.cms.core.Content;


/**
 * This class wrapps a user content object to provide some nice methods
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class User {

    /**
     * Under this subnodes the assigned roles are saved
     */
    private static final String NODE_ROLES = "roles";

    /**
     * the content object
     */
    private Content userNode;

    /**
     * @param userNode the Content object representing this user
     */
    public User(Content userNode) {
        super();
        this.userNode = userNode;
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
                if (node.getNodeData("path").getString().equals("/" + roleName)) {
                    return true;
                }
            }
            if (rolesNode.hasContent(roleName)) {
                return true;
            }

        }
        catch (Exception e) {
            // nothing
        }

        return false;
    }
}