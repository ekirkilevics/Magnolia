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
package info.magnolia.ui.admincentral.column;

import info.magnolia.ui.model.column.definition.AbstractColumnDefinition;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import com.vaadin.ui.Component;

/**
 * Defines a Column - e.g. for lists or trees.
 *
 * @param <D>
 *            type of the definition for this column.
 * @author dlipp
 */
public interface Column<D extends AbstractColumnDefinition> {

    D getDefinition();

    /**
     * @return value to be displayed in the corresponding column (from the provided Node)
     */
    Component getComponent(Item item) throws RepositoryException;

    void setComponent(Item item, Component newValue) throws RepositoryException;

    int getWidth();

    void setWidth(int newWidth);

    String getLabel();

    void setLabel(String newLabel);
}
