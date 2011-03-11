/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ui.admincentral.tree.column;

import info.magnolia.ui.admincentral.tree.container.JcrContainer;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import com.vaadin.ui.Field;

/**
 * Base class for tree columns.
 *
 * @param <E> type of the hosted values of this column.
 * @author dlipp
 * @author tmattsson
 */
public abstract class TreeColumn<E> {

    /**
     * @return Field used when editing this column. Defaults to null.
     */
    public Field getEditField(Item item) {
        return null;
    }

    /**
     * Type of the column: Subclasses have to make sure the getValue methods return instances of
     * this type!
     */
    public abstract Class<E> getType();

    /**
     * @return value to be displayed in the corresponding column (from the provided Node)
     */
    public abstract Object getValue(Item item) throws RepositoryException;

    /**
     * Set value of Property for the provided node to the new value.
     */
    public void setValue(JcrContainer jcrContainer, Item item, Object newValue) throws RepositoryException {
    }
}
