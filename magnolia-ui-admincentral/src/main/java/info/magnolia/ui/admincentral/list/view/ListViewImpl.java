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
package info.magnolia.ui.admincentral.list.view;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.jcr.view.JcrView;
import info.magnolia.ui.admincentral.tree.container.ContainerItemId;
import info.magnolia.ui.admincentral.tree.container.JcrContainer;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

/**
 * Vaadin UI component that displays a list.
 * @author fgrilli
 *
 */
public class ListViewImpl implements ListView, IsVaadinComponent {

    private JcrView.Presenter presenter;

    private final Table table;

    private final JcrContainer container;

    private final TreeModel treeModel;

    private final WorkbenchDefinition workbenchDefinition;

    private final Logger log = LoggerFactory.getLogger(ListViewImpl.class);

    public ListViewImpl(WorkbenchDefinition workbenchDefinition, TreeModel treeModel, Shell shell){
        this.treeModel = treeModel;
        table = new Table();
        table.setSizeFull();
        // next two lines are required to make the browser (Table) react on selection change via mouse
        table.setImmediate(true);
        table.setNullSelectionAllowed(false);
        
        table.addListener(new ItemClickEvent.ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    openChildren((ContainerItemId) event.getItemId());
                }
                // TODO JcrBrowser should have a click event of its own that sends a JCR item instead of a ContainerItemId
                presenterOnItemSelection((ContainerItemId) event.getItemId());
            }
        });

        this.workbenchDefinition = workbenchDefinition;

        table.setEditable(false);
        table.setSelectable(true);
        table.setColumnCollapsingAllowed(true);

        // TODO: check Ticket http://dev.vaadin.com/ticket/5453
        table.setColumnReorderingAllowed(true);

        this.container = new JcrContainer(treeModel);

        for (Column<?> treeColumn : treeModel.getColumns().values()) {
            String columnName = treeColumn.getDefinition().getName();
            table.setColumnExpandRatio(columnName, treeColumn.getWidth() <= 0 ? 1 : treeColumn.getWidth());
            container.addContainerProperty(columnName, Component.class, "");
            table.setColumnHeader(columnName, treeColumn.getLabel());
        }

        table.setContainerDataSource(container);
        table.setPageLength(900);
    }

    private void openChildren(ContainerItemId itemId) {
        log.debug("opening folder "+ itemId);
        // TODO: reinitialize table with children on double click
    }

    public void select(String path) {
        ContainerItemId itemId = container.getItemByPath(path);
        table.select(itemId);
    }

    public void refresh() {
        container.fireItemSetChange();
        table.requestRepaintAll();
    }

    public Component asVaadinComponent() {
        return table;
    }

    public String getPathInTree(Item jcrItem) {
        try {
            return treeModel.getPathInTree(jcrItem);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    private void presenterOnItemSelection(ContainerItemId id) {
        if (presenter != null) {
            Item item = null;
            try {
                item = container.getJcrItem(id);
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
            presenter.onItemSelection(item);
        }
    }
}
