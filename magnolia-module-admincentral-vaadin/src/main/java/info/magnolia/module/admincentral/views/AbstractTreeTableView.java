/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.views;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.AdminCentralVaadinApplication;
import info.magnolia.module.admincentral.tree.TreeColumn;
import info.magnolia.module.admincentral.tree.TreeDefinition;
import info.magnolia.module.admincentral.website.WebsiteTreeTable;

import java.util.Date;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.navigator.Navigator.View;

import com.vaadin.addon.treetable.HieararchicalContainerOrderedWrapper;
import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ContainerHierarchicalWrapper;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table.TableDragMode;

/**
 * A {@link CustomComponent} base class to create view components based on {@link TreeTable}. To obtain the contained tree table use {@link AbstractTreeTableView#getTreeTable()}.
 * Subclasses need to implement the methods defined by the {@link View} interface. This should provide the basic infrastructure needed to handle history (browser's back button behaviour) and "bookmarkability".
 * It is the component/subclass responsibility to handle what should be done when users bookmark an application containing their component. E.g. a component may wish to
 * open the tree at exactly the same point where the user opened before bookmarking the application.
 * To create a tree view for a particular repository, you need to get hold of an instance of  {@link TreeManager} for that repo and typically populate container data source. Concretely, in your subclass constructor you need to do something along these lines
 * <pre>
 * setTreeDefinition(TreeManager.getInstance().getTree("config"));
 * getTreeTable().setContainerDataSource(getConfigRepoData());
 * </pre>
 * @author fgrilli
 *
 */
abstract class AbstractTreeTableView extends CustomComponent implements View {

    private static final long serialVersionUID = 1L;

   // TODO: read the available menus from repository together with their icons
    private static final Action ACTION_ADD = createAddAction();

    private static final Action ACTION_DELETE = createDeleteAction();

    private static final Action ACTION_OTHER = createHelpAction();

    /**
     * TODO: define what menu items and what icons to use. Do not load icons from the web then -
     * that's quite slow...
     */
    private static final Action[] FTL_ACTIONS = new Action[]{ACTION_ADD,
        ACTION_OTHER};

    private static final Action[] JSP_ACTIONS = new Action[]{ACTION_ADD,
        ACTION_DELETE};

    private static Logger log = LoggerFactory.getLogger(AbstractTreeTableView.class);

    private static Action createAddAction() {
        Action add = new Action("Add");
        add.setIcon(new ExternalResource("http://www.iconarchive.com/download/deleket/button/Button-Add.ico"));
        return add;
    }

    private static Action createDeleteAction() {
        Action add = new Action("Delete");
        add.setIcon(new ExternalResource("http://www.iconarchive.com/download/deleket/button/Button-Delete.ico"));
        return add;
    }

    private static Action createHelpAction() {
        Action add = new Action("Other");
        add.setIcon(new ExternalResource("http://www.iconarchive.com/download/deleket/button/Button-Help.ico"));
        return add;
    }

    private Object selectedItemId = null;

    private Object selectedPropertyId = null;

    private TreeDefinition treeDefinition;

    private TreeTable treeTable = new TreeTable();

    public AbstractTreeTableView() {
        setCompositionRoot(treeTable);
        treeTable.setSizeFull();
        treeTable.setEditable(true);
        treeTable.setSelectable(true);
        treeTable.setColumnCollapsingAllowed(true);
        // TODO: check Ticket http://dev.vaadin.com/ticket/5453
        treeTable.setColumnReorderingAllowed(true);
        addContextMenu();
        addEditingByDoubleClick();
        addDragAndDrop();
        setSizeFull();
    }

    public TreeTable getTreeTable() {
        return treeTable;
    }

    public void setTreeDefinition(TreeDefinition treeDefinition) {
        this.treeDefinition = treeDefinition;
    }

    public void addChildren(Content parent, Object parentItemId, ItemType type, String pathToIcon, Container.Hierarchical container) throws RepositoryException{
        for (Content content : parent.getChildren(type)) {
            Object itemId = container.addItem();

            treeTable.setItemIcon(itemId, new ClassResource(pathToIcon, AdminCentralVaadinApplication.application));

            if (parentItemId != null) {
                container.setParent(itemId, parentItemId);
            }

            for (TreeColumn treeColumn : this.treeDefinition.getColumns()) {
                container.getContainerProperty(itemId, treeColumn.getLabel()).setValue(treeColumn.getValue(content));
            }

            addChildrenToContainer(container, content, itemId);
        }
    }


    /**
     * Recursively add Children of passed parent to the provided container.
     */
    private Container.Hierarchical addChildrenToContainer(Container.Hierarchical container, Content parent, Object parentItemId) throws RepositoryException {

        addChildren(parent, parentItemId, ItemType.CONTENT, "/mgnl-resources/icons/16/folder_cubes.gif", container);
        addChildren(parent, parentItemId, ItemType.CONTENTNODE, "/mgnl-resources/icons/16/cubes.gif", container);

        if (treeDefinition.isIncludeNodeData()) {
            for (NodeData nodeData : parent.getNodeDataCollection()) {
                Object nodeDataItemId = container.addItem();

                treeTable.setItemIcon(nodeDataItemId, new ClassResource(
                    "/mgnl-resources/icons/16/cube_green.gif",
                    AdminCentralVaadinApplication.application));
                container.setChildrenAllowed(nodeDataItemId, false);

                for (TreeColumn treeColumn : this.treeDefinition.getColumns()) {
                    container.getContainerProperty(nodeDataItemId, treeColumn.getLabel()).setValue(treeColumn.getValue(parent, nodeData));
                }
                container.setParent(nodeDataItemId, parentItemId);
            }
        }

        return container;
    }


    void addContextMenu() {
        treeTable.addActionHandler(new Action.Handler() {

            public Action[] getActions(Object target, Object sender) {
                Item selection = treeTable.getItem(target);
                String template = (String) selection.getItemProperty(WebsiteTreeTable.TEMPLATE).getValue();
                if (template == null) {
                    return new Action[0];
                }
                // TODO: Just a dummy demo for creating different context menus depending on
                // selected item...
                return template.endsWith("JSP") ? JSP_ACTIONS : FTL_ACTIONS;
            }

            /*
             * Handle actions
             */
            public void handleAction(Action action, Object sender, Object target) {
                if (action == ACTION_ADD) {
                    Object itemId = treeTable.addItem();
                    treeTable.setParent(itemId, target);

                    Item item = treeTable.getItem(itemId);
                    Property name = item.getItemProperty(WebsiteTreeTable.PAGE);
                    name.setValue("New Item");
                    Property status = item.getItemProperty(WebsiteTreeTable.STATUS);
                    status.setValue(0);
                    Property modDate = item.getItemProperty(WebsiteTreeTable.MOD_DATE);
                    modDate.setValue(new Date());
                }
                else if (action == ACTION_DELETE) {
                    treeTable.removeItem(target);
                }
            }
        });
    }

    /**
     *Add Drag and Drop functionality to the provided TreeTable.
     */
    void addDragAndDrop() {
        treeTable.setDragMode(TableDragMode.ROW);
        treeTable.setDropHandler(new DropHandler() {

            /*
             * @seecom.vaadin.event.dd.DropHandler#drop(com.vaadin.event.dd.
             * DragAndDropEvent)
             */
            public void drop(DragAndDropEvent event) {
                // Wrapper for the object that is dragged
                Transferable t = event.getTransferable();

                // Make sure the drag source is the same tree
                if (t.getSourceComponent() != treeTable)
                    return;

                AbstractSelectTargetDetails target = (AbstractSelectTargetDetails) event.getTargetDetails();
                // Get ids of the dragged item and the target item
                Object sourceItemId = t.getData("itemId");
                Object targetItemId = target.getItemIdOver();
                // On which side of the target the item was dropped
                VerticalDropLocation location = target.getDropLocation();

                log.debug("DropLocation: " + location.name());

                HieararchicalContainerOrderedWrapper container = (HieararchicalContainerOrderedWrapper) treeTable
                        .getContainerDataSource();
                // Drop right on an item -> make it a child -
                if (location == VerticalDropLocation.MIDDLE) {
                    treeTable.setParent(sourceItemId, targetItemId);
                    forceRefreshOfTreeTable();
                }
                // Drop at the top of a subtree -> make it previous
                else if (location == VerticalDropLocation.TOP) {
                    Object parentId = container.getParent(targetItemId);
                    if (parentId != null) {
                        log.debug("Parent:" + container.getItem(parentId));
                        container.setParent(sourceItemId, parentId);
                        container.addItemAfter(parentId, sourceItemId);
                        forceRefreshOfTreeTable();
                    }
                }

                // Drop below another item -> make it next
                else if (location == VerticalDropLocation.BOTTOM) {
                    Object parentId = container.getParent(targetItemId);
                    if (parentId != null) {
                        container.setParent(sourceItemId, parentId);
                        // container.moveAfterSibling(sourceItemId,
                        // targetItemId);
                        container.removeItem(targetItemId);
                        container.addItemAfter(sourceItemId, targetItemId);
                        forceRefreshOfTreeTable();
                    }
                }
            }

            private void forceRefreshOfTreeTable() {
                // TODO replace this hack - get Table to be refreshed the proper
                // way (Hack from Vaadin Demo-Sources... - TreeTableWorkLog)
                Object tempId = treeTable.getContainerDataSource().addItem();
                treeTable.removeItem(tempId);
            }

            /*
             * @see com.vaadin.event.dd.DropHandler#getAcceptCriterion()
             */
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }
        });
    }

    void addEditingByDoubleClick() {

        treeTable.setTableFieldFactory(new DefaultFieldFactory() {

            public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                if (selectedItemId != null) {
                    if ((selectedItemId.equals(itemId)) && (selectedPropertyId.equals(propertyId))) {
                        if (ArrayUtils.contains(WebsiteTreeTable.EDITABLE_FIELDS, propertyId)) {
                            return super.createField(container, itemId, propertyId, uiContext);
                        }
                    }
                }
                return null;
            }
        });

        treeTable.addListener(new ItemClickEvent.ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {

                    // TODO we need to unset these somehow...

                    selectedItemId = event.getItemId();
                    selectedPropertyId = event.getPropertyId();
                    treeTable.setEditable(true);
                }
                else if (treeTable.isEditable()) {
                    treeTable.setEditable(false);
                    treeTable.setValue(event.getItemId());
                }
            }
        });
    }

    public Container.Hierarchical getWebsiteData() {

        Content parent = null;
        try {
            parent = MgnlContext.getHierarchyManager(treeDefinition.getRepository()).getContent(treeDefinition.getPath());
        }
        catch (RepositoryException e) {
            /**
             * TODO: proper ExceptionHandling
             */
            throw new RuntimeException(e);
        }

        Container.Hierarchical container = new HieararchicalContainerOrderedWrapper(new ContainerHierarchicalWrapper(new IndexedContainer()));

        for (TreeColumn treeColumn : this.treeDefinition.getColumns()) {
            container.addContainerProperty(treeColumn.getLabel(), treeColumn.getType(), "");
        }

        try {
            addChildrenToContainer(container, parent, null);
        }
        catch (RepositoryException e) {
            // TODO proper exception handling (maybe logging the vaadin exception handler is enough)
            throw new RuntimeException(e);
        }

        return container;
    }
}
