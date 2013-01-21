/**
 * This file Copyright (c) 2010-2013 Magnolia International
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
package info.magnolia.commands.impl;

import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.exchange.ActivationManagerFactory;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.iterator.FilteringNodeIterator;
import info.magnolia.jcr.iterator.FilteringPropertyIterator;
import info.magnolia.jcr.predicate.JCRMgnlPropertyHidingPredicate;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.objectfactory.Components;

import java.util.Calendar;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

/**
 * Command to mark node as deleted and remove all the non-system content.
 * 
 */
public class MarkNodeAsDeletedCommand extends BaseRepositoryCommand {

    public static final String DELETED_NODE_TEMPLATE = "adminInterface:mgnlDeleted";

    public static final String DELETED_NODE_DELETED_BY = "mgnl:deletedBy";
    public static final String DELETED_NODE_DELETED_ON = "mgnl:deletedOn";

    public static final String DELETED_NODE_PROP_NAME = "deleteNode";

    private boolean versionManually = true;

    private boolean forcePreDelete;

    private VersionManager versionManager;

    @Override
    public boolean execute(Context context) throws Exception {
        versionManager = Components.getSingleton(VersionManager.class);

        final Node parentNode = getJCRNode(context);
        final Node node = parentNode.getNode((String) context.get(DELETED_NODE_PROP_NAME));
        boolean hasActiveSubscriber = false;
        for (Subscriber subscriber : ActivationManagerFactory.getActivationManager().getSubscribers()) {
            if (subscriber.isActive()) {
                hasActiveSubscriber = true;
                break;
            }
        }
        if (hasActiveSubscriber || isForcePreDelete()) {
            preDeleteNode(node, context);
        } else {
            node.remove();
            parentNode.getSession().save();
        }

        return true;
    }

    private void preDeleteNode(Node node, Context context) throws RepositoryException, AccessDeniedException {
        // TODO: versioning might be "unsupported" do we still purge in such case?
        version(node, context);
        synchronized (ExclusiveWrite.getInstance()) {
            markAsDeleted(node);
            purgeContent(node);
            storeDeletionInfo(node, context);
            // save changes before progressing on sub node - means we can't roll back, but session doesn't grow out of limits
            node.getSession().save();
        }
        for (Iterator<Node> iter = new FilteringNodeIterator(node.getNodes(), new NodeTypePredicate(MgnlNodeType.NT_CONTENT, true)); iter.hasNext();) {
            preDeleteNode(iter.next(), context);
        }
    }

    private void storeDeletionInfo(Node node, Context context) throws AccessDeniedException, PathNotFoundException, RepositoryException {
        node.setProperty(DELETED_NODE_DELETED_BY, MgnlContext.getUser().getName());
        node.setProperty(DELETED_NODE_DELETED_ON, Calendar.getInstance());
        String comment = (String) context.get("comment");
        if (comment == null) {
            comment = MessagesManager.get("versions.comment.restore");
        }
        MetaDataUtil.getMetaData(node).setProperty(Context.ATTRIBUTE_COMMENT, comment);
    }

    private void version(Node node, Context context) throws UnsupportedRepositoryOperationException, RepositoryException {
        if (isVersionManually()) {
            synchronized (ExclusiveWrite.getInstance()) {
                String comment = (String) context.get("comment");
                if (comment == null) {
                    comment = MessagesManager.get("versions.comment.deleted");
                }
                MetaDataUtil.getMetaData(node).setProperty(Context.ATTRIBUTE_COMMENT, comment);
                node.getSession().save();
            }
            versionManager.addVersion(node);
        }
    }

    protected void markAsDeleted(Node node) throws RepositoryException, AccessDeniedException {
        // add mixin
        node.addMixin(MgnlNodeType.MIX_DELETED);
        // change template
        MetaData metadata = MetaDataUtil.getMetaData(node);
        metadata.setTemplate(DELETED_NODE_TEMPLATE);
        if (metadata.getActivationStatus() != MetaData.ACTIVATION_STATUS_NOT_ACTIVATED) {
            metadata.setModificationDate();
        }
        MetaDataUtil.updateMetaData(node);
    }

    protected void purgeContent(Node node) throws RepositoryException {
        // delete paragraphs & collections
        for (Iterator<Node> iter = new FilteringNodeIterator(node.getNodes(), new NodeTypePredicate(MgnlNodeType.NT_CONTENTNODE)); iter.hasNext();) {
            iter.next().remove();
        }
        // delete properties (incl title ??)
        for (Iterator<Property> iter = new FilteringPropertyIterator(node.getProperties(), new JCRMgnlPropertyHidingPredicate()); iter.hasNext();) {
            Property property = iter.next();
            property.remove();
        }
    }

    public boolean isVersionManually() {
        return versionManually;
    }

    public void setVersionManually(boolean versionManually) {
        this.versionManually = versionManually;
    }

    public boolean isForcePreDelete() {
        return forcePreDelete;
    }

    public void setForcePreDelete(boolean forcePreDelete) {
        this.forcePreDelete = forcePreDelete;
    }
}
