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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.dialog.builder.DialogBuilder;
import info.magnolia.ui.admincentral.dialog.definition.DialogDefinition;
import info.magnolia.ui.admincentral.dialog.registry.DialogRegistry;
import info.magnolia.ui.admincentral.jcr.JCRUtil;
import info.magnolia.ui.framework.editor.ContentDriver;

/**
 * Window for creating or editing content using a dialog.
 */
public class DialogPresenter implements DialogView.Presenter {

    private DialogRegistry dialogRegistry;
    private ComponentProvider componentProvider;

    private String workspace;
    private String path;
    private ContentDriver driver;
    private DialogView dialogView;

    public DialogPresenter(DialogRegistry dialogRegistry, ComponentProvider componentProvider) {
        this.dialogRegistry = dialogRegistry;
        this.componentProvider = componentProvider;
    }

    public void showDialog(Node userNode, String dialogName) throws RepositoryException {
        showDialog(userNode, dialogRegistry.getDialog(dialogName));
    }

    public void showDialog(Node node, DialogDefinition dialogDefinition) throws RepositoryException {
        showDialog(node.getSession().getWorkspace().getName(), node.getPath(), dialogDefinition);
    }

    public void showDialog(String workspace, String path, DialogDefinition dialogDefinition) {
        this.workspace = workspace;
        this.path = path;

        try {
//            setCaption(storageNode != null ? "Edit paragraph" : "New paragraph");

            Node node = getNode();

            DialogBuilder builder = componentProvider.newInstance(DialogBuilder.class);
            dialogView = builder.build(dialogDefinition);

            driver = new ContentDriver();
            driver.initialize(dialogView);
            driver.edit(node);

            dialogView.setPresenter(this);

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void onSave() {
        try {
            driver.flush(getNode());

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

    private Node getNode() throws RepositoryException {
        return JCRUtil.getSession(workspace).getNode(path);
    }

    public void onCancel() {
        dialogView.close();
    }
}
