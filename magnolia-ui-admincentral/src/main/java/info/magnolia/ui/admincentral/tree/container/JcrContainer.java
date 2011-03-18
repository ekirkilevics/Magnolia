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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Vaadin container that reads its items from a JCR repository.
 *
 * @author tmattsson
 */
public class JcrContainer extends AbstractHierarchicalContainer implements Container.ItemSetChangeNotifier {

    private static final long serialVersionUID = 7567243386105952325L;

    private static final Logger log = LoggerFactory.getLogger(JcrContainer.class);

    private Set<ItemSetChangeListener> itemSetChangeListeners;

    private JcrContainerBackend jcrContainerBackend;

    public JcrContainer(JcrContainerBackend jcrContainerBackend) {
        this.jcrContainerBackend = jcrContainerBackend;
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
        if (jcrContainerBackend.containsId((ContainerItemId) itemId))
            return new ContainerItem((ContainerItemId) itemId, this);
        else
            throw new IllegalArgumentException("TODO");
    }

    public Collection<ContainerItemId> getItemIds() {
        return jcrContainerBackend.getRootItemIds();
    }

    public Property getContainerProperty(Object itemId, Object propertyId) {
        return new JcrContainerProperty((String) propertyId, (ContainerItemId) itemId, this);
    }

    public int size() {
        return 0;
    }

    public boolean containsId(Object itemId) {
        return jcrContainerBackend.containsId((ContainerItemId) itemId);
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
        return jcrContainerBackend.getChildren((ContainerItemId) itemId);
    }

    public ContainerItemId getParent(Object itemId) {
        return jcrContainerBackend.getParent((ContainerItemId) itemId);
    }

    public Collection<ContainerItemId> rootItemIds() {
        return jcrContainerBackend.getRootItemIds();
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
        return jcrContainerBackend.isRoot((ContainerItemId) itemId);
    }

    public boolean hasChildren(Object itemId) {
        return jcrContainerBackend.hasChildren((ContainerItemId) itemId);
    }

    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        // throw new UnsupportedOperationException();
        fireItemSetChange();
        return true;
    }

    // Used by JcrContainerProperty

    public Object getColumnValue(String propertyId, Object itemId) {
        return jcrContainerBackend.getColumnValue(propertyId, (ContainerItemId) itemId);
    }

    public void setColumnValue(String propertyId, Object itemId, Object newValue) {
        jcrContainerBackend.setColumnValue(propertyId, (ContainerItemId) itemId, newValue);
    }
}
