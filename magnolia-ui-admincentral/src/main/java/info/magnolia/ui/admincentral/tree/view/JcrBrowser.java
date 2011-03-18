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

import java.util.ArrayList;
import java.util.List;
import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.addon.treetable.HierarchicalContainerOrderedWrapper;
import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.data.Container;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TableFieldFactory;
import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.util.JCRUtil;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.tree.action.EditWorkspaceActionFactory;
import info.magnolia.ui.admincentral.tree.container.ContainerItemId;
import info.magnolia.ui.admincentral.tree.container.JcrContainer;
import info.magnolia.ui.admincentral.tree.container.JcrContainerBackend;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;
import info.magnolia.ui.model.tree.definition.TreeDefinition;

/**
 * User interface component that extends TreeTable and uses a TreeDefinition for layout and invoking command callbacks.
 *
 * @author tmattsson
 */
public class JcrBrowser extends TreeTable {

    private static final long serialVersionUID = -6202685472650709855L;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private TreeDefinition treeDefinition;
    private JcrContainer container;
    private EditWorkspaceActionFactory actionFactory;
    private Shell shell;

    private Object selectedItemId = null;
    private Object selectedPropertyId = null;

    private JcrContainerBackend jcrContainerBackend;

    public JcrBrowser(TreeDefinition treeDefinition, JcrContainerBackend jcrContainerBackend, EditWorkspaceActionFactory actionFactory, Shell shell) throws RepositoryException {
        this.actionFactory = actionFactory;
        this.treeDefinition = treeDefinition;
        this.jcrContainerBackend = jcrContainerBackend;
        // TODO the view should not know the shell
        this.shell = shell;

        setSizeFull();
        setEditable(false);
        setSelectable(true);
        setColumnCollapsingAllowed(true);

        // TODO: check Ticket http://dev.vaadin.com/ticket/5453
        setColumnReorderingAllowed(true);

        addEditingByDoubleClick();
        addDragAndDrop();

        this.container = new JcrContainer(jcrContainerBackend);

        for (Column<?, ?> treeColumn : jcrContainerBackend.getColumns().values()) {
            container.addContainerProperty(treeColumn.getLabel(), treeColumn.getType(), "");
        }

        setContainerDataSource(container);
        addContextMenu();
        setPageLength(900);
    }

    private  class JcrBrowserAction extends Action {
        private static final long serialVersionUID = -5358813017929951816L;
        private ActionDefinition actionDefinition;

        private JcrBrowserAction(MenuItemDefinition menuItemDefinition) {
            super(menuItemDefinition.getLabel());
            super.setIcon(new ExternalResource(MgnlContext.getContextPath() + menuItemDefinition.getIcon()));
            this.actionDefinition = menuItemDefinition.getActionDefinition();
        }

        public void handleAction(Item item) throws ActionExecutionException {
            info.magnolia.ui.model.action.Action action = actionFactory.createAction(actionDefinition, item);
            try {
                action.execute();
            }
            catch (ActionExecutionException e) {
                shell.showError("Can't execute action.", e);
            }
        }
    }

    private void addContextMenu() {

        addActionHandler(new Action.Handler() {
            private static final long serialVersionUID = 4311121075528949148L;

            public Action[] getActions(Object target, Object sender) {
                // FIXME make that item type, security dependent
                List<JcrBrowserAction> actions = new ArrayList<JcrBrowserAction>();
                for (MenuItemDefinition menuItemDefinition : treeDefinition.getContextMenuItems()) {
                    actions.add(new JcrBrowserAction(menuItemDefinition));
                }

                return actions.toArray(new Action[actions.size()]);
            }

            public void handleAction(Action action, Object sender, Object target) {
                try {
                    Item item = jcrContainerBackend.getJcrItem((ContainerItemId) target);
                    try {
                        ((JcrBrowserAction) action).handleAction(item);
                    } catch (ActionExecutionException e) {
                        shell.showError("Can't execute action.", e);
                    }
                } catch (RepositoryException e) {
                    shell.showError("Can't access content.", e);
                }
            }
        });
    }

    /**
     * Add Drag and Drop functionality to the provided TreeTable.
     */
    private void addDragAndDrop() {
        setDragMode(TableDragMode.ROW);
        setDropHandler(new DropHandler() {

            private static final long serialVersionUID = 1108084327834238921L;

            /*
             * @seecom.vaadin.event.dd.DropHandler#drop(com.vaadin.event.dd.
             * DragAndDropEvent)
             */
            public void drop(DragAndDropEvent event) {

                try {

                    // Wrapper for the object that is dragged
                    Transferable t = event.getTransferable();

                    // Make sure the drag source is the same tree
                    if (t.getSourceComponent() != JcrBrowser.this)
                        return;

                    AbstractSelectTargetDetails target = (AbstractSelectTargetDetails) event.getTargetDetails();
                    // Get ids of the dragged item and the target item
                    Object sourceItemId = t.getData("itemId");
                    Object targetItemId = target.getItemIdOver();
                    // On which side of the target the item was dropped
                    VerticalDropLocation location = target.getDropLocation();

                    log.debug("DropLocation: " + location.name());

                    HierarchicalContainerOrderedWrapper container = (HierarchicalContainerOrderedWrapper) getContainerDataSource();
                    // Drop right on an item -> make it a child -
                    if (location == VerticalDropLocation.MIDDLE) {
                        if (jcrContainerBackend.moveItem((ContainerItemId) sourceItemId, (ContainerItemId) targetItemId))
                            setParent(sourceItemId, targetItemId);
                    }
                    // Drop at the top of a subtree -> make it previous
                    else if (location == VerticalDropLocation.TOP) {
                        Object parentId = container.getParent(targetItemId);
                        if (parentId != null) {
                            log.debug("Parent:" + container.getItem(parentId));
                            if (jcrContainerBackend.moveItemBefore((ContainerItemId) sourceItemId, (ContainerItemId) targetItemId))
                                setParent(sourceItemId, targetItemId);
                        }
                    }

                    // Drop below another item -> make it next
                    else if (location == VerticalDropLocation.BOTTOM) {
                        Object parentId = container.getParent(targetItemId);
                        if (parentId != null) {
                            if (jcrContainerBackend.moveItemAfter((ContainerItemId) sourceItemId, (ContainerItemId) targetItemId))
                                setParent(sourceItemId, targetItemId);
                        }
                    }
                } catch (RepositoryException e) {
                    throw new RuntimeRepositoryException(e);
                }
            }

            /*
             * @see com.vaadin.event.dd.DropHandler#getAcceptCriterion()
             */
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }
        });
    }

    private void addEditingByDoubleClick() {

        setTableFieldFactory(new TableFieldFactory() {

            private static final long serialVersionUID = 1656067341998458083L;

            public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                try {
                    if (selectedItemId != null) {
                        if ((selectedItemId.equals(itemId)) && (selectedPropertyId.equals(propertyId))) {
                            Field field = jcrContainerBackend.getFieldForColumn((ContainerItemId) itemId, (String) propertyId);
                            if (field != null) {
                                field.focus();
                                if (field instanceof AbstractComponent)
                                    ((AbstractComponent) field).setImmediate(true);
                                return field;
                            }
                        }
                    }
                } catch (RepositoryException e) {
                    throw new RuntimeRepositoryException(e);
                }
                return null;
            }
        });

        addListener(new ItemClickEvent.ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {

                    // TODO we need to unset these somehow...
                    selectedItemId = event.getItemId();
                    selectedPropertyId = event.getPropertyId();

                    setEditable(true);
                } else if (isEditable()) {
                    setEditable(false);
                }
            }
        });
    }

    public void select(String path) {

        try {

            String absPath = jcrContainerBackend.getPathInWorkspace(path);
            Item item = jcrContainerBackend.getSession().getItem(absPath);
            ContainerItemId itemId = new ContainerItemId(item);

            // Expand parent node all the way up to the root
            if (item.getDepth() > 1) {
                Item parent = item.getParent();
                while (!parent.getPath().equals(treeDefinition.getPath())) {
                    setCollapsed(new ContainerItemId(parent), false);
                    parent = parent.getParent();
                }
            }

            // Select the item
            select(itemId);

            // Make sure its in view
            setCurrentPageFirstItemId(itemId);

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void refresh() {
        container.fireItemSetChange();
    }

    public JcrContainerBackend getJcrContainerBackend() {
        return jcrContainerBackend;
    }

    @Override
    public Resource getItemIcon(Object itemId) {

        // FIXME this is not the best place to do it, ideally we could set it when we create a new item (investigate, might not make a difference)

        ContainerItemId containerItemId = (ContainerItemId) itemId;
        String itemIcon = jcrContainerBackend.getItemIcon(containerItemId);
        if (itemIcon != null) {
            String tmp = MgnlContext.getContextPath() + (!itemIcon.startsWith(JCRUtil.PATH_SEPARATOR) ? JCRUtil.PATH_SEPARATOR + itemIcon : itemIcon);
            return new ExternalResource(tmp);
        }
        return super.getItemIcon(itemId);
    }
}
