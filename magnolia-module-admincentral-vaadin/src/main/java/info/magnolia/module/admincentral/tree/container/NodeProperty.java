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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.admincentral.tree.TreeColumn;
import info.magnolia.module.admincentral.tree.TreeDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;

import javax.jcr.RepositoryException;

import com.vaadin.data.Property;


/**
 * Mgnl <code>Content</code> backed implementation of Vaadin <code>Property</code>.
 *
 * @author daniellipp
 * @version $Id$ *
 */
public class NodeProperty implements Property,
        Property.ValueChangeNotifier, Serializable {

    /**
     * ValueChangeEvent.
     */
    protected static class ValueChangeEvent extends EventObject implements
            Property.ValueChangeEvent {

        private static final long serialVersionUID = 1547153927026681707L;

        private ValueChangeEvent(Property source) {
            super(source);
        }

        public Property getProperty() {
            return (Property) getSource();
        }
    }

    private static final long serialVersionUID = -6410776877324535766L;

    private TreeDefinition definition;

    private ArrayList<Property.ValueChangeListener> listeners = new ArrayList<Property.ValueChangeListener>();

    private Content node;

    private String propertyId;

    private boolean readOnly;

    /**
     * Create a Node backed Vaadin Property.
     *
     * @param parentNode - the source Node
     * @param propId - the name of the property
     */
    public NodeProperty(Content parentNode, String propId, TreeDefinition definition) {
        super();
        propertyId = propId;
        node = parentNode;
        this.definition = definition;
    }

    public void addListener(ValueChangeListener listener) {
        listeners.add(listener);
    }

    protected void fireValueChange() {
        final Property.ValueChangeEvent event = new ValueChangeEvent(
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
            return NodeDataUtil.getValueObject(node.getNodeData(propertyId)).getClass();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getValue() {
        // make use of treeColumns - they know how to retrieve property values...
        for (TreeColumn column : definition.getColumns()) {
            if (column.getLabel().equals(propertyId)) {
                   return column.getValue(node);
            }
        }
        return null;
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

    /**
     * TODO: will not work like that - compare with getValue()...
     */
    public void setValue(Object newValue) throws ReadOnlyException,
            ConversionException {
        if (readOnly) {
            throw new ReadOnlyException();
        }
        try {
            node.setNodeData(propertyId, newValue);
            fireValueChange();
        }
        catch (RepositoryException e) {
            throw new ConversionException(e);
        }
    }

    @Override
    public String toString() {
        return "" + getValue();
    }

}
