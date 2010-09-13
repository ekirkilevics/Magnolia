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
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.components.MagnoliaBaseComponent;
import info.magnolia.module.admincentral.tree.TreeColumn;
import info.magnolia.module.admincentral.tree.TreeDefinition;
import info.magnolia.module.admincentral.tree.TreeItemType;
import info.magnolia.module.admincentral.tree.container.JcrContainer;

import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.addon.treetable.HieararchicalContainerOrderedWrapper;
import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.data.Container;
import com.vaadin.data.util.ContainerHierarchicalWrapper;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;


/**
 * A {@link MagnoliaBaseComponent} implementation to create view components based on
 * {@link TreeTable}. To obtain the contained tree table use
 * {@link AbstractTreeTableView#getTreeTable()}. To get or set the contained {@link TreeDefinition}
 * use {@link AbstractTreeTableView#getTreeDefinition()} and
 * {@link AbstractTreeTableView#setTreeDefinition(TreeDefinition)}, respectively.
 *
 * @author fgrilli
 */
public abstract class AbstractTreeTableView extends MagnoliaBaseComponent {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(AbstractTreeTableView.class);

    /**
     * Keeps already used Resource in order to save resources/not create new Resource for every
     * item.
     */
    private static ConcurrentHashMap<String, Resource> itemIcons = new ConcurrentHashMap<String, Resource>();

    private TreeDefinition treeDefinition;

    private TreeTable treeTable = new TreeTable();

    public AbstractTreeTableView() {
        setCompositionRoot(treeTable);
        treeTable.setSizeFull();
        treeTable.setEditable(false);
        treeTable.setSelectable(true);
        treeTable.setColumnCollapsingAllowed(true);
        // TODO: check Ticket http://dev.vaadin.com/ticket/5453
        treeTable.setColumnReorderingAllowed(true);
        addEditingByDoubleClick();
        addDragAndDrop();
        setSizeFull();
    }

    public TreeTable getTreeTable() {
        return treeTable;
    }

    public TreeDefinition getTreeDefinition() {
        return treeDefinition;
    }

    public void setTreeDefinition(TreeDefinition treeDefinition) {
        this.treeDefinition = treeDefinition;
    }

    /**
     * Add Drag and Drop functionality to the provided TreeTable.
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

    private Object selectedItemId = null;

    private Object selectedPropertyId = null;

    void addEditingByDoubleClick() {

        treeTable.setTableFieldFactory(new TableFieldFactory() {

            public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                if (selectedItemId != null) {
                    if ((selectedItemId.equals(itemId)) && (selectedPropertyId.equals(propertyId))) {

                        try {

                            for (TreeColumn column : treeDefinition.getColumns()) {

                                if (column.getLabel().equals(propertyId)) {

                                    String x = (String) itemId;
                                    if (x.indexOf('@') == -1) {
                                        Content content = MgnlContext.getHierarchyManager(treeDefinition.getRepository()).getContentByUUID(x);

                                        Field field = column.getEditField(content);
                                        if (field != null)
                                            return field;

                                    }
                                    else {
                                        String uuid = StringUtils.substringBefore(x, "@");
                                        String nodeDataName = StringUtils.substringAfter(x, "@");
                                        Content content = MgnlContext.getHierarchyManager(treeDefinition.getRepository()).getContentByUUID(uuid);

                                        NodeData nodeData = content.getNodeData(nodeDataName);

                                        Field field = column.getEditField(content, nodeData);
                                        if (field != null)
                                            return field;
                                    }
                                }
                            }
                        }
                        catch (RepositoryException e) {
                            e.printStackTrace(); // To change body of catch statement use File |
                                                 // Settings | File Templates.
                        }
                    }
                }
                return null;
            }
        });

        treeTable.addListener(new ItemClickEvent.ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {
                setUriFragmentOnItemClickEvent(event);
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

            private void setUriFragmentOnItemClickEvent(ItemClickEvent event) {
                String currentUriFragment = getUriFragmentUtility().getFragment();
                if (StringUtils.isNotEmpty(currentUriFragment)) {
                    String[] tokens = currentUriFragment.split(";");
                    // we already have an item id in the uri fragment, replace it
                    if (tokens.length == 2) {
                        currentUriFragment = tokens[0];
                    }
                    currentUriFragment = currentUriFragment + ";" + event.getItemId();
                }

                log.info("currentUriFragment is {}", currentUriFragment);
                getUriFragmentUtility().setFragment(currentUriFragment, false);
            }
        });
    }

    public Container.Hierarchical getContainer() {
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

        // would be nice to be able to do so, but stupid container insists on showing all props as
        // columns
        // container.addContainerProperty("handle", String.class, "");

        try {
            addChildrenToContainer(container, parent, null);
        }
        catch (RepositoryException e) {
            // TODO proper exception handling (maybe logging the vaadin exception handler is enough)
            throw new RuntimeException(e);
        }
        return container;
    }

    // Preparation for using proper JcrContainer...
    public Container.Hierarchical getContainer(TreeTable tree) {
        Container.Hierarchical container = new JcrContainer(tree, treeDefinition, "/");
        for (TreeColumn treeColumn : treeDefinition.getColumns()) {
            container.addContainerProperty(treeColumn.getLabel(), treeColumn.getType(), "");
        }
        return container;
    }

    /**
     * Recursively add Children of passed parent to the provided container.
     */
    private Container.Hierarchical addChildrenToContainer(Container.Hierarchical container, Content parent, Object parentItemId) throws RepositoryException {

        for (TreeItemType itemType : this.treeDefinition.getItemTypes()) {

            if (itemType.getItemType().equals(TreeItemType.ITEM_TYPE_NODE_DATA))
                addNodeDataToContainer(container, parent, parentItemId, itemType.getIcon());

            addChildrenOfType(container, parent, parentItemId, itemType.getItemType(), itemType.getIcon());
        }

        return container;
    }

    private void addNodeDataToContainer(Container.Hierarchical container, Content parent, Object parentItemId, String nodeDataIcon) {
        for (NodeData nodeData : parent.getNodeDataCollection()) {

            String nodeDataItemId = parent.getUUID() + "@" + nodeData.getName();
            container.addItem(nodeDataItemId);

            treeTable.setItemIcon(nodeDataItemId, getItemIconFor(nodeDataIcon));
            container.setChildrenAllowed(nodeDataItemId, false);

            for (TreeColumn treeColumn : this.treeDefinition.getColumns()) {
                container.getContainerProperty(nodeDataItemId, treeColumn.getLabel()).setValue(treeColumn.getValue(parent, nodeData));
            }
            container.setParent(nodeDataItemId, parentItemId);
        }
    }

    /**
     * Extracts all relevant properties (handle + those specified by columns) from the content to
     * the item representing piece of content in the tree table.
     */
    public void addChildrenOfType(Container.Hierarchical container, Content parent, Object parentItemId, String type, String pathToIcon) throws RepositoryException {
        for (Content content : parent.getChildren(type)) {

            String itemId = content.getUUID();
            container.addItem(itemId);

            treeTable.setItemIcon(itemId, getItemIconFor(pathToIcon));

            if (parentItemId != null) {
                container.setParent(itemId, parentItemId);
            }

            for (TreeColumn treeColumn : this.treeDefinition.getColumns()) {
                container.getContainerProperty(itemId, treeColumn.getLabel()).setValue(treeColumn.getValue(content));
            }
            // don't do this otherwise all handles become visible columns ... :(
            // container.getContainerProperty(itemId, "handle").setValue(content.getHandle());

            addChildrenToContainer(container, content, itemId);
        }
    }

    private Resource getItemIconFor(String pathToIcon) {
        if (!itemIcons.containsKey(pathToIcon)) {
            // check if this path starts or not with a /
            String tmp = MgnlContext.getContextPath() + (!pathToIcon.startsWith("/") ? "/" + pathToIcon : pathToIcon);
            itemIcons.put(pathToIcon, new ExternalResource(tmp));
        }
        return itemIcons.get(pathToIcon);
    }

    /**
     * Selects the tree node to open based on the uri fragment value.
     */
    public void fragmentChanged(FragmentChangedEvent source) {
        final String fragment = source.getUriFragmentUtility().getFragment();
        if (fragment != null) {
            final String[] uriFragmentTokens = fragment.split(";");
            if (uriFragmentTokens.length <= 1)
                return;
            final String treeItemToOpenId = uriFragmentTokens[1];
            log.debug("before: is selected? {}", treeTable.isSelected(treeItemToOpenId));
            treeTable.select(treeItemToOpenId);
            log.debug("after: is selected? {}", treeTable.isSelected(treeItemToOpenId));
        }
    }
}
