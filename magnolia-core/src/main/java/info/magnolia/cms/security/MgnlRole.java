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
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.HierarchyManager;

import java.util.Collection;

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
    private static final Logger log = LoggerFactory.getLogger(MgnlRole.class);

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
                HierarchyManager hm = MgnlSecurityUtil.getSystemHierarchyManager(ContentRepository.USER_ROLES);
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
            Collection<Content> children = aclNode.getChildren();
            for (Content child : children) {
                if (child.getNodeData("path").getString().equals(path)) {    
                    if (permission == MgnlRole.PERMISSION_ANY
                        || child.getNodeData("permissions").getLong() == permission) {
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
        Collection<Content> children = aclNode.getChildren();
        for (Content child : children) {
            if (child.getNodeData("path").getString().equals(path)) {
                if (permission == MgnlRole.PERMISSION_ANY
                    || child.getNodeData("permissions").getLong() == permission) {
                    return true;
                }
            }
        }
        return false;
    }


}
