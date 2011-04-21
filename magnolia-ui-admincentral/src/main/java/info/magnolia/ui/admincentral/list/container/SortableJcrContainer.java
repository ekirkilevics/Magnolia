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

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.data.Item;

import info.magnolia.ui.admincentral.tree.container.JcrContainer;
import info.magnolia.ui.admincentral.tree.container.JcrContainerSource;
/**
 *
 * Sortable jcr container.
 * @author had
 *
 */
public class SortableJcrContainer extends JcrContainer implements Container.Sortable {

    public SortableJcrContainer(JcrContainerSource jcrContainerSource) {
        super(jcrContainerSource);
    }

    public Object nextItemId(Object itemId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object prevItemId(Object itemId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object firstItemId() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object lastItemId() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isFirstId(Object itemId) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isLastId(Object itemId) {
        // TODO Auto-generated method stub
        return false;
    }

    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    public void sort(Object[] propertyId, boolean[] ascending) {
        // TODO Auto-generated method stub

    }

    public Collection<?> getSortableContainerPropertyIds() {
        // TODO Auto-generated method stub
        return null;
    }

}
