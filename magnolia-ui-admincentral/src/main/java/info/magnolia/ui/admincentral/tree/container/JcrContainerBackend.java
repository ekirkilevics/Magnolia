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
package info.magnolia.ui.admincentral.tree.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

import com.vaadin.ui.Field;
import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.tree.action.EditWorkspaceActionFactory;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.tree.definition.TreeDefinition;
import info.magnolia.ui.model.tree.definition.TreeItemType;

/**
 * Backend operations for the JcrContainer and JcrBrowser.
 *
 * TODO this should merge with the tree presenter.
 *
 * @author tmattsson
 */
public class JcrContainerBackend {

    private EditWorkspaceActionFactory actionFactory;
    private TreeDefinition treeDefinition;
    private Map<String, Column<?, ?>> columns;

    public JcrContainerBackend(TreeDefinition treeDefinition, Map<String, Column<?, ?>> columns, EditWorkspaceActionFactory actionFactory) {
        this.treeDefinition = treeDefinition;
        this.actionFactory = actionFactory;
        this.columns = columns;
    }
/*
    public boolean containsId(ContainerItemId itemId) {
        try {
            getJcrItem(itemId);
            return true;
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
*/
    public Collection<Item> getChildren(Item item) {
        if (item instanceof Property)
            return Collections.emptySet();
        try {
            Node node = (Node) item;

            ArrayList<Item> c = new ArrayList<Item>();

            for (TreeItemType itemType : treeDefinition.getItemTypes()) {
                ArrayList<Node> nodes = new ArrayList<Node>();
                NodeIterator iterator = node.getNodes();
                while (iterator.hasNext()) {
                    Node next = (Node) iterator.next();
                    if (itemType.getItemType().equals(next.getPrimaryNodeType().getName())) {
                        nodes.add(next);
                    }
                }
                // TODO This behaviour is optional in old AdminCentral, you can set a custom comparator.
                Collections.sort(nodes, new Comparator<Node>() {
                    public int compare(Node lhs, Node rhs) {
                        try {
                            return lhs.getName().compareTo(rhs.getName());
                        } catch (RepositoryException e) {
                            throw new RuntimeRepositoryException(e);
                        }
                    }
                });
                for (Node n : nodes) {
                    c.add(n);
                }
            }

            boolean includeProperties = false;
            for (TreeItemType itemType : this.treeDefinition.getItemTypes()) {
                if (itemType.getItemType().equals(TreeItemType.ITEM_TYPE_NODE_DATA)) {
                    includeProperties = true;
                    break;
                }
            }

            if (includeProperties) {
                ArrayList<Property> properties = new ArrayList<Property>();
                PropertyIterator propertyIterator = node.getProperties();
                while (propertyIterator.hasNext()) {
                    Property property = propertyIterator.nextProperty();
                    if (!property.getName().startsWith("jcr:")) {
                        properties.add(property);
                    }
                }
                Collections.sort(properties, new Comparator<Property>() {
                    public int compare(Property lhs, Property rhs) {
                        try {
                            return lhs.getName().compareTo(rhs.getName());
                        } catch (RepositoryException e) {
                            throw new RuntimeRepositoryException(e);
                        }
                    }
                });
                for (Property p : properties) {
                    c.add(p);
                }
            }

            return Collections.unmodifiableCollection(c);

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
/*
    public ContainerItemId getParent(ContainerItemId itemId) {
        try {
            if (itemId.isProperty())
                return new ContainerItemId(itemId.getNodeIdentifier());
            Node node = (Node) getJcrItem(itemId);
            return new ContainerItemId(node.getParent());
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
*/
    public Collection<Item> getRootItemIds() {
        try {
            return getChildren(getRootNode());
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public boolean isRoot(Item item) {
        try {
            if (item instanceof Property)
                return false;
            int depthOfRootNodesInTree = getRootNode().getDepth() + 1;
            return item.getDepth() <= depthOfRootNodesInTree;
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public boolean hasChildren(Item item) {
        if (item instanceof Property)
            return false;
        return !getChildren((Node)item).isEmpty();
    }

    public void setColumnValue(String columnLabel, Item item, Object newValue) {
        try {
            getColumn(columnLabel).setValue(null, item, newValue);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public Object getColumnValue(String columnLabel, Item item) {
        try {
            return getColumn(columnLabel).getValue(item);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    private Column<?, ?> getColumn(String columnLabel) {
        return columns.get(columnLabel);
    }

    public Session getSession() {
        return MgnlContext.getHierarchyManager(treeDefinition.getRepository()).getWorkspace().getSession();
    }

    public String getItemIcon(Item item) {
        try {

            for (TreeItemType itemType : treeDefinition.getItemTypes()) {
                if (item instanceof javax.jcr.Property && itemType.getItemType().equals(TreeItemType.ITEM_TYPE_NODE_DATA)) {
                    return itemType.getIcon();
                } else if (item instanceof Node) {
                    Node node = (Node) item;
                    if (itemType.getItemType().equals(node.getPrimaryNodeType().getName())) {
                        return itemType.getIcon();
                    }
                }
            }
            return null;
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    // TODO these move methods need to be commands instead

    public boolean moveItem(Item source, Item target) throws RepositoryException {

        if (target instanceof Property)
            return false;

        if (source instanceof Property)
            // Not yet implemented
            return false;

        source.getSession().move(source.getPath(), target.getPath() + "/" + source.getName());
        source.getSession().save();

        return true;
    }

    public boolean moveItemBefore(Item source, Item target) throws RepositoryException {

        if (target instanceof Property)
            return false;

        if (source instanceof Property)
            // Not yet implemented
            return false;

        // TODO: verify all this works for nodes under root node

        Node targetParent = target.getParent();

        if (!source.getParent().isSame(targetParent)) {
            source.getSession().move(source.getPath(), targetParent.getPath() + "/" + source.getName());
        }

        targetParent.orderBefore(source.getName(), target.getName());

        source.getSession().save();
        return true;
    }

    public boolean moveItemAfter(Item source, Item target) throws RepositoryException {

        if (target instanceof Property)
            return false;

        if (source instanceof Property)
            // Not yet implemented
            return false;

        // TODO: verify all this works for nodes under root node

        Node targetParent = target.getParent();

        if (!source.getParent().isSame(targetParent)) {
            source.getSession().move(source.getPath(), targetParent.getPath() + "/" + source.getName());
        }

        targetParent.orderBefore(target.getName(), source.getName());

        source.getSession().save();
        return true;
    }

    public String getPathInWorkspace(String pathInTree) {
        String base = this.treeDefinition.getPath();
        if (base.equals("/"))
            return pathInTree;
        else
            return base + pathInTree;
    }

    public String getPathInTree(Item item) throws RepositoryException {
        String base = treeDefinition.getPath();
        if (base.equals("/"))
            return item.getPath();
        else
            return StringUtils.substringAfter(item.getPath(), base);
    }

    // TODO should be name not label
    public Field getFieldForColumn(Item item, String columnLabel) throws RepositoryException {
        Column<?,?> column = columns.get((String) columnLabel);
        return column.getEditField(item);
    }

    public Map<String,Column<?, ?>> getColumns() {
        return columns;
    }

    public Item getItemByPath(String path) {
        try {
            String absolutePath = getPathInWorkspace(path);
            return getSession().getItem(absolutePath);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void execute(ActionDefinition actionDefinition, Item item) throws ActionExecutionException {
        Action action = actionFactory.createAction(actionDefinition, item);
        action.execute();
    }

    private Node getRootNode() throws RepositoryException {
        return getSession().getNode(treeDefinition.getPath());
    }
}
