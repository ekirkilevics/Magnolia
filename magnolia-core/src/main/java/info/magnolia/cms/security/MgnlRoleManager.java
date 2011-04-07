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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.context.MgnlContext;
import info.magnolia.cms.core.HierarchyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages the users stored in the {@link ContentRepository#USER_ROLES} workspace.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class MgnlRoleManager extends RepositoryBackedSecurityManager implements RoleManager {
    private static final Logger log = LoggerFactory.getLogger(MgnlRoleManager.class);

    /**
     * Do not instantiate it!
     */
    public MgnlRoleManager() {
    }

    public Role getRole(String name) {
        try {
            return newRoleInstance(findPrincipalNode(name, MgnlContext.getJCRSession(getRepositoryName())));
        }
        catch (Exception e) {
            log.debug("can't find role [" + name + "]", e);
            return null;
        }
    }

    public Role createRole(String name) {
        try {
            Content node = getHierarchyManager().createContent("/", name, ItemType.ROLE.getSystemName());
            getHierarchyManager().save();
            return newRoleInstance(node);
        }
        catch (Exception e) {
            log.error("can't create role [" + name + "]", e);
            return null;
        }
    }

    /**
     * @deprecated since 5.0
     */
    @Deprecated
    protected MgnlRole newRoleInstance(Content node) throws RepositoryException {
        return newRoleInstance(node.getJCRNode());
    }

    protected MgnlRole newRoleInstance(Node node) throws RepositoryException {
        return new MgnlRole(node.getName(), node.getIdentifier(), getACLs(node).values());
    }

    protected HierarchyManager getHierarchyManager() {
        return MgnlContext.getHierarchyManager(ContentRepository.USER_ROLES);
    }

    public void removePermission(Role role, String repository, String path, long permission) {
        try {
            Session session = MgnlContext.getJCRSession(ContentRepository.REPOSITORY_USER);
            Node roleNode = session.getNodeByIdentifier(role.getId());
            Node aclNode = getAclNode(roleNode, repository);
            NodeIterator children = aclNode.getNodes();
            while(children.hasNext()) {
                Node child = children.nextNode();
                if (child.getProperty("path").getString().equals(path)) {
                    if (permission == MgnlRole.PERMISSION_ANY
                            || child.getProperty("permissions").getLong() == permission) {
                        child.remove();
                    }
                }
            }
            session.save();
        }
        catch (Exception e) {
            log.error("can't remove permission", e);
        }
    }

    /**
     * Get the ACL node for the current role node.
     * @param roleNode
     */
    private Node getAclNode(Node roleNode, String repository) throws RepositoryException, PathNotFoundException,
    AccessDeniedException {
        Node aclNode;
        if (!roleNode.hasNode("acl_" + repository)) {
            aclNode = roleNode.addNode("acl_" + repository, ItemType.CONTENTNODE.getSystemName());
        }
        else {
            aclNode = roleNode.getNode("acl_" + repository);
        }
        return aclNode;
    }

    /**
     * Does this permission exist?
     */
    private boolean existsPermission(Node aclNode, String path, long permission) throws RepositoryException {
        NodeIterator children = aclNode.getNodes();
        while(children.hasNext()) {
            Node child = children.nextNode();
            if (child.hasProperty("path") && child.getProperty("path").getString().equals(path)) {
                if (permission == MgnlRole.PERMISSION_ANY
                        || child.getProperty("permissions").getLong() == permission) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addPermission(Role role, String repository, String path, long permission) {
        try {
            Session session = MgnlContext.getJCRSession(getRepositoryName());
            Node roleNode = session.getNodeByIdentifier(role.getId());
            Node aclNode = getAclNode(roleNode, repository);
            if (!this.existsPermission(aclNode, path, permission)) {
                String nodename = Path.getUniqueLabel(session, aclNode.getPath(), "0");
                Node node = aclNode.addNode(nodename, ItemType.CONTENTNODE.getSystemName());
                node.setProperty("path", path);
                node.setProperty("permissions", permission);
                session.save();
            }
        }
        catch (Exception e) {
            log.error("can't add permission", e);
        }
    }

    @Override
    protected Node findPrincipalNode(String principalName, Session session) throws RepositoryException {
        return session.getNode("/" + principalName);
    }

    @Override
    protected String getRepositoryName() {
        return ContentRepository.USER_ROLES;
    }

    public String getRoleNameById(String string) {
        return getResourceName(string);
    }

}
