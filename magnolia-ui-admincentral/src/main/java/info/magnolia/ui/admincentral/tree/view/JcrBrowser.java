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
import info.magnolia.jcr.util.JCRUtil;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.tree.action.EditWorkspaceActionFactory;
import info.magnolia.ui.admincentral.tree.builder.TreeBuilder;
import info.magnolia.ui.admincentral.tree.container.ContainerItemId;
import info.magnolia.ui.admincentral.tree.container.JcrContainer;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.UIModel;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;
import info.magnolia.ui.model.tree.definition.ColumnDefinition;
import info.magnolia.ui.model.tree.definition.TreeDefinition;
import info.magnolia.ui.model.tree.definition.TreeItemType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
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

    private Object selectedItemId = null;
    private Object selectedPropertyId = null;

    private Map<String, Column<?,?>> columns = new LinkedHashMap<String, Column<?,?>>();

    private EditWorkspaceActionFactory actionFactory;

    private Shell shell;

    // TODO just pass the tree definition
    public JcrBrowser(String treeName, UIModel uiModel, TreeBuilder builder, EditWorkspaceActionFactory actionFactory, Shell shell) throws RepositoryException {
        this.actionFactory = actionFactory;
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
        this.treeDefinition = uiModel.getTreeDefinition(treeName);

        for (ColumnDefinition columnDefintion : treeDefinition.getColumns()) {
            // FIXME use getName() not getLabel()
            Column<?, ?> column = builder.createTreeColumn(columnDefintion);
            // only add if not null - null meaning there's no definitionToImplementationMapping defined for that column.
            if (column != null) {
                columns.put(columnDefintion.getLabel(), column);
            }
        }

        this.container = new JcrContainer(treeDefinition, columns);
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

        public void handleAction(JcrBrowser jcrBrowser, Item item) throws ActionExecutionException {
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
                for(MenuItemDefinition menuItemDefinition: treeDefinition.getContextMenuItems()){
                    actions.add(new JcrBrowserAction(menuItemDefinition));
                }

                return actions.toArray(new Action[actions.size()]);
            }

            public void handleAction(Action action, Object sender, Object target) {
                ContainerItemId containerItemId = (ContainerItemId) target;
                Item item;
                try {
                    item = container.getJcrItem(containerItemId);
                    try {
                        ((JcrBrowserAction) action).handleAction(JcrBrowser.this, item);
                    }
                    catch (ActionExecutionException e) {
                        shell.showError("Can't execute action.", e);
                    }
                }
                catch (RepositoryException e) {
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
                    moveItem(sourceItemId, targetItemId);
                }
                // Drop at the top of a subtree -> make it previous
                else if (location == VerticalDropLocation.TOP) {
                    Object parentId = container.getParent(targetItemId);
                    if (parentId != null) {
                        log.debug("Parent:" + container.getItem(parentId));
                        moveItemBefore(sourceItemId, targetItemId);
                    }
                }

                // Drop below another item -> make it next
                else if (location == VerticalDropLocation.BOTTOM) {
                    Object parentId = container.getParent(targetItemId);
                    if (parentId != null) {
                        moveItemAfter(sourceItemId, targetItemId);
                    }
                }
                } catch (RepositoryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            private void moveItem(Object sourceItemId, Object targetItemId) throws RepositoryException {

                Item source = container.getJcrItem((ContainerItemId) sourceItemId);
                Item target = container.getJcrItem((ContainerItemId) targetItemId);

                if (target instanceof Property)
                    return;

                if (source instanceof Property)
                    // Not yet implemented
                    return;

                source.getSession().move(source.getPath(), target.getPath() + "/" + source.getName());
                source.getSession().save();

                setParent(sourceItemId, targetItemId);
            }

            private void moveItemBefore(Object sourceItemId, Object targetItemId) throws RepositoryException {

                Item source = container.getJcrItem((ContainerItemId) sourceItemId);
                Item target = container.getJcrItem((ContainerItemId) targetItemId);

                if (target instanceof Property)
                    return;

                if (source instanceof Property)
                    // Not yet implemented
                    return;

                // TODO: verify all this works for nodes under root node

                Node targetParent = target.getParent();

                if (!source.getParent().isSame(targetParent)) {
                    source.getSession().move(source.getPath(), targetParent.getPath() + "/" + source.getName());
                }

                targetParent.orderBefore(source.getName(), target.getName());

                source.getSession().save();

                setParent(sourceItemId, new ContainerItemId(target));
//                addItemAfter(targetItemId, sourceItemId);
            }

            private void moveItemAfter(Object sourceItemId, Object targetItemId) throws RepositoryException {
                Item source = container.getJcrItem((ContainerItemId) sourceItemId);
                Item target = container.getJcrItem((ContainerItemId) targetItemId);

                if (target instanceof Property)
                    return;

                if (source instanceof Property)
                    // Not yet implemented
                    return;

                // TODO: verify all this works for nodes under root node

                Node targetParent = target.getParent();

                if (!source.getParent().isSame(targetParent)) {
                    source.getSession().move(source.getPath(), targetParent.getPath() + "/" + source.getName());
                }

                targetParent.orderBefore(target.getName(), source.getName());

                source.getSession().save();

                setParent(sourceItemId, new ContainerItemId(target));
//                addItemAfter(sourceItemId, targetItemId);
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
                            Column<?,?> column = columns.get((String) propertyId);
                            ContainerItemId containerItemId = (ContainerItemId) itemId;
                            Field field = column.getEditField(JcrBrowser.this.container.getJcrItem(containerItemId));
                            if (field != null) {
                                field.focus();
                                if (field instanceof AbstractComponent)
                                    ((AbstractComponent) field).setImmediate(true);
                                return field;
                            }
                        }
                    }
                } catch (RepositoryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

            String absPath = getPathInWorkspace(path);
            Item item = container.getSession().getItem(absPath);
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public String getPathInWorkspace(String pathInTree) {
        String base = this.treeDefinition.getPath();
        if (base.equals("/"))
            return pathInTree;
        else
            return base + pathInTree;
    }

    public JcrContainer getContainer() {
        return container;
    }


    @Override
    public Resource getItemIcon(Object itemId) {
        try {
            Item item = container.getJcrItem((ContainerItemId) itemId);

            for (TreeItemType itemType : treeDefinition.getItemTypes()) {
                if (item instanceof javax.jcr.Property && itemType.getItemType().equals(TreeItemType.ITEM_TYPE_NODE_DATA)) {
                    String pathToIcon = itemType.getIcon();
                    String tmp = MgnlContext.getContextPath() + (!pathToIcon.startsWith(JCRUtil.PATH_SEPARATOR) ? JCRUtil.PATH_SEPARATOR + pathToIcon : pathToIcon);
                    return new ExternalResource(tmp);
                } else if (item instanceof Node) {
                    Node node = (Node) item;
                    if (itemType.getItemType().equals(node.getPrimaryNodeType().getName())) {
                        String pathToIcon = itemType.getIcon();
                        String tmp = MgnlContext.getContextPath() + (!pathToIcon.startsWith(JCRUtil.PATH_SEPARATOR) ? JCRUtil.PATH_SEPARATOR + pathToIcon : pathToIcon);
                        return new ExternalResource(tmp);
                    }
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return super.getItemIcon(itemId);
    }
}
