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
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

import com.vaadin.data.Property;

/**
 * Represents a property on an item in JcrContainer. Think of this as a cell in a table.
 *
 * Note: Vaadin calls the toString() method to get the value to display in the TreeTable.
 *
 * @author tmattsson
 */
public class JcrContainerProperty implements Property, Property.ValueChangeNotifier {

    private String propertyId;
    private ContainerItemId itemId;
    private JcrContainer container;
    private boolean readOnly = false;
    private List<ValueChangeListener> listeners = new ArrayList<Property.ValueChangeListener>();

    public JcrContainerProperty(String propertyId, ContainerItemId itemId, JcrContainer container) {
        this.propertyId = propertyId;
        this.itemId = itemId;
        this.container = container;
    }

    public Object getValue() {
        return container.getColumnValue(propertyId, itemId);
    }

    public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
        container.setColumnValue(propertyId, itemId, newValue);
        fireValueChange();
    }

    public Class<?> getType() {
        return container.getType(propertyId);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean newStatus) {
        readOnly = newStatus;
    }

    public void addListener(ValueChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ValueChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String toString() {
        Object value = getValue();
        return value != null ? value.toString() : "";
    }

    protected void fireValueChange() {
        ValueChangeEvent event = new ValueChangeEvent(this);
        for (ValueChangeListener listener : Collections .unmodifiableList(listeners)) {
            listener.valueChange(event);
        }
    }

    /**
     * Event for changes of values.
     */
    protected static class ValueChangeEvent extends EventObject implements Property.ValueChangeEvent {

        private static final long serialVersionUID = 348981570885096308L;

        private ValueChangeEvent(com.vaadin.data.Property source) {
            super(source);
        }

        public Property getProperty() {
            return (Property) getSource();
        }
    }
}
