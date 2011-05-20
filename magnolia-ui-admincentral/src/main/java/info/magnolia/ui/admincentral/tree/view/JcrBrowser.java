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

import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.column.EditEvent;
import info.magnolia.ui.admincentral.column.EditListener;
import info.magnolia.ui.admincentral.column.Editable;
import info.magnolia.ui.admincentral.container.ContainerItemId;
import info.magnolia.ui.admincentral.jcr.JCRUtil;
import info.magnolia.ui.admincentral.tree.container.HierarchicalJcrContainer;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.addon.treetable.HierarchicalContainerOrderedWrapper;
import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Component;


/**
 * User interface component that extends TreeTable and uses a WorkbenchDefinition for layout and
 * invoking command callbacks.
 *
 * @author tmattsson
 */
public class JcrBrowser extends TreeTable {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private WorkbenchDefinition workbenchDefinition;
    private HierarchicalJcrContainer container;
    private Shell shell;

    private TreeModel treeModel;

    public JcrBrowser(WorkbenchDefinition workbenchDefinition, TreeModel treeModel, Shell shell) {
        this.workbenchDefinition = workbenchDefinition;
        this.treeModel = treeModel;
        // TODO the view should not know the shell (it's used to show errors)
        this.shell = shell;

        setSizeFull();
        setEditable(false);
        setSelectable(true);
        setColumnCollapsingAllowed(true);
        setMultiSelect(false);
        setImmediate(true);

        // TODO: check Ticket http://dev.vaadin.com/ticket/5453
        setColumnReorderingAllowed(true);

        addDragAndDrop();

        container = new HierarchicalJcrContainer(treeModel, workbenchDefinition.getWorkspace());

        for (Column< ? > treeColumn : treeModel.getColumns().values()) {
            String columnName = treeColumn.getDefinition().getName();
            super.setColumnExpandRatio(columnName, treeColumn.getWidth() <= 0 ? 1 : treeColumn.getWidth());
            container.addContainerProperty(columnName, Component.class, "");
            super.setColumnHeader(columnName, treeColumn.getLabel());
        }

        setContainerDataSource(container);
        addContextMenu();
        setPageLength(900);

        addListener(new ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                final Object itemId = event.getItemId();
                final String propertyId = (String) event.getPropertyId();
                if (isSelected(itemId)) {
                    Property containerProperty = getContainerProperty(itemId,
                        propertyId);
                    Object value = containerProperty.getValue();
                    if (value instanceof Editable) {
                        final Editable editable = (Editable) value;

                        editable.addListener(new EditListener() {

                            @Override
                            public void edit(EditEvent event) {
                                getContainerProperty(itemId,
                                    propertyId).setValue(editable);
                            }
                        });

                        Component editorComponent = editable.getEditorComponent();
                        containerProperty.setValue(editorComponent);
                    }
                }
            }
        });
    }

    public String getPathInTree(Item item) {
        try {
            return treeModel.getPathInTree(item);
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    // TODO this should not be needed, JcrBrowser should have event mechanisms that expose the JCR
    // item not the ContainerItemId
    public Item getJcrItem(ContainerItemId itemId) {
        try {
            return container.getJcrItem(itemId);
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    private class JcrBrowserAction extends Action {

        private ActionDefinition actionDefinition;

        private JcrBrowserAction(MenuItemDefinition menuItemDefinition) {
            super(menuItemDefinition.getLabel());
            super.setIcon(new ExternalResource(MgnlContext.getContextPath() + menuItemDefinition.getIcon()));
            actionDefinition = menuItemDefinition.getActionDefinition();
        }

        public void handleAction(ContainerItemId itemId) {
            try {
                try {
                    treeModel.execute(actionDefinition, container.getJcrItem(itemId));

                    // Refresh the tree
                    container.fireItemSetChange();
                }
                catch (RepositoryException e) {
                    shell.showError("Can't access content.", e);
                }
            }
            catch (ActionExecutionException e) {
                shell.showError("Can't execute action.", e);
            }
        }
    }

    private void addContextMenu() {

        addActionHandler(new Action.Handler() {

            @Override
            public Action[] getActions(Object target, Object sender) {
                // FIXME make that item type, security dependent
                List<JcrBrowserAction> actions = new ArrayList<JcrBrowserAction>();
                for (MenuItemDefinition menuItemDefinition : workbenchDefinition.getMenuItems()) {
                    actions.add(new JcrBrowserAction(menuItemDefinition));
                }

                return actions.toArray(new Action[actions.size()]);
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                ((JcrBrowserAction) action).handleAction((ContainerItemId) target);
            }
        });
    }

    /**
     * Add Drag and Drop functionality to the provided TreeTable.
     */
    private void addDragAndDrop() {
        setDragMode(TableDragMode.ROW);
        setDropHandler(new DropHandler() {

            /*
             * @seecom.vaadin.event.dd.DropHandler#drop(com.vaadin.event.dd.
             * DragAndDropEvent)
             */
            @Override
            public void drop(DragAndDropEvent event) {

                try {

                    // Wrapper for the object that is dragged
                    Transferable t = event.getTransferable();

                    // Make sure the drag source is the same tree
                    if (t.getSourceComponent() != JcrBrowser.this) {
                        return;
                    }

                    AbstractSelectTargetDetails target = (AbstractSelectTargetDetails) event.getTargetDetails();
                    // Get ids of the dragged item and the target item
                    Object sourceItemId = t.getData("itemId");
                    Object targetItemId = target.getItemIdOver();
                    // On which side of the target the item was dropped
                    VerticalDropLocation location = target.getDropLocation();

                    log.debug("DropLocation: " + location.name());

                    HierarchicalContainerOrderedWrapper containerWrapper = (HierarchicalContainerOrderedWrapper) getContainerDataSource();
                    // Drop right on an item -> make it a child -
                    if (location == VerticalDropLocation.MIDDLE) {
                        Item sourceItem = container.getJcrItem((ContainerItemId) sourceItemId);
                        Item targetItem = container.getJcrItem((ContainerItemId) targetItemId);
                        if (treeModel.moveItem(sourceItem, targetItem)) {
                            setParent(sourceItemId, targetItemId);
                        }
                    }
                    // Drop at the top of a subtree -> make it previous
                    else if (location == VerticalDropLocation.TOP) {
                        Object parentId = containerWrapper.getParent(targetItemId);
                        if (parentId != null) {
                            log.debug("Parent:" + containerWrapper.getItem(parentId));
                            Item sourceItem = container.getJcrItem((ContainerItemId) sourceItemId);
                            Item targetItem = container.getJcrItem((ContainerItemId) targetItemId);
                            if (treeModel.moveItemBefore(sourceItem, targetItem)) {
                                setParent(sourceItemId, targetItemId);
                            }
                        }
                    }

                    // Drop below another item -> make it next
                    else if (location == VerticalDropLocation.BOTTOM) {
                        Object parentId = containerWrapper.getParent(targetItemId);
                        if (parentId != null) {
                            Item sourceItem = container.getJcrItem((ContainerItemId) sourceItemId);
                            Item targetItem = container.getJcrItem((ContainerItemId) targetItemId);
                            if (treeModel.moveItemAfter(sourceItem, targetItem)) {
                                setParent(sourceItemId, targetItemId);
                            }
                        }
                    }
                }
                catch (RepositoryException e) {
                    throw new RuntimeRepositoryException(e);
                }
            }

            /*
             * @see com.vaadin.event.dd.DropHandler#getAcceptCriterion()
             */
            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }
        });
    }

    public void select(String path) {

        ContainerItemId itemId = container.getItemByPath(path);

        if (!container.isRoot(itemId)) {
            ContainerItemId parent = container.getParent(itemId);
            while (!container.isRoot(parent)) {
                setCollapsed(parent, false);
                parent = container.getParent(parent);
            }
            // finally expand the root else children won't be visible.
            setCollapsed(parent, false);
        }

        // Select the item
        select(itemId);

        // Make sure its in view
        // TODO commented out to avoid flicker on selection via place controller while this should
        // definitely be called when navigated by the history
        // setCurrentPageFirstItemId(itemId);
    }

    public void refresh() {
        container.fireItemSetChange();
    }

    @Override
    public Resource getItemIcon(Object itemId) {

        // FIXME this is not the best place to do it, ideally we could set it when we create a new
        // item (investigate, might not make a difference)
        try {

            // TODO should getItemIcon be available on JcrContainerSource ?

            String itemIcon = treeModel.getItemIcon(container.getJcrItem((ContainerItemId) itemId));
            if (itemIcon != null) {
                String tmp = MgnlContext.getContextPath()
                    + (!itemIcon.startsWith(JCRUtil.PATH_SEPARATOR) ? JCRUtil.PATH_SEPARATOR + itemIcon : itemIcon);
                return new ExternalResource(tmp);
            }

        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        return super.getItemIcon(itemId);
    }

    public HierarchicalJcrContainer getContainer() {
        return container;
    }
}
