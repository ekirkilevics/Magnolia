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
package info.magnolia.ui.admincentral.tree.model;

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
import info.magnolia.ui.admincentral.tree.container.JcrContainerSource;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.tree.definition.TreeDefinition;
import info.magnolia.ui.model.tree.definition.TreeItemType;

/**
 * Model class for tree. Serves as a source for operations by JcrContainer and executes
 *
 * @author tmattsson
 */
public class TreeModel implements JcrContainerSource {

    private EditWorkspaceActionFactory actionFactory;
    private TreeDefinition treeDefinition;
    private Map<String, Column<?, ?>> columns;

    public TreeModel(TreeDefinition treeDefinition, Map<String, Column<?, ?>> columns, EditWorkspaceActionFactory actionFactory) {
        this.treeDefinition = treeDefinition;
        this.actionFactory = actionFactory;
        this.columns = columns;
    }

    // JcrContainerSource

    public Collection<Item> getChildren(Item item) throws RepositoryException {
        if (item instanceof Property)
            return Collections.emptySet();

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
    }

    public Collection<Item> getRootItemIds() throws RepositoryException {
        return getChildren(getRootNode());
    }

    public boolean isRoot(Item item) throws RepositoryException {
        if (item instanceof Property)
            return false;
        int depthOfRootNodesInTree = getRootNode().getDepth() + 1;
        return item.getDepth() <= depthOfRootNodesInTree;
    }

    public boolean hasChildren(Item item) throws RepositoryException {
        if (item instanceof Property)
            return false;
        return !getChildren(item).isEmpty();
    }

    public void setColumnValue(String columnLabel, Item item, Object newValue) throws RepositoryException {
        getColumn(columnLabel).setValue(null, item, newValue);
    }

    public Object getColumnValue(String columnLabel, Item item) throws RepositoryException {
        return getColumn(columnLabel).getValue(item);
    }

    public String getItemIcon(Item item) throws RepositoryException {
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
    }

    public Node getNodeByIdentifier(String nodeIdentifier) throws RepositoryException {
        return getSession().getNodeByIdentifier(nodeIdentifier);
    }

    public Item getItemByPath(String path) throws RepositoryException {
        String absolutePath = getPathInWorkspace(path);
        return getSession().getItem(absolutePath);
    }

    // Move operations performed by drag-n-drop in JcrBrowser

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

    // Used by JcrBrowser and TreeViewImpl

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

    public void execute(ActionDefinition actionDefinition, Item item) throws ActionExecutionException {
        Action action = actionFactory.createAction(actionDefinition, item);
        action.execute();
    }

    private Session getSession() {
        return MgnlContext.getHierarchyManager(treeDefinition.getRepository()).getWorkspace().getSession();
    }

    private Node getRootNode() throws RepositoryException {
        return getSession().getNode(treeDefinition.getPath());
    }

    private Column<?, ?> getColumn(String columnLabel) {
        return columns.get(columnLabel);
    }
}
