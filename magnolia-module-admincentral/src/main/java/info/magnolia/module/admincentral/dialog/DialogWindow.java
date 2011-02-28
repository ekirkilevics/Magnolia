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
package info.magnolia.module.admincentral.dialog;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Window;
import info.magnolia.module.admincentral.RuntimeRepositoryException;
import info.magnolia.module.admincentral.editor.ContentDriver;
import info.magnolia.module.admincentral.editor.vaadin.VaadinDialog;
import info.magnolia.module.admincentral.editor.vaadin.VaadinDialogBuilder;
import info.magnolia.module.admincentral.jcr.JCRUtil;

/**
 * Window for creating or editing content using a dialog.
 */
public class DialogWindow extends Window implements VaadinDialog.Presenter {

    private static final Logger log = LoggerFactory.getLogger(EditParagraphWindow.class);

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

            VaadinDialogBuilder builder = new VaadinDialogBuilder();
            VaadinDialog dialog = builder.getDialog();

            driver = new ContentDriver();
            driver.initialize(builder, dialogDefinition);
            driver.edit(node);

            dialog.setPresenter(this);

            super.setCaption(builder.getMessages(dialogDefinition).getWithDefault(dialogDefinition.getLabel(), dialogDefinition.getLabel()));
            super.getContent().addComponent(dialog);

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
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        getParent().removeWindow(this);
    }

    private Node getNode() throws RepositoryException {
        return JCRUtil.getSession(workspace).getNode(path);
    }

    public void onCancel() {
        getParent().removeWindow(this);
    }
}
