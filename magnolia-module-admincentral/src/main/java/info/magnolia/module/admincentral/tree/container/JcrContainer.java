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
package info.magnolia.module.admincentral.tree.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.tree.TreeColumn;
import info.magnolia.module.admincentral.tree.TreeDefinition;
import info.magnolia.module.admincentral.tree.TreeItemType;

/**
 * Vaadin container that reads its items from a JCR repository.
 *
 * @author tmattsson
 */
public class JcrContainer extends AbstractHierarchicalContainer implements Container.ItemSetChangeNotifier {

    private Set<ItemSetChangeListener> itemSetChangeListeners;

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

        System.out.println("Firing item set changed");
        if (itemSetChangeListeners != null && !itemSetChangeListeners.isEmpty()) {
            final Container.ItemSetChangeEvent event = new ItemSetChangeEvent();
            Object[] array = itemSetChangeListeners.toArray();
            for (Object anArray : array) {
                ItemSetChangeListener listener = (ItemSetChangeListener) anArray;
                listener.containerItemSetChange(event);
            }
        }
    }

    private TreeDefinition treeDefinition;
    public JcrContainer(TreeDefinition treeDefinition) {
        this.treeDefinition = treeDefinition;
        for (TreeColumn<?> treeColumn : treeDefinition.getColumns()) {
            addContainerProperty(treeColumn.getLabel(), treeColumn.getType(), "");
        }
    }

    // Container

    public Item getItem(Object itemId) {
        return getItem((ContainerItemId)itemId);
    }

    public Collection<ContainerItemId> getItemIds() {
        return getRootItemIds();
    }

    public Property getContainerProperty(Object itemId, Object propertyId) {
        return new JcrContainerProperty((String)propertyId, (ContainerItemId)itemId, this);
    }

    public int size() {
        return 0;
    }

    public boolean containsId(Object itemId) {
        return containsId((ContainerItemId)itemId);
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
        return getChildren((ContainerItemId)itemId);
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
        return isRoot((ContainerItemId)itemId);
    }

    public boolean hasChildren(Object itemId) {
        return hasChildren((ContainerItemId) itemId);
    }

    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
//        throw new UnsupportedOperationException();
        fireItemSetChange();
        return true;
    }

    // Private

    private ContainerItem getItem(ContainerItemId itemId) {
        try {
            getJcrItem(itemId);
            return new ContainerItem(itemId, this);
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    private boolean containsId(ContainerItemId itemId) {
        try {
            getJcrItem(itemId);
            return true;
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
                NodeIterator iterator = node.getNodes();
                while (iterator.hasNext()) {
                    Node next = (Node) iterator.next();
                    if (itemType.getItemType().equals(next.getPrimaryNodeType().getName())) {
                        c.add(new ContainerItemId(next));
                    }
                }
            }

            PropertyIterator propertyIterator = node.getProperties();
            while (propertyIterator.hasNext()) {
                javax.jcr.Property property = propertyIterator.nextProperty();
                for (TreeItemType itemType : treeDefinition.getItemTypes()) {
                    if (!property.getName().startsWith("jcr:") && itemType.getItemType().equals(TreeItemType.ITEM_TYPE_NODE_DATA)) {
                        c.add(new ContainerItemId(node, property.getName()));
                        break;
                    }
                }
            }

            // TODO returned items should be sorted by type then name

            return Collections.unmodifiableCollection(c);

        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    private Collection<ContainerItemId> getRootItemIds() {
        try {
            Session session = getSession();
            Node rootNode = session.getRootNode();
            return getChildren(new ContainerItemId(rootNode));
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return Collections.emptySet();
        }
    }

    private boolean isRoot(ContainerItemId itemId) {
        try {
            if (itemId.isProperty())
                return false;
            return getNode(itemId).getDepth() == 0;
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
            return treeDefinition.getColumn(propertyId).getValue(getJcrItem(itemId));
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    public void setColumnValue(String propertyId, ContainerItemId itemId, Object newValue) {
        try {
            treeDefinition.getColumn(propertyId).setValue(this, getJcrItem(itemId), newValue);
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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
