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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.data.Container;

/**
 * Abstract base class for hierarchical containers. Provides support for managing the set of properties (columns) and
 * an implementation of ItemSetChangeEvent.
 *
 * @author tmattsson
 */
public abstract class AbstractHierarchicalContainer implements Container.Hierarchical {

    private Map<String, PropertyDefinition> containerProperties = new LinkedHashMap<String, PropertyDefinition>();

    @Override
    public Collection<?> getContainerPropertyIds() {
        return Collections.unmodifiableCollection(containerProperties.keySet());
    }

    @Override
    public Class<?> getType(Object propertyId) {
        return containerProperties.get(propertyId).getType();
    }

    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
        containerProperties.put((String) propertyId, new PropertyDefinition((String) propertyId, type, defaultValue));
        return true;
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        containerProperties.remove(propertyId);
        return true;
    }

    /**
     * ImplementationConfiguration of item set change event.
     */
    protected class ItemSetChangeEvent implements Container.ItemSetChangeEvent {

        /**
         * Gets the Property where the event occurred.
         *
         * @see com.vaadin.data.Container.ItemSetChangeEvent#getContainer()
         */
        @Override
        public Container getContainer() {
            return AbstractHierarchicalContainer.this;
        }

    }
}
