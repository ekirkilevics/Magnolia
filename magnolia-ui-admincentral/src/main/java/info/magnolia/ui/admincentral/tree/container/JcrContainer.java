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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.util.TraversingItemVisitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

import info.magnolia.exception.RuntimeRepositoryException;

/**
 * Vaadin container that reads its items from a JCR repository.
 *
 * @author tmattsson
 */
public class JcrContainer extends AbstractHierarchicalContainer implements Container.ItemSetChangeNotifier {

    private static final Logger log = LoggerFactory.getLogger(JcrContainer.class);

    private Set<ItemSetChangeListener> itemSetChangeListeners;

    private final JcrContainerSource jcrContainerSource;

    private boolean flat = false;

    private int size = 0;

    public JcrContainer(JcrContainerSource jcrContainerSource, boolean flat) {
        this.jcrContainerSource = jcrContainerSource;
        this.flat = flat;
    }

    public void addListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners == null) {
            itemSetChangeListeners = new LinkedHashSet<ItemSetChangeListener>();
        }
        itemSetChangeListeners.add(listener);
    }

    public void removeListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners != null) {
            itemSetChangeListeners.remove(listener);
            if (itemSetChangeListeners.isEmpty()) {
                itemSetChangeListeners = null;
            }
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
        try {
            getJcrItem(((ContainerItemId) itemId));
            return new ContainerItem((ContainerItemId) itemId, this);
        } catch (RepositoryException e) {
            throw new IllegalArgumentException("TODO");
        }
    }

    public Collection<ContainerItemId> getItemIds() {
        try {
            Collection<ContainerItemId> collection = Collections.unmodifiableCollection(createContainerIds(jcrContainerSource.getRootItemIds()));
            size = collection.size();
            return collection;
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public Property getContainerProperty(Object itemId, Object propertyId) {
        return new JcrContainerProperty((String) propertyId, (ContainerItemId) itemId, this);
    }

    public int size() {
        log.debug("jcr container size is {}", size);
        return size;
    }

    public boolean containsId(Object itemId) {
        try {
            getJcrItem((ContainerItemId) itemId);
            return true;
        } catch (RepositoryException e) {
            return false;
        }
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
        try {
            return createContainerIds(jcrContainerSource.getChildren(getJcrItem((ContainerItemId) itemId)));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public ContainerItemId getParent(Object itemId) {
        try {
            javax.jcr.Item item = getJcrItem((ContainerItemId) itemId);
            if (item instanceof javax.jcr.Property) {
                return createContainerId(item.getParent());
            }
            Node node = (Node) item;
            return node.getDepth() > 0 ? createContainerId(node.getParent()) : null;
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public Collection<ContainerItemId> rootItemIds() {
        try {
            return createContainerIds(jcrContainerSource.getRootItemIds());
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
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
        try {
            return jcrContainerSource.isRoot(getJcrItem((ContainerItemId) itemId));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public boolean hasChildren(Object itemId) {
        try {
            return jcrContainerSource.hasChildren(getJcrItem((ContainerItemId) itemId));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        // throw new UnsupportedOperationException();
        fireItemSetChange();
        return true;
    }

    // Used by JcrContainerProperty

    public Object getColumnValue(String propertyId, Object itemId) {
        try {
            return jcrContainerSource.getColumnComponent(propertyId, getJcrItem(((ContainerItemId) itemId)));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public javax.jcr.Item getJcrItem(ContainerItemId containerItemId) throws RepositoryException {
        if (containerItemId == null) {
            return null;
        }
        Node node = jcrContainerSource.getNodeByIdentifier(containerItemId.getNodeIdentifier());
        if (containerItemId.isProperty()) {
            return node.getProperty(containerItemId.getPropertyName());
        }
        return node;
    }

    // Used by JcrBrowser

    public ContainerItemId getItemByPath(String path) {
        try {
            return createContainerId(jcrContainerSource.getItemByPath(path));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public boolean isFlat(){
        return flat;
    }

    // Private
    private Collection<ContainerItemId> createContainerIds(Collection<javax.jcr.Item> children) throws RepositoryException {
        MagnoliaContentTraversingItemVisitor visitor = new MagnoliaContentTraversingItemVisitor(false, isFlat() ? -1 : 0);
        for (javax.jcr.Item child : children) {
            child.accept(visitor);
        }
        return visitor.getIds();
    }

    private ContainerItemId createContainerId(javax.jcr.Item item) throws RepositoryException {
        return new ContainerItemId(item);
    }

    /**
     * Creates a list of {@link ContainerItemId} for all <code>magnolia:content</code> node types in a given item hierarchy.
     * The list of item ids can be retrived with {@link MagnoliaContentTraversingItemVisitor#getIds()}.
     * TODO: make it protected?
     */
    private class MagnoliaContentTraversingItemVisitor extends TraversingItemVisitor {
        private ArrayList<ContainerItemId> ids = new ArrayList<ContainerItemId>();

        public MagnoliaContentTraversingItemVisitor(boolean breadthFirst, int level){
            super(breadthFirst, level);
        }
        @Override
        protected void entering(javax.jcr.Property property, int level) throws RepositoryException {
            //do nothing
        }

        @Override
        protected void entering(Node node, int level) throws RepositoryException {
           //do nothing
        }

        @Override
        protected void leaving(javax.jcr.Property property, int level) throws RepositoryException {
            if(this.maxLevel > -1 && !property.getName().startsWith("jcr:") && !property.getName().startsWith("mgnl:")){
                log.debug("adding property {}", property.getName());
                ids.add(createContainerId(property));
            }
        }

        @Override
        protected void leaving(Node node, int level) throws RepositoryException {
            if(node.getPrimaryNodeType().isNodeType("mgnl:content") || (this.maxLevel > -1 && node.getPrimaryNodeType().isNodeType("mgnl:contentNode")) ){
                log.debug("adding node {}", node.getName());
                ids.add(createContainerId(node));
            }
        }

        public List<ContainerItemId> getIds(){
            return ids;
        }
    }
}
