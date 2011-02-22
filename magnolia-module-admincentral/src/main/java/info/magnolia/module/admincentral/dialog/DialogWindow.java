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

/**
 * Window for creating or editing content using a dialog.
 */
public class DialogWindow extends Window implements DialogView.Presenter {

    /**
     * Called after saving and before closing the window.
     */
    public interface PostSaveListener {

        void postSave(Node orCreateContentNode);

    }

    private static final Logger log = LoggerFactory.getLogger(EditParagraphWindow.class);

    private String repository;
    private String path;
    private String nodeCollection;
    private String nodeName;
    private DialogDefinition dialog;
    private PostSaveListener postSaveListener;

    public DialogWindow(String dialogName, String repository, String path, String nodeCollectionName, String nodeName) throws RepositoryException {
        this.repository = repository;
        this.path = path;
        this.nodeCollection = nodeCollectionName;
        this.nodeName = nodeName;
        this.dialog = DialogRegistry.getInstance().getDialog(dialogName);

        setModal(true);
        setResizable(true);
        setScrollable(false);
        setClosable(false);
        setWidth("800px");
//        setCaption(storageNode != null ? "Edit paragraph" : "New paragraph");

        super.getContent().addComponent(new DialogView(this, repository, path, nodeCollectionName, nodeName, dialogName, dialog));
    }

    public DialogWindow(String dialogName, String repository, String path) throws RepositoryException {
        this(dialogName, repository, path, null, null);
    }

    public DialogWindow(String dialogName, Node node) throws RepositoryException {
        this(dialogName, node.getSession().getWorkspace().getName(), node.getPath(), null, null);
    }

    public void onSave() {
    }

    public void onCancel() {
    }

    public void onClose() {
        ((Window) getParent()).removeWindow(this);
    }
}
