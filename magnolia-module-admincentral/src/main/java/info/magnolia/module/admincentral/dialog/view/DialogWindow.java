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
package info.magnolia.module.admincentral.dialog.view;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.ui.Window;

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.module.admincentral.dialog.builder.VaadinDialogBuilder;
import info.magnolia.module.admincentral.dialog.definition.DialogDefinition;
import info.magnolia.module.admincentral.dialog.registry.DialogRegistry;
import info.magnolia.module.admincentral.jcr.JCRUtil;
import info.magnolia.ui.editor.ContentDriver;

/**
 * Window for creating or editing content using a dialog.
 */
public class DialogWindow extends Window implements DialogView.Presenter {

    private String workspace;
    private String path;
    private ContentDriver driver;

    public DialogWindow(String dialogName, String workspace, String path) {
        this.workspace = workspace;
        this.path = path;

        try {
            setModal(true);
            setResizable(true);
            setScrollable(false);
            setClosable(false);
            setWidth("800px");
//            setCaption(storageNode != null ? "Edit paragraph" : "New paragraph");

            Node node = getNode();

            DialogDefinition dialogDefinition = DialogRegistry.getInstance().getDialog(dialogName);

            // FIXME inject the builder
            VaadinDialogBuilder builder = new VaadinDialogBuilder();
            DialogView dialog = builder.build(dialogDefinition);

            driver = new ContentDriver();
            driver.initialize(dialog);
            driver.edit(node);

            dialog.setPresenter(this);

            super.setCaption(builder.getMessages(dialogDefinition).getWithDefault(dialogDefinition.getLabel(), dialogDefinition.getLabel()));
            super.getContent().addComponent(dialog.asComponent());

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public DialogWindow(String dialogName, Node node) throws RepositoryException {
        this(dialogName, node.getSession().getWorkspace().getName(), node.getPath());
    }

    public void onSave() {
        try {
            driver.flush(getNode());

            if (driver.hasErrors()) {
                    // TODO should check if there are unconsumed errors and display them
//                super.getApplication().getMainWindow().showNotification("You have errors");
            } else {
                getParent().removeWindow(this);
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    private Node getNode() throws RepositoryException {
        return JCRUtil.getSession(workspace).getNode(path);
    }

    public void onCancel() {
        getParent().removeWindow(this);
    }
}