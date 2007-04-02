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
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.api.HierarchyManager;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wraps a role jcr-node.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class MgnlRole implements Role {

    public static Logger log = LoggerFactory.getLogger(MgnlRole.class);

    /**
     * Add or remove any permission
     */
    public static long PERMISSION_ANY = -1;

    /**
     * the content object
     */
    private Content roleNode;

    /**
     * @param roleNode the Content object representing this role
     */
    protected MgnlRole(Content roleNode) {
        super();
        this.roleNode = roleNode;
    }

    public String getName() {
        return roleNode.getName();
    }

    public void addPermission(String repository, String path, long permission) {
        try {
            Content aclNode = getAclNode(repository);
            if (!this.existsPermission(aclNode, path, permission)) {
                HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USER_ROLES);
                String nodename = Path.getUniqueLabel(hm, aclNode.getHandle(), "0");
                Content node = aclNode.createContent(nodename, ItemType.CONTENTNODE);
                node.createNodeData("path").setValue(path);
                node.createNodeData("permissions").setValue(String.valueOf(permission));
                roleNode.save();
            }
        }
        catch (Exception e) {
            log.error("can't add permission", e);
        }
    }

    public void removePermission(String repository, String path) {
        this.removePermission(repository, path, MgnlRole.PERMISSION_ANY);
    }

    public void removePermission(String repository, String path, long permission) {
        try {
            Content aclNode = getAclNode(repository);
            Collection children = aclNode.getChildren();
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                Content child = (Content) iter.next();
                if (child.getNodeData("path").getString().equals("path")) {
                    if (permission == MgnlRole.PERMISSION_ANY
                        || child.getNodeData("permissions").getString().equals(String.valueOf(permission))) {
                        child.delete();
                    }
                }
            }
            roleNode.save();
        }
        catch (Exception e) {
            log.error("can't remove permission", e);
        }
    }

    /**
     * Get the acl node for the current role node
     * @param repository
     * @return
     * @throws RepositoryException
     * @throws PathNotFoundException
     * @throws AccessDeniedException
     */
    private Content getAclNode(String repository) throws RepositoryException, PathNotFoundException,
        AccessDeniedException {
        Content aclNode;
        if (!roleNode.hasContent("acl_" + repository)) {
            aclNode = roleNode.createContent("acl_" + repository, ItemType.CONTENTNODE);
        }
        else {
            aclNode = roleNode.getContent("acl_" + repository);
        }
        return aclNode;
    }

    /**
     * Does this permission exist?
     * @param aclNode
     * @param path
     * @param permission
     */
    private boolean existsPermission(Content aclNode, String path, long permission) {
        Collection children = aclNode.getChildren();
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            Content child = (Content) iter.next();
            if (child.getNodeData("path").getString().equals(path)) {
                if (permission == MgnlRole.PERMISSION_ANY
                    || child.getNodeData("permission").getString().equals(String.valueOf(permission))) {
                    return true;
                }
            }
        }
        return false;
    }
}
