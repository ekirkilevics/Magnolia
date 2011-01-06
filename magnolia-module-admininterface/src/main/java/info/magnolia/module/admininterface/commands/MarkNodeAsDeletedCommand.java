/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.admininterface.commands;

import java.util.Calendar;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.commands.BaseRepositoryCommand;


public class MarkNodeAsDeletedCommand extends BaseRepositoryCommand {

    public static final String DELETED_NODE_TEMPLATE = "mgnlDeleted";

    public static final String DELETED_NODE_DELETED_BY = "mgnl:deletedBy";
    public static final String DELETED_NODE_DELETED_ON = "mgnl:deletedOn";

    private static final String DELETED_NODE_PROP_NAME = "deleteNode";

    private boolean versionManually = true;

    @Override
    public boolean execute(Context context) throws Exception {

        Content parentNode = getNode(context);
        Content node = parentNode.getContent((String) context.get(DELETED_NODE_PROP_NAME));
        preDeleteNode(node, context);

        return true;
    }

    private void preDeleteNode(Content node, Context context) throws RepositoryException, AccessDeniedException {
        // TODO: versioning might be "unsupported" do we still purge in such case?
        version(node, context);
        synchronized (ExclusiveWrite.getInstance()) {
            markAsDeleted(node);
            purgeContent(node);
            storeDeletionInfo(node, context);
            // save changes before progressing on sub node - means we can't roll back, but session doesn't grow out of limits
            node.save();
        }
        for(Content childPage : node.getChildren(ItemType.CONTENT)) {
            preDeleteNode(childPage, context);
        }
    }

    private void storeDeletionInfo(Content node, Context context) throws AccessDeniedException, PathNotFoundException, RepositoryException {
        node.setNodeData(DELETED_NODE_DELETED_BY, MgnlContext.getUser().getName());
        node.setNodeData(DELETED_NODE_DELETED_ON, Calendar.getInstance());
        String comment = (String) context.get("comment");
        if (comment == null) {
            comment = MessagesManager.get("versions.comment.restore");
        }
        node.getMetaData().setProperty(Context.ATTRIBUTE_COMMENT, comment);
    }

    private void version(Content node, Context context) throws UnsupportedRepositoryOperationException, RepositoryException {
        if (versionManually) {
            synchronized (ExclusiveWrite.getInstance()) {
                String comment = (String) context.get("comment");
                if (comment == null) {
                    comment = MessagesManager.get("versions.comment.deleted");
                }
                node.getMetaData().setProperty(Context.ATTRIBUTE_COMMENT, comment);
                node.save();
            }
            node.addVersion();
        }
    }

    protected void markAsDeleted(Content node) throws RepositoryException, AccessDeniedException {
        // add mixin
        node.addMixin(ItemType.DELETED_NODE_MIXIN);
        // change template
        MetaData metadata = node.getMetaData();
        metadata.setTemplate(DELETED_NODE_TEMPLATE);
        if (metadata.getActivationStatus() != MetaData.ACTIVATION_STATUS_NOT_ACTIVATED) {
            metadata.setModificationDate();
        }
    }

    protected void purgeContent(Content node) throws RepositoryException {
        // delete paragraphs & collections
        for (Content child : node.getChildren(ItemType.CONTENTNODE.getSystemName())) {
            child.delete();
        }
        // delete properties (incl title ??)
        for (NodeData prop : node.getNodeDataCollection()) {
            prop.delete();
        }
    }

    public boolean isVersionManually() {
        return versionManually;
    }

    public void setVersionManually(boolean versionManually) {
        this.versionManually = versionManually;
    }

}
