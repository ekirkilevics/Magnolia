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

import info.magnolia.ui.admincentral.container.ContainerItemId;
import info.magnolia.ui.admincentral.container.JcrContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jcr.RepositoryException;

import com.vaadin.data.Container;
import com.vaadin.data.util.ContainerOrderedWrapper;
import com.vaadin.data.util.ItemSorter;

/**
 * TODO: review.
 * Sortable jcr container wrapper.
 * @author had
 *
 */
public class SortableJcrContainerWrapper extends ContainerOrderedWrapper implements Container.Sortable {

    private JcrContainer wrappedJcrContainer;
    private ItemSorter itemSorter;

    public SortableJcrContainerWrapper(JcrContainer toBeWrapped, ItemSorter sorter) {
        super(toBeWrapped);
        wrappedJcrContainer = toBeWrapped;
        itemSorter = sorter;
    }

    public ItemSorter getItemSorter() {
        return itemSorter;
    }

    public void setItemSorter(ItemSorter itemSorter) {
        this.itemSorter = itemSorter;
    }

    public void sort(Object[] propertyId, boolean[] ascending) {
        // Set up the item sorter for the sort operation
        itemSorter.setSortProperties(this, propertyId, ascending);

        // Perform the actual sort
        doSort();
    }

    public Collection<?> getSortableContainerPropertyIds() {
        //delegate determining the actual sortable properties to a custom ItemSorter
        return ((ConfigurableItemSorter)itemSorter).getSortablePropertyIds();
    }

    /**
     * Perform the sorting of the data structures in the container. This is
     * invoked when the <code>itemSorter</code> has been prepared for the sort
     * operation. Typically this method calls
     * <code>Collections.sort(aCollection, getItemSorter())</code> on all arrays
     * (containing item ids) that need to be sorted.
     *
     */
    protected void doSort() {
        List<ContainerItemId> a = new ArrayList<ContainerItemId>(wrappedJcrContainer.getItemIds());
        //FIXME this is for testing only. Sorting does not work now as we need access to the underlying collection of id items
        System.out.println("before " +a);
        Collections.sort(a, getItemSorter());
        System.out.println("after " +a);
    }

    public ContainerItemId getItemByPath(String path) {
        return wrappedJcrContainer.getItemByPath(path);
    }

    public void fireItemSetChange() {
        wrappedJcrContainer.fireItemSetChange();
    }

    public javax.jcr.Item getJcrItem(ContainerItemId id) throws RepositoryException {
        return wrappedJcrContainer.getJcrItem(id);
    }

}
