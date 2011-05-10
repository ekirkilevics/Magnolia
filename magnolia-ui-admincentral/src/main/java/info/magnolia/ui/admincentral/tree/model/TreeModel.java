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

import com.vaadin.ui.Component;
import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.util.JCRUtil;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.container.JcrContainerSource;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.workbench.definition.ItemTypeDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

/**
 * Model class for tree. Serves as a source for operations by JcrContainer and executes them.
 *
 * @author tmattsson
 */
public class TreeModel implements JcrContainerSource {

    private WorkbenchActionFactory actionFactory;
    private WorkbenchDefinition workbenchDefinition;
    private Map<String, Column<?>> columns;

    public TreeModel(WorkbenchDefinition workbenchDefinition, Map<String, Column<?>> columns, WorkbenchActionFactory actionFactory) {
        this.workbenchDefinition = workbenchDefinition;
        this.actionFactory = actionFactory;
        this.columns = columns;
    }

    // JcrContainerSource

    @Override
    public Collection<Item> getChildren(Item item) throws RepositoryException {
        if (item instanceof Property) {
            return Collections.emptySet();
        }

        Node node = (Node) item;

        ArrayList<Item> c = new ArrayList<Item>();

        for (ItemTypeDefinition itemType : workbenchDefinition.getItemTypes()) {
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
                @Override
                public int compare(Node lhs, Node rhs) {
                    try {
                        return lhs.getName().compareTo(rhs.getName());
                    } catch (RepositoryException e) {
                        throw new RuntimeRepositoryException(e);
                    }
                }
            });
            //TODO is addAll() more efficient?
            for (Node n : nodes) {
                c.add(n);
            }
        }

        boolean includeProperties = false;
        for (ItemTypeDefinition itemType : this.workbenchDefinition.getItemTypes()) {
            if (itemType.getItemType().equals(ItemTypeDefinition.ITEM_TYPE_NODE_DATA)) {
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
                @Override
                public int compare(Property lhs, Property rhs) {
                    try {
                        return lhs.getName().compareTo(rhs.getName());
                    } catch (RepositoryException e) {
                        throw new RuntimeRepositoryException(e);
                    }
                }
            });
            //TODO is addAll() more efficient?
            for (Property p : properties) {
                c.add(p);
            }
        }

        return Collections.unmodifiableCollection(c);
    }

    @Override
    public Collection<Item> getRootItemIds() throws RepositoryException {
        return getChildren(getRootNode());
    }

    @Override
    public boolean isRoot(Item item) throws RepositoryException {
        if (item instanceof Property) {
            return false;
        }
        int depthOfRootNodesInTree = getRootNode().getDepth() + 1;
        return item.getDepth() <= depthOfRootNodesInTree;
    }

    @Override
    public boolean hasChildren(Item item) throws RepositoryException {
        if (item instanceof Property) {
            return false;
        }
        return !getChildren(item).isEmpty();
    }

    @Override
    public Component getColumnComponent(String columnName, Item item) throws RepositoryException {
        return getColumn(columnName).getComponent(item);
    }

    @Override
    public String getItemIcon(Item item) throws RepositoryException {
        for (ItemTypeDefinition itemType : workbenchDefinition.getItemTypes()) {
            if (item instanceof javax.jcr.Property && itemType.getItemType().equals(ItemTypeDefinition.ITEM_TYPE_NODE_DATA)) {
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

    @Override
    public Node getNodeByIdentifier(String nodeIdentifier) throws RepositoryException {
        return getSession().getNodeByIdentifier(nodeIdentifier);
    }

    @Override
    public Item getItemByPath(String path) throws RepositoryException {
        String absolutePath = getPathInWorkspace(path);
        return getSession().getItem(absolutePath);
    }

    // Move operations performed by drag-n-drop in JcrBrowser

    // TODO these move methods need to be commands instead

    public boolean moveItem(Item source, Item target) throws RepositoryException {

        if (target instanceof Property) {
            return false;
        }

        if (source instanceof Property) {
            // Not yet implemented
            return false;
        }

        JCRUtil.moveNode((Node)source, (Node)target);
        source.getSession().save();

        return true;
    }

    public boolean moveItemBefore(Item source, Item target) throws RepositoryException {

        if (target instanceof Property) {
            return false;
        }

        if (source instanceof Property) {
            // Not yet implemented
            return false;
        }

        JCRUtil.moveNodeBefore((Node)source, (Node)target);
        source.getSession().save();

        return true;
    }

    public boolean moveItemAfter(Item source, Item target) throws RepositoryException {

        if (target instanceof Property) {
            return false;
        }

        if (source instanceof Property) {
            // Not yet implemented
            return false;
        }

        JCRUtil.moveNodeAfter((Node)source, (Node)target);
        source.getSession().save();

        return true;
    }

    // Used by JcrBrowser and TreeViewImpl

    public String getPathInTree(Item item) throws RepositoryException {
        String base = workbenchDefinition.getPath();
        if (base.equals("/")) {
            return item.getPath();
        } else {
            return StringUtils.substringAfter(item.getPath(), base);
        }
    }

    public Map<String,Column<?>> getColumns() {
        return columns;
    }

    public void execute(ActionDefinition actionDefinition, Item item) throws ActionExecutionException {
        Action action = actionFactory.createAction(actionDefinition, item);
        action.execute();
    }

    // Private

    private Session getSession() {
        return MgnlContext.getHierarchyManager(workbenchDefinition.getWorkspace()).getWorkspace().getSession();
    }

    private Node getRootNode() throws RepositoryException {
        return getSession().getNode(workbenchDefinition.getPath());
    }

    private Column<?> getColumn(String columnName) {
        return columns.get(columnName);
    }

    private String getPathInWorkspace(String pathInTree) {
        String base = this.workbenchDefinition.getPath();
        if (base.equals("/")) {
            return pathInTree;
        } else {
            return base + pathInTree;
        }
    }
}
