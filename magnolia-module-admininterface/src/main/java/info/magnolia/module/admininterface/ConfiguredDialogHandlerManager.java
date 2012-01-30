/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.module.admininterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.ExtendingContentWrapper;
import info.magnolia.cms.util.ModuleConfigurationObservingManager;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.SystemContentWrapper;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;
import info.magnolia.objectfactory.Classes;

/**
 * Observes dialogs configured in modules and registers them with {@link DialogHandlerManager} when they're changed.
 *
 * @version $Id$
 */
@Singleton
public class ConfiguredDialogHandlerManager extends ModuleConfigurationObservingManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final String CLASS = "class";
    private static final String ND_NAME = "name";

    private Set<String> registeredIds = new HashSet<String>();
    private final DialogHandlerManager dialogHandlerManager;

    @Inject
    public ConfiguredDialogHandlerManager(ModuleRegistry moduleRegistry, DialogHandlerManager dialogHandlerManager) {
        super("dialogs", moduleRegistry);
        this.dialogHandlerManager = dialogHandlerManager;
    }

    @Override
    protected void reload(List<Node> nodes) throws RepositoryException {

        final List<DialogHandlerProvider> providers = new ArrayList<DialogHandlerProvider>();

        for (Node node2 : nodes) {

            Content node = ContentUtil.asContent(node2);
            
            final List<Content> dialogNodes = new ArrayList<Content>();
            try {
                collectDialogNodes(node, dialogNodes);
            } catch (RepositoryException e) {
                log.error("Can't collect dialog nodes for [" + node.getHandle() + "]: " + ExceptionUtils.getMessage(e), e);
                throw new IllegalStateException("Can't collect dialog nodes for [" + node.getHandle() + "]: " + ExceptionUtils.getMessage(e));
            }

            for (Iterator<Content> iter = dialogNodes.iterator(); iter.hasNext(); ) {
                Content dialogNode = new ExtendingContentWrapper(new SystemContentWrapper(iter.next()));
                try {
                    if (dialogNode.getItemType().equals(ItemType.CONTENT)) {
                        log.warn("Dialog definitions should be of type contentNode but [" + dialogNode.getHandle() + "] is of type content.");
                    }
                } catch (RepositoryException e) {
                    log.error("Can't check for node type of the dialog node [" + dialogNode.getHandle() + "]: " + ExceptionUtils.getMessage(e), e);
                    throw new IllegalStateException("Can't check for node type of the dialog node ["
                            + dialogNode.getHandle()
                            + "]: "
                            + ExceptionUtils.getMessage(e));
                }

                String dialogId;
                try {
                    dialogId = getDialogId(dialogNode);
                } catch (RepositoryException e) {
                    log.warn("Can't establish id for dialog [" + dialogNode.getHandle() + "]: " + ExceptionUtils.getMessage(e), e);
                    continue;
                }

                String name = dialogNode.getNodeData(ND_NAME).getString();
                if (StringUtils.isEmpty(name)) {
                    name = dialogNode.getName();
                }

                // dialog class is not mandatory
                String className = NodeDataUtil.getString(dialogNode, CLASS);
                Class<? extends DialogMVCHandler> dialogClass;
                try {
                    if (StringUtils.isNotEmpty(className)) {
                        dialogClass = Classes.getClassFactory().forName(className);
                    } else {
                        dialogClass = ConfiguredDialog.class;
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("Can't find dialog handler class " + className, e);
                    continue;
                }

                providers.add(new ConfiguredDialogHandlerProvider(name, dialogNode, dialogClass));
                providers.add(new ConfiguredDialogHandlerProvider(dialogId, dialogNode, dialogClass));
            }
        }

        this.registeredIds = dialogHandlerManager.unregisterAndRegister(registeredIds, providers);
    }

    protected String getDialogId(Content node) throws RepositoryException {
        Content moduleNode = node.getAncestor(2);
        Content dialogsNode = node.getAncestor(3);
        return moduleNode.getName() + ":" + StringUtils.removeStart(node.getHandle(), dialogsNode.getHandle() + "/");
    }

    protected void collectDialogNodes(Content current, List<Content> dialogNodes) throws RepositoryException {
        if (isDialogNode(current)) {
            dialogNodes.add(current);
            return;
        }
        for (Content child : ContentUtil.getAllChildren(current)) {
            collectDialogNodes(child, dialogNodes);
        }
    }

    protected boolean isDialogNode(Content node) throws RepositoryException {
        if (isDialogControlNode(node)) {
            return false;
        }

        // if leaf
        if (ContentUtil.getAllChildren(node).isEmpty()) {
            return true;
        }

        // if has node datas
        if (!node.getNodeDataCollection().isEmpty()) {
            return true;
        }

        // if one subnode is a control
        for (Content child : node.getChildren(ItemType.CONTENTNODE)) {
            if (isDialogControlNode(child)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isDialogControlNode(Content node) throws RepositoryException {
        return node.hasNodeData("controlType") || node.hasNodeData("reference");
    }
}
