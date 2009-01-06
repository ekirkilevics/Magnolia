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
package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.cms.gui.dialog.DialogControlImpl;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.SaveHandler;

import java.util.Iterator;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class UserEditDialog extends ConfiguredDialog {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    protected static Logger log = LoggerFactory.getLogger(UserEditDialog.class);

    protected static final String NODE_ACLUSERS = "acl_users"; //$NON-NLS-1$

    protected static final String NODE_ACLROLES = "acl_userroles"; //$NON-NLS-1$

    protected static final String NODE_ACLCONFIG = "acl_config"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#getRepository()
     */
    public String getRepository() {
        String repository = super.getRepository();
        if (repository == null) {
            repository = ContentRepository.USERS;
        }
        return repository;
    }

    /**
     * @param name
     * @param request
     * @param response
     * @param configNode
     */
    public UserEditDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);
    }

    /**
     * @see info.magnolia.module.admininterface.DialogMVCHandler#configureSaveHandler(info.magnolia.module.admininterface.SaveHandler)
     */
    protected void configureSaveHandler(SaveHandler save) {
        super.configureSaveHandler(save);
        save.setPath(path);
    }

    /**
     * Is called during showDialog(). Here can you create/ add controls for the dialog.
     * @param configNode
     * @param storageNode
     * @throws javax.jcr.RepositoryException
     */
    protected Dialog createDialog(Content configNode, Content storageNode) throws RepositoryException {
        Dialog dialog = super.createDialog(configNode, storageNode);
        // don't do anything if command is "save"
        if (this.getCommand().equalsIgnoreCase(COMMAND_SAVE)) {
            return dialog;
        }

        // replace UUID with Path for groups and roles
        DialogControlImpl control = dialog.getSub("groups");
        // it is ok to use system context here as long as it is used only to retrieve UUIDs of nodes we are interested in
        HierarchyManager groupsHM = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.USER_GROUPS);
        List values = control.getValues();
        for (int index = 0; index < values.size(); index++) {
            // replace uuid with path
            String uuid = (String) values.get(index);
            if (StringUtils.isEmpty(uuid)) {
                continue;
            }
            try {
                values.set(index, groupsHM.getContentByUUID(uuid).getHandle());
            }
            catch (ItemNotFoundException e) {
                // remove invalid ID
                values.remove(index);
            }
        }

        control = dialog.getSub("roles");
        // it is ok to use system context here as long as it is used only to retrieve UUIDs of nodes we are interested in
        HierarchyManager rolesHM = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.USER_ROLES);
        values = control.getValues();
        for (int index = 0; index < values.size(); index++) {
            // replace uuid with path
            String uuid = (String) values.get(index);
            if (StringUtils.isEmpty(uuid)) {
                continue;
            }
            try {
                values.set(index, rolesHM.getContentByUUID(uuid).getHandle());
            }
            catch (ItemNotFoundException e) {
                // remove invalid ID
                values.remove(index);
            }
        }
        return dialog;
    }

    /**
     * Write ACL entries under the given user node
     * @param node under which ACL for all workspaces needs to be created
     */
    protected void writeACL(Content node) throws RepositoryException {
        // remove existing
        Iterator repositoryNames = ContentRepository.getAllRepositoryNames();
        while (repositoryNames.hasNext()) {
            String repository = (String) repositoryNames.next();
            try {
                node.delete("acl_" + repository); //$NON-NLS-1$
            }
            catch (RepositoryException re) {
                // new user
            }
        }

        // rewrite
        Content aclUsers;

        aclUsers = node.createContent(NODE_ACLUSERS, ItemType.CONTENTNODE);

        node.createContent(NODE_ACLROLES, ItemType.CONTENTNODE);
        node.createContent(NODE_ACLCONFIG, ItemType.CONTENTNODE);

        // give user permission to edit himself
        Content u3 = aclUsers.createContent("0", ItemType.CONTENTNODE); //$NON-NLS-1$
        u3.createNodeData("path").setValue(node.getHandle() + "/*"); //$NON-NLS-1$ //$NON-NLS-2$
        u3.createNodeData("permissions").setValue(Permission.ALL); //$NON-NLS-1$
        // give user permission to read himself
        Content u4 = aclUsers.createContent("00", ItemType.CONTENTNODE); //$NON-NLS-1$
        u4.createNodeData("path").setValue(node.getHandle()); //$NON-NLS-1$
        u4.createNodeData("permissions").setValue(Permission.READ); //$NON-NLS-1$
    }

    protected boolean onPostSave(SaveHandler saveControl) {

        Content node = this.getStorageNode();

        HierarchyManager groupsHM = MgnlContext.getHierarchyManager(
            ContentRepository.USER_GROUPS);
        HierarchyManager rolesHM = MgnlContext.getHierarchyManager(
            ContentRepository.USER_ROLES);

        try {
            this.writeRolesOrGroups(groupsHM, node, "groups");
            this.writeRolesOrGroups(rolesHM, node, "roles");
            this.writeACL(node);
            node.save();
            return true;
        } catch (RepositoryException re) {
            log.error("Failed to update user, reverting all transient modifications made for this node", re);
            try {
                node.refresh(false);
            } catch (RepositoryException e) {
                log.error("Failed to revert transient modifications", re);
            }
        }
        return false;
    }

    private void writeRolesOrGroups(HierarchyManager hm, Content parentNode, String nodeName)
            throws RepositoryException {
        try {
            Content groupOrRoleNode = parentNode.getContent(nodeName);
            // remove existing roles, leave the node as is
            Iterator existingNodes = groupOrRoleNode.getNodeDataCollection().iterator();
            while (existingNodes.hasNext()) {
                ((NodeData) existingNodes.next()).delete();
            }
            List values = getDialog().getSub(nodeName).getValues();
            String path = null;
            for (int index = 0; index < values.size(); index++) {
                try {
                    path = (String) values.get(index);
                    if (StringUtils.isNotEmpty(path)) {
                        groupOrRoleNode.createNodeData(Integer.toString(index)).setValue(hm.getContent(path).getUUID());
                    }
                } catch(AccessDeniedException e) {
                    String user = MgnlContext.getUser().getName();
                    log.warn("User {} tried to assign {} {} to {} without having privileges to do so.", new Object[] {user, nodeName.substring(0, nodeName.length() - 1), path, (parentNode.getName() == user ? "self" : parentNode.getName())});
                }
            }
        } catch (PathNotFoundException e) {
            // this might happen if all groups are deleted via dialog
        }
    }
}
