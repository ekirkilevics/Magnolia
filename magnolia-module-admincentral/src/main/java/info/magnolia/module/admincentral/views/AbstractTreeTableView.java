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

import info.magnolia.module.admincentral.components.MagnoliaBaseComponent;
import info.magnolia.module.admincentral.tree.TreeColumn;
import info.magnolia.module.admincentral.tree.TreeDefinition;
import info.magnolia.module.admincentral.tree.container.JcrContainer;
import info.magnolia.module.admincentral.tree.container.NodeItem;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.addon.treetable.HierarchicalContainerOrderedWrapper;
import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TableFieldFactory;
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

    private static Logger log = LoggerFactory.getLogger(AbstractTreeTableView.class);

    private static final long serialVersionUID = -1135599469729524071L;

    private Object selectedItemId = null;

    private Object selectedPropertyId = null;

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

    /**
     * Add Drag and Drop functionality to the provided TreeTable.
     */
    void addDragAndDrop() {
        treeTable.setDragMode(TableDragMode.ROW);
        treeTable.setDropHandler(new DropHandler() {

            private static final long serialVersionUID = 1108084327834238921L;

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

                HierarchicalContainerOrderedWrapper container = (HierarchicalContainerOrderedWrapper) treeTable
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
                Object tempId = treeTable.getContainerDataSource().addItem("/tempId");
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

        treeTable.setTableFieldFactory(new TableFieldFactory() {

            private static final long serialVersionUID = 1656067341998458083L;

            public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                if (selectedItemId != null) {
                    if ((selectedItemId.equals(itemId)) && (selectedPropertyId.equals(propertyId))) {
                        try {
                            for (TreeColumn< ? > column : treeDefinition.getColumns()) {
                                if (column.getLabel().equals(propertyId)) {
                                    NodeItem nodeItem = (NodeItem) treeTable.getItem(itemId);
                                        Field field = column.getEditField(nodeItem.getNode());
                                        if (field != null)
                                            return field;

                                }
                            }
                        }
                        catch (RepositoryException e) {
                            log.warn(e.getMessage());
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

    /**
     * Selects the tree node to open based on the uri fragment value.
     */
    public void fragmentChanged(FragmentChangedEvent source) {
        final String fragment = source.getUriFragmentUtility().getFragment();
        if (fragment != null) {
            final String[] uriFragmentTokens = fragment.split(";");
            if (uriFragmentTokens.length <= 1)
                return;
            final String path = uriFragmentTokens[1];
            //leaf is expanded and selected
            setExpanded(path, true);
        }
    }
    /**
     * Expand a tree from the node we want to show, up to the root. Expanding the single node won't suffice to show it
     * as it will be hidden by the collapsed ancestors.
     * @param path the path to the node to expand.
     * @param select if true will also select the corresponding row in the table.
     */
    protected void setExpanded(String path, boolean select) {
        log.debug("expanding {}...", path);

        if(StringUtils.isBlank(path) || treeTable.getParent(path)==null){
            //nothing to expand here
            return;
        }

        treeTable.setCollapsed(path, false);
        if(select){
            treeTable.select(path);
        }
        log.debug("{} is expanded? {} and selected? {}", new Object[]{path, !treeTable.isCollapsed(path), treeTable.isSelected(path)});

        final String parent = StringUtils.substringBeforeLast(path, "/");
        setExpanded(parent, false);
    }

    public Container.Hierarchical createContainer(TreeTable tree) {
        Container.Hierarchical container = new JcrContainer(tree, treeDefinition, "/");
        for (TreeColumn<?> treeColumn : treeDefinition.getColumns()) {
            container.addContainerProperty(treeColumn.getLabel(), treeColumn.getType(), "");
        }
        // Container.Hierarchical container = new FilesystemContainer(new
        // File("/Users/daniellipp/Public"));
        return container;
    }

    public TreeDefinition getTreeDefinition() {
        return treeDefinition;
    }

    public TreeTable getTreeTable() {
        return treeTable;
    }

    public void setTreeDefinition(TreeDefinition treeDefinition) {
        this.treeDefinition = treeDefinition;
    }
}
