/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.tree.view;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import info.magnolia.ui.admincentral.tree.container.ContainerItemId;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

/**
 * Vaadin UI component that displays a tree.
 *
 * @author tmattsson
 */
public class TreeViewImpl implements TreeView, IsVaadinComponent {

    private static final long serialVersionUID = 4152174218601590465L;

    private JcrBrowser jcrBrowser;

    private TreeView.Presenter presenter;

    public TreeViewImpl(WorkbenchDefinition workbenchDefinition, TreeModel treeModel, Shell shell) throws RepositoryException {

        jcrBrowser = new JcrBrowser(workbenchDefinition, treeModel, shell);
        jcrBrowser.setSizeFull();
        jcrBrowser.addListener(new ItemClickEvent.ItemClickListener() {

            private static final long serialVersionUID = -6752253952550059502L;

            public void itemClick(ItemClickEvent event) {

                // TODO JcrBrowser should have a click event of its own that sends a JCR item instead of a ContainerItemId

                presenter.onItemSelection(jcrBrowser.getJcrItem((ContainerItemId) event.getItemId()));
            }
        });
    }

    /**
     *
     * @param path relative to the tree root, must start with /
     */
    public void select(String path){
        jcrBrowser.select(path);
    }

    public void refresh() {
        jcrBrowser.refresh();
    }

    public String getPathInTree(Item item) {
        return jcrBrowser.getPathInTree(item);
    }

    public Component asVaadinComponent() {
        return jcrBrowser;
    }


    public void setPresenter(TreeView.Presenter presenter) {
        this.presenter = presenter;
    }
}
