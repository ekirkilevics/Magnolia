/**
 * This file Copyright (c) 2010 Magnolia International
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

import info.magnolia.module.admincentral.tree.TreeColumn;
import info.magnolia.module.admincentral.tree.TreeDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.data.Property;


/**
 * JCR <code>Node</code> Backed implementation of Vaadin <code>Property</code>.
 *
 * @author lance
 *
 */
public class NodeProperty implements Property,
        Property.ValueChangeNotifier, Serializable {

    /**
     * Event for changes of values.
     */
    protected class ValueChangeEvent extends EventObject implements
            Property.ValueChangeEvent {

        private static final long serialVersionUID = 348981570885096308L;

        private ValueChangeEvent(com.vaadin.data.Property source) {
            super(source);
        }

        public Property getProperty() {
            return (Property) getSource();
        }
    }

    private static final long serialVersionUID = -6410776877324535766L;

    private ArrayList<Property.ValueChangeListener> listeners = new ArrayList<Property.ValueChangeListener>();

    private Node node;

    private String propertyId;

    private boolean readOnly;

    private TreeDefinition definition;

    /**
     * Create a Node backed Vaadin Property.
     *
     * @param parentNode - the source Node
     * @param propId - the name of the property
     */
    public NodeProperty(Node parentNode, TreeDefinition definition, String propId) {
        super();
        this.propertyId = propId;
        this.node = parentNode;
        this.definition = definition;
    }

    public void addListener(ValueChangeListener listener) {
        listeners.add(listener);
    }

    protected void fireValueChange() {
        final com.vaadin.data.Property.ValueChangeEvent event = new ValueChangeEvent(
                this);
        for (ValueChangeListener listener : Collections
                .unmodifiableList(listeners)) {
            listener.valueChange(event);
        }
    }

    public String getPropertyId() {
        return propertyId;
    }

    public Class< ? > getType() {
        try {
            return PropertyMapper.getType(node.getProperty(propertyId));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getValue() {
        Object value = "";
        TreeColumn<?> column = definition.getColumn(propertyId);
        if (column != null) {
            try {
                value = column.getValue(node);
            }
            catch (RepositoryException e) {
                // value is not there - return empty String...
            }
        }
        return value;
        // String propertyIdToLookFor = propertyId;
        // if (propertyId.equals("Title")) {
        // propertyIdToLookFor = "title";
        // }
        // if (propertyId.equals("Page")) {
        // propertyIdToLookFor = "text";
        // }
        // try {
        // return PropertyMapper.getValue(node.getProperty(propertyIdToLookFor));
        // }
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void removeListener(ValueChangeListener listener) {
        listeners.remove(listener);
    }

    public void setReadOnly(boolean newStatus) {
        readOnly = newStatus;
    }

    public void setValue(Object newValue) throws ReadOnlyException,
            ConversionException {
        if (readOnly) {
            throw new ReadOnlyException();
        }
        try {
            TreeColumn<?> column = definition.getColumn(propertyId);
            column.setValue(node, newValue);
            fireValueChange();
        }
        catch (RepositoryException e) {
            throw new ConversionException(e);
        }
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

}