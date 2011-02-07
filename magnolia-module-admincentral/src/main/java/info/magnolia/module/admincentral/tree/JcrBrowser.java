/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admincentral.tree;

import java.util.ArrayList;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TableFieldFactory;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.jcr.JCRUtil;
import info.magnolia.module.admincentral.tree.action.TreeAction;
import info.magnolia.module.admincentral.tree.container.ContainerItemId;
import info.magnolia.module.admincentral.tree.container.JcrContainer;

/**
 * User interface component that extends TreeTable and uses a TreeDefinition for layout and invoking command callbacks.
 *
 * @author tmattsson
 */
public class JcrBrowser extends TreeTable {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private TreeDefinition treeDefinition;
    private JcrContainer container;

    private Object selectedItemId = null;
    private Object selectedPropertyId = null;

    public JcrBrowser(String treeName) throws RepositoryException {
        setSizeFull();
        setEditable(false);
        setSelectable(true);
        setColumnCollapsingAllowed(true);

        // TODO: check Ticket http://dev.vaadin.com/ticket/5453
        setColumnReorderingAllowed(true);

        addEditingByDoubleClick();
        addDragAndDrop();
        this.treeDefinition = TreeRegistry.getInstance().getTree(treeName);
        this.container = new JcrContainer(treeDefinition);
        setContainerDataSource(container);
        addContextMenu();
    }

    private void addContextMenu() {

        addActionHandler(new Action.Handler() {

            private static final long serialVersionUID = 4311121075528949148L;

            public Action[] getActions(Object target, Object sender) {

                ArrayList<Action> actions = new ArrayList<Action>();
                try {
                    ContainerItemId itemId = (ContainerItemId) target;
                    Item item = container.getJcrItem(itemId);

                    for (MenuItem mi : treeDefinition.getContextMenuItems()) {
                        TreeAction action = mi.getAction();

                        if (!action.isAvailable(item))
                            continue;

                        action.setCaption(mi.getLabel());
                        action.setIcon(new ExternalResource(MgnlContext.getContextPath() + mi.getIcon()));
                        actions.add(action);

                    }
                } catch (RepositoryException e) {
                    log.error(e.getMessage(), e);
                }

                return actions.toArray(new Action[actions.size()]);
            }

            public void handleAction(Action action, Object sender, Object target) {
                try {
                    ((TreeAction) action).handleAction(JcrBrowser.this, treeDefinition, sender, target);
                } catch (ClassCastException e) {
                    // not our action
                    log.error("Encountered untreatable action {}:{}", action.getCaption(), e.getMessage());
                } catch (RepositoryException e) {
                    log.error(e.getMessage(), e);
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
/*
                HierarchicalContainerOrderedWrapper container = (HierarchicalContainerOrderedWrapper) treeTable.getContainerDataSource();
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
*/
            }

            private void forceRefreshOfTreeTable() {
                // TODO replace this hack - get Table to be refreshed the proper
                // way (Hack from Vaadin Demo-Sources... - TreeTableWorkLog)
                Object tempId = container.addItem("/tempId");
                removeItem(tempId);
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
                            TreeColumn<?> column = treeDefinition.getColumn((String) propertyId);
                            ContainerItemId containerItemId = (ContainerItemId) itemId;
                            Field field = column.getEditField(JcrBrowser.this.container.getJcrItem(containerItemId));
                            if (field != null) {
                                field.focus();
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
                    setValue(event.getItemId()); // Clicking a selected row still unselects it.. strange
                }
            }
        });
    }

    /**
     * Expand a tree from the node we want to show, up to the root. Expanding the single node won't suffice to show it
     * as it will be hidden by the collapsed ancestors.
     *
     * @param path   the path to the node to expand.
     * @param select if true will also select the corresponding row in the table.
     */
    public void setExpanded(String path, boolean select) {
        try {
            log.debug("expanding {}...", path);

            Item item = container.getSession().getItem(path);

            if (item instanceof Node && ((Node) item).getDepth() > 1)
                setExpanded(((Node) item).getParent().getPath(), false);

            ContainerItemId itemId = new ContainerItemId(item);
            setCollapsed(itemId, false);

            if (select) {
                select(itemId);
                setCurrentPageFirstItemId(itemId);
            }
            log.debug("{} is expanded? {} and selected? {}", new Object[]{path, !isCollapsed(path), isSelected(path)});

        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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
