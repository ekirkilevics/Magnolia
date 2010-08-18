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
package info.magnolia.module.admincentral.tree;

import com.vaadin.addon.treetable.HieararchicalContainerOrderedWrapper;
import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.data.Container;
import com.vaadin.data.util.ContainerHierarchicalWrapper;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.AdminCentralVaadinApplication;
import info.magnolia.module.admincentral.website.WebsiteTreeTable;
import org.apache.commons.lang.ArrayUtils;

import javax.jcr.RepositoryException;

/**
 * A page for AdminCentral that finds its TreeDefinition in the repository.
 */
public class ConfiguredTreePage extends VerticalLayout {

    private TreeTable treeTable;
    private TreeDefinition treeDefinition;
    private Object selectedItemId = null;
    private Object selectedPropertyId = null;

    public ConfiguredTreePage(String name) {

        this.treeDefinition = new TreeManager().getTree(name);

        treeTable = new TreeTable();
        treeTable.setSizeFull();
        treeTable.setEditable(true);
        treeTable.setSelectable(true);
        treeTable.setColumnCollapsingAllowed(true);
        // TODO dlipp: check whether to open a bug here (reordering does not work when swapping with
        // first (tree) column).
        treeTable.setColumnReorderingAllowed(true);

        treeTable.setContainerDataSource(getWebsiteData());
        setHeight("100%");

        addEditingByDoubleClick();

        addComponent(treeTable);
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
        } catch (RepositoryException e) {
            // TODO proper exception handling (maybe logging the vaadin exception handler is enough)
            throw new RuntimeException(e);
        }

        return container;
    }

    /**
     * Recursively add Children of passed parent to the provided container.
     */
    private Container.Hierarchical addChildrenToContainer(Container.Hierarchical container, Content parent, Object parentItemId) throws RepositoryException {

        for (Content content : parent.getChildren(ItemType.CONTENT)) {

            Object itemId = container.addItem();

            treeTable.setItemIcon(itemId, new ClassResource("/mgnl-resources/icons/16/folder_cubes.gif", AdminCentralVaadinApplication.application));

            if (parentItemId != null) {
                container.setParent(itemId, parentItemId);
            }

            for (TreeColumn treeColumn : this.treeDefinition.getColumns()) {
                container.getContainerProperty(itemId, treeColumn.getLabel()).setValue(treeColumn.getValue(content));
            }

            addChildrenToContainer(container, content, itemId);
        }

        for (Content content : parent.getChildren(ItemType.CONTENTNODE)) {
            Object itemId = container.addItem();

            treeTable.setItemIcon(itemId, new ClassResource("/mgnl-resources/icons/16/cubes.gif", AdminCentralVaadinApplication.application));

            if (parentItemId != null) {
                container.setParent(itemId, parentItemId);
            }

            for (TreeColumn treeColumn : this.treeDefinition.getColumns()) {
                container.getContainerProperty(itemId, treeColumn.getLabel()).setValue(treeColumn.getValue(content));
            }

            addChildrenToContainer(container, content, itemId);
        }

        if (treeDefinition.isIncludeNodeData()) {
            for (NodeData nodeData : parent.getNodeDataCollection()) {
                Object nodeDataItemId = container.addItem();

                treeTable.setItemIcon(nodeDataItemId, new ClassResource("/mgnl-resources/icons/16/cube_green.gif", AdminCentralVaadinApplication.application));
                container.setChildrenAllowed(nodeDataItemId, false);

                for (TreeColumn treeColumn : this.treeDefinition.getColumns()) {
                    container.getContainerProperty(nodeDataItemId, treeColumn.getLabel()).setValue(treeColumn.getValue(parent, nodeData));
                }
                container.setParent(nodeDataItemId, parentItemId);
            }
        }

        return container;
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
}
