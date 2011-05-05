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
package info.magnolia.ui.admincentral.dialog.view;

import java.io.Serializable;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.dialog.builder.DialogBuilder;
import info.magnolia.ui.admincentral.jcr.JCRUtil;
import info.magnolia.ui.framework.editor.ContentDriver;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.registry.DialogRegistry;

/**
 * Window for creating or editing content using a dialog.
 * TODO should this be merged with {@link info.magnolia.ui.admincentral.dialog.activity.DialogActivity}
 */
public class DialogPresenter implements DialogView.Presenter, Serializable {

    private final DialogRegistry dialogRegistry;
    private final ComponentProvider componentProvider;

    private String workspace;
    private String path;
    private String collectionName;
    private ContentDriver driver;
    private DialogView dialogView;

    public DialogPresenter(DialogRegistry dialogRegistry, ComponentProvider componentProvider) {
        this.dialogRegistry = dialogRegistry;
        this.componentProvider = componentProvider;
    }

    public void showDialog(Node userNode, String dialogName) throws RepositoryException {
        showDialog(userNode.getSession().getWorkspace().getName(), userNode.getPath(), null, dialogRegistry.getDialog(dialogName));
    }

    public void showDialog(Node node, DialogDefinition dialogDefinition) throws RepositoryException {
        showDialog(node.getSession().getWorkspace().getName(), node.getPath(), null, dialogDefinition);
    }

    public void showDialog(String workspace, String path, String collectionName, String dialogName) throws RepositoryException {
        showDialog(workspace, path, collectionName, dialogRegistry.getDialog(dialogName));
    }

    public void showDialog(String workspace, String path, String collectionName, DialogDefinition dialogDefinition) {
        this.workspace = workspace;
        this.path = path;
        this.collectionName = collectionName;

        try {
            //            setCaption(storageNode != null ? "Edit paragraph" : "New paragraph");

            DialogBuilder builder = componentProvider.newInstance(DialogBuilder.class);
            dialogView = builder.build(dialogDefinition);
            dialogView.setPresenter(this);

            driver = new ContentDriver();
            driver.initialize(dialogView);

            Node node = getNode();
            if (node != null) {
                driver.edit(node);
            }

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void onSave() {
        try {
            Node node = getOrCreateNode();
            driver.flush(node);

            if (driver.hasErrors()) {
                // TODO should check if there are unconsumed errors and display them
                //                super.getApplication().getMainWindow().showNotification("You have errors");
            } else {
                dialogView.close();
                // TODO we should fire a tree update event so changes are reflected in the tree view
                // eventBus.fireEvent(new ContentChangedEvent(treeName, path));
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    private Node getOrCreateNode() throws RepositoryException {
        try {
            Node node = MgnlContext.getJCRSession(workspace).getNode(path);
            if (collectionName != null) {
                if (node.hasNode(collectionName)) {
                    node = node.getNode(collectionName);
                } else {
                    node = node.addNode(collectionName, ItemType.CONTENTNODE.getSystemName());
                }
                node = node.addNode(JCRUtil.getUniqueLabel(node, "0"), ItemType.CONTENTNODE.getSystemName());
            }
            return node;
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    private Node getNode() throws RepositoryException {
        if (collectionName != null) {
            return null;
        }
        try {
            return MgnlContext.getJCRSession(workspace).getNode(path);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    public void onCancel() {
        dialogView.close();
    }
}
