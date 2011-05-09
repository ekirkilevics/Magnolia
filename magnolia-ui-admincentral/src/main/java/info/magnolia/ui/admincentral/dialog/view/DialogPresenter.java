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
import info.magnolia.ui.admincentral.dialog.DialogSaveCallback;
import info.magnolia.ui.admincentral.dialog.builder.DialogBuilder;
import info.magnolia.ui.admincentral.jcr.JCRUtil;
import info.magnolia.ui.framework.editor.ContentDriver;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;

/**
 * Window for creating or editing content using a dialog.
 *
 * Note: This is a use-once object.
 *
 * TODO should this be merged with {@link info.magnolia.ui.admincentral.dialog.activity.DialogActivity}
 */
public class DialogPresenter implements DialogView.Presenter, Serializable {

    private DialogBuilder dialogBuilder;
    private DialogDefinition dialogDefinition;
    private ContentDriver driver;
    private DialogView dialogView;

    private String workspace;
    private String path;
    private String collectionName;
    private DialogSaveCallback dialogSaveCallback;
    private String label;

    public DialogPresenter(DialogBuilder dialogBuilder, DialogDefinition dialogDefinition) {
        this.dialogBuilder = dialogBuilder;
        this.dialogDefinition = dialogDefinition;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void setDialogSaveCallback(DialogSaveCallback dialogSaveCallback) {
        this.dialogSaveCallback = dialogSaveCallback;
    }

    public void setNode(Node node) throws RepositoryException {
        this.workspace = node.getSession().getWorkspace().getName();
        this.path = node.getPath();
        this.collectionName = null;
    }

    public void showDialog() {

        try {
            // TODO we should be able to set the label/caption of the dialog here

            dialogView = dialogBuilder.build(dialogDefinition);
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

            // TODO we create the node before we know that validation succeeded... not so good

            Node node = getOrCreateNode();
            driver.flush(node);

            if (driver.hasErrors()) {
                // TODO should check if there are unconsumed errors and display them
                //                super.getApplication().getMainWindow().showNotification("You have errors");
            } else {
                dialogView.close();
                // TODO we should fire a tree update event so changes are reflected in the tree view
                // eventBus.fireEvent(new ContentChangedEvent(treeName, path));

                if (dialogSaveCallback != null)
                    dialogSaveCallback.onSave(node);
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
