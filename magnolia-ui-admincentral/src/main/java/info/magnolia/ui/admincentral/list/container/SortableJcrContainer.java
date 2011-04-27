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
package info.magnolia.ui.admincentral.list.container;

import info.magnolia.ui.admincentral.tree.container.ContainerItemId;
import info.magnolia.ui.admincentral.tree.container.JcrContainer;
import info.magnolia.ui.admincentral.tree.container.JcrContainerSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.RepositoryException;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ContainerOrderedWrapper;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.data.util.ItemSorter;

/**
 * TODO: review.
 * Sortable jcr container.
 * @author had
 *
 */
public class SortableJcrContainer implements Container.Sortable {

    private ContainerOrderedWrapper wrappedJcrContainer;
    private JcrContainer internalJcrContainer;
    //FIXME DefaultItemSorter wants also inner classes to be comparable?
    private ItemSorter itemSorter = new DefaultItemSorter();

    public SortableJcrContainer(JcrContainerSource jcrContainerSource) {
        internalJcrContainer = new JcrContainer(jcrContainerSource, true);
        wrappedJcrContainer = new ContainerOrderedWrapper(internalJcrContainer);
    }

    public ItemSorter getItemSorter() {
        return itemSorter;
    }

    public void setItemSorter(ItemSorter itemSorter) {
        this.itemSorter = itemSorter;
    }

    public String toString() {
        return wrappedJcrContainer.toString();
    }

    public void sort(Object[] propertyId, boolean[] ascending) {
        // Set up the item sorter for the sort operation
        itemSorter.setSortProperties(this, propertyId, ascending);

        // Perform the actual sort
        doSort();
    }

    public Collection<?> getSortableContainerPropertyIds() {
        final List<Object> list = new LinkedList<Object>();
        for (final Iterator<?> i = getContainerPropertyIds().iterator(); i.hasNext();) {
            final Object id = i.next();
            //FIXME always return Component which can not be cast to Comparable hence no cols are sortable.
            //See ListViewImpl line 111. Assigning there the correct class implementing the column type causes the values being displayed as classname@hashcode (Vaadin evidently uses toString())
            final Class<?> type = getType(id);
            if (type != null && Comparable.class.isAssignableFrom(type)) {
                list.add(id);
            }
        }
        return list;
    }

    /**
     * Perform the sorting of the data structures in the container. This is
     * invoked when the <code>itemSorter</code> has been prepared for the sort
     * operation. Typically this method calls
     * <code>Collections.sort(aCollection, getItemSorter())</code> on all arrays
     * (containing item ids) that need to be sorted.
     *
     */
    @SuppressWarnings("unchecked")
    protected void doSort() {
        Collections.sort(new ArrayList(getItemIds()), getItemSorter());
    }

    public Object nextItemId(Object itemId) {
        return wrappedJcrContainer.nextItemId(itemId);
    }

    public Object prevItemId(Object itemId) {
        return wrappedJcrContainer.prevItemId(itemId);
    }

    public Object firstItemId() {
        return wrappedJcrContainer.firstItemId();
    }

    public Object lastItemId() {
        return wrappedJcrContainer.lastItemId();
    }

    public boolean isFirstId(Object itemId) {
        return wrappedJcrContainer.isFirstId(itemId);
    }

    public boolean isLastId(Object itemId) {
        return wrappedJcrContainer.isLastId(itemId);
    }

    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        return wrappedJcrContainer.addItemAfter(previousItemId);
    }

    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        return wrappedJcrContainer.addItemAfter(previousItemId, newItemId);
    }

    public Item getItem(Object itemId) {
        return wrappedJcrContainer.getItem(itemId);
    }

    public Collection<?> getContainerPropertyIds() {
        return wrappedJcrContainer.getContainerPropertyIds();
    }

    public Collection<ContainerItemId> getItemIds() {
        return (Collection<ContainerItemId>) wrappedJcrContainer.getItemIds();
    }

    public Property getContainerProperty(Object itemId, Object propertyId) {
        return wrappedJcrContainer.getContainerProperty(itemId, propertyId);
    }

    public Class<?> getType(Object propertyId) {
        return wrappedJcrContainer.getType(propertyId);
    }

    public int size() {
        return wrappedJcrContainer.size();
    }

    public boolean containsId(Object itemId) {
        return wrappedJcrContainer.containsId(itemId);
    }

    public Item addItem(Object itemId) throws UnsupportedOperationException {
        return wrappedJcrContainer.addItem(itemId);
    }

    public Object addItem() throws UnsupportedOperationException {
        return wrappedJcrContainer.addItem();
    }

    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        return wrappedJcrContainer.removeItem(itemId);
    }

    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
        return wrappedJcrContainer.addContainerProperty(propertyId, type, defaultValue);
    }

    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        return wrappedJcrContainer.removeContainerProperty(propertyId);
    }

    public boolean removeAllItems() throws UnsupportedOperationException {
        return wrappedJcrContainer.removeAllItems();
    }

    public ContainerItemId getItemByPath(String path) {
        return internalJcrContainer.getItemByPath(path);
    }

    public void fireItemSetChange() {
        internalJcrContainer.fireItemSetChange();
    }

    public javax.jcr.Item getJcrItem(ContainerItemId id) throws RepositoryException {
        return internalJcrContainer.getJcrItem(id);
    }

}
