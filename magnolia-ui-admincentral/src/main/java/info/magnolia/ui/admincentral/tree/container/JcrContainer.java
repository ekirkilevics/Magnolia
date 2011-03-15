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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.ui.admincentral.tree.column.TreeColumn;
import info.magnolia.ui.model.tree.definition.TreeDefinition;
import info.magnolia.ui.model.tree.definition.TreeItemType;

/**
 * Vaadin container that reads its items from a JCR repository.
 *
 * @author tmattsson
 */
public class JcrContainer extends AbstractHierarchicalContainer implements Container.ItemSetChangeNotifier {
    private static final long serialVersionUID = 7567243386105952325L;

    private static final Logger log = LoggerFactory.getLogger(JcrContainer.class);

    private Set<ItemSetChangeListener> itemSetChangeListeners;

    private TreeDefinition treeDefinition;

    private Map<String, TreeColumn<?, ?>> columns;

    public JcrContainer(TreeDefinition treeDefinition, Map<String, TreeColumn<?, ?>> columns) {

        this.treeDefinition = treeDefinition;
        this.columns = columns;

        for (TreeColumn<?, ?> treeColumn : columns.values()) {
            addContainerProperty(treeColumn.getLabel(), treeColumn.getType(), "");
        }
    }

    public void addListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners == null)
            itemSetChangeListeners = new LinkedHashSet<ItemSetChangeListener>();
        itemSetChangeListeners.add(listener);
    }

    public void removeListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners != null) {
            itemSetChangeListeners.remove(listener);
            if (itemSetChangeListeners.isEmpty())
                itemSetChangeListeners = null;
        }
    }

    public void fireItemSetChange() {

        log.debug("Firing item set changed");
        if (itemSetChangeListeners != null && !itemSetChangeListeners.isEmpty()) {
            final Container.ItemSetChangeEvent event = new ItemSetChangeEvent();
            Object[] array = itemSetChangeListeners.toArray();
            for (Object anArray : array) {
                ItemSetChangeListener listener = (ItemSetChangeListener) anArray;
                listener.containerItemSetChange(event);
            }
        }
    }

    // Container

    public Item getItem(Object itemId) {
        return getItem((ContainerItemId) itemId);
    }

    public Collection<ContainerItemId> getItemIds() {
        return getRootItemIds();
    }

    public Property getContainerProperty(Object itemId, Object propertyId) {
        return new JcrContainerProperty((String) propertyId, (ContainerItemId) itemId, this);
    }

    public int size() {
        return 0;
    }

    public boolean containsId(Object itemId) {
        return containsId((ContainerItemId) itemId);
    }

    public Item addItem(Object itemId) throws UnsupportedOperationException {
        fireItemSetChange();
        return getItem(itemId);
    }

    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    // Container.Hierarchical

    public Collection<ContainerItemId> getChildren(Object itemId) {
        return getChildren((ContainerItemId) itemId);
    }

    public ContainerItemId getParent(Object itemId) {
        return getParent((ContainerItemId) itemId);
    }

    public Collection<ContainerItemId> rootItemIds() {
        return getRootItemIds();
    }

    public boolean setParent(Object itemId, Object newParentId) throws UnsupportedOperationException {
        fireItemSetChange();
        return true;
    }

    public boolean areChildrenAllowed(Object itemId) {
        return ((ContainerItemId) itemId).isNode();
    }

    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public boolean isRoot(Object itemId) {
        return isRoot((ContainerItemId) itemId);
    }

    public boolean hasChildren(Object itemId) {
        return hasChildren((ContainerItemId) itemId);
    }

    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        // throw new UnsupportedOperationException();
        fireItemSetChange();
        return true;
    }

    // Private

    private ContainerItem getItem(ContainerItemId itemId) {
        try {
            getJcrItem(itemId);
            return new ContainerItem(itemId, this);
        } catch (RepositoryException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    private boolean containsId(ContainerItemId itemId) {
        try {
            getJcrItem(itemId);
            return true;
        } catch (RepositoryException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
            return false;
        }
    }

    private Collection<ContainerItemId> getChildren(ContainerItemId itemId) {
        try {
            if (itemId.isProperty())
                return Collections.emptySet();
            Node node = getNode(itemId);

            ArrayList<ContainerItemId> c = new ArrayList<ContainerItemId>();

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
                    c.add(new ContainerItemId(n));
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
                ArrayList<javax.jcr.Property> properties = new ArrayList<javax.jcr.Property>();
                PropertyIterator propertyIterator = node.getProperties();
                while (propertyIterator.hasNext()) {
                    javax.jcr.Property property = propertyIterator.nextProperty();
                    if (!property.getName().startsWith("jcr:")) {
                        properties.add(property);
                    }
                }
                Collections.sort(properties, new Comparator<javax.jcr.Property>() {
                    public int compare(javax.jcr.Property lhs, javax.jcr.Property rhs) {
                        try {
                            return lhs.getName().compareTo(rhs.getName());
                        } catch (RepositoryException e) {
                            throw new RuntimeRepositoryException(e);
                        }
                    }
                });
                for (javax.jcr.Property p : properties) {
                    c.add(new ContainerItemId(node, p.getName()));
                }
            }

            return Collections.unmodifiableCollection(c);

        } catch (RepositoryException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
            return Collections.emptySet();
        }
    }

    private ContainerItemId getParent(ContainerItemId itemId) {
        try {
            if (itemId.isProperty())
                return new ContainerItemId(itemId.getNodeIdentifier());
            Node node = getNode(itemId);
            return new ContainerItemId(node.getParent());
        } catch (RepositoryException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    private Collection<ContainerItemId> getRootItemIds() {
        try {
            Session session = getSession();
            Node rootNode = session.getNode(treeDefinition.getPath());
            return getChildren(new ContainerItemId(rootNode));
        } catch (RepositoryException e) {
            log.warn("Could not get RootItemIds", e);
            return Collections.emptySet();
        }
    }

    private boolean isRoot(ContainerItemId itemId) {
        try {
            if (itemId.isProperty())
                return false;
            return getNode(itemId).getDepth() == 0;
        } catch (RepositoryException e) {
            log.warn("Could not determine whether id " + itemId + " is root or not", e);
            return false;
        }
    }

    private boolean hasChildren(ContainerItemId itemId) {
        if (itemId.isProperty())
            return false;
        return !getChildren(itemId).isEmpty();
    }

    public Object getColumnValue(String propertyId, ContainerItemId itemId) {
        try {
            return getColumn(propertyId).getValue(getJcrItem(itemId));
        } catch (RepositoryException e) {
            log.warn("Could not determine columnValue", e);
            return null;
        }
    }

    public void setColumnValue(String propertyId, ContainerItemId itemId, Object newValue) {
        try {
            getColumn(propertyId).setValue(this, getJcrItem(itemId), newValue);
        } catch (RepositoryException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
    }

    // FIXME this is the job of a tree builder
    private TreeColumn<?, ?> getColumn(String propertyId) {
        return columns.get(propertyId);
    }

    public Node getNode(ContainerItemId itemId) throws RepositoryException {
        try {
            return getSession().getNodeByIdentifier(itemId.getNodeIdentifier());
        } catch (NullPointerException e) {
            throw e;
        }
    }

    public javax.jcr.Property getProperty(ContainerItemId itemId) throws RepositoryException {
        return getNode(itemId).getProperty(itemId.getPropertyName());
    }

    public Session getSession() {
        return MgnlContext.getHierarchyManager(treeDefinition.getRepository()).getWorkspace().getSession();
    }

    public javax.jcr.Item getJcrItem(ContainerItemId containerItemId) throws RepositoryException {
        Node node = getNode(containerItemId);
        if (containerItemId.isProperty())
            return node.getProperty(containerItemId.getPropertyName());
        return node;
    }
}
