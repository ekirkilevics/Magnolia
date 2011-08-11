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
package info.magnolia.ui.admincentral.container;

import com.vaadin.data.util.AbstractProperty;
import com.vaadin.ui.Label;


/**
 * Represents a property on an item in JcrContainer. Think of this as a cell in a table. This
 * implementation refers back to the container that created the item to retrieve the value via
 * {@link JcrContainer#getColumnValue(String, Object)}.
 * 
 * Note: Vaadin calls the toString() method to get the value to display in the TreeTable.
 * 
 * @author tmattsson
 */
public class JcrContainerProperty extends AbstractProperty {

    private String propertyId;
    private Object itemId;
    private JcrContainer container;
    private boolean readOnly = false;

    public JcrContainerProperty(String propertyId, Object itemId, JcrContainer container) {
        this.propertyId = propertyId;
        this.itemId = itemId;
        this.container = container;
        setReadOnly(false);
    }

    @Override
    public Object getValue() {
        Object columnValue = container.getColumnValue(propertyId, itemId);
        return (columnValue instanceof Label && ((Label) columnValue).getContentMode() == Label.CONTENT_TEXT)
                ? " " + ((Label) columnValue).getValue()
                : columnValue;
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
        container.setColumnValue(propertyId, itemId, newValue);
        fireValueChange();
    }

    @Override
    public Class<?> getType() {
        return container.getType(propertyId);
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadOnly(boolean newStatus) {
        readOnly = newStatus;
    }

    @Override
    public String toString() {
        Object value = getValue();
        return value != null ? value.toString() : "";
    }
}
