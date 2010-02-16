/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.content2bean;

import info.magnolia.cms.core.ItemType;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;


/**
 * @author philipp
 * @version $Id$
 * @todo
 */
public class TypeDescriptor {

    private Class<?> type;

    private ItemType itemType;

    private Content2BeanTransformer transformer;

    private boolean isMap;

    private boolean isCollection;

    private Map<String, PropertyTypeDescriptor> descriptors;

    public ItemType getItemType() {
        return this.itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public Class<?> getType() {
        return this.type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }


    public boolean isCollection() {
        return this.isCollection;
    }


    public void setCollection(boolean isCollection) {
        this.isCollection = isCollection;
    }


    public boolean isMap() {
        return this.isMap;
    }


    public void setMap(boolean isMap) {
        this.isMap = isMap;
    }

    public PropertyTypeDescriptor getPropertyTypeDescriptor(String properyName) {
        return getPropertyDescriptors().get(properyName);
    }

    /**
     * @return true if this descriptor represents an map or collection, without a concrete type thereof.
     */
    public boolean needsDefaultMapping() {
        return (isMap() || isCollection()) && (getType().isInterface() || getType().isArray());
    }

    /**
     * This method is not synchronized to avoid thread blocking, but the method guarantees that the returned map is not mutated afterward.
     */
    public Map<String, PropertyTypeDescriptor> getPropertyDescriptors() {
        if(this.descriptors == null){
            // TODO this breaks the usage of a custom mapping
            TypeMapping mapping = TypeMapping.Factory.getDefaultMapping();

            // for not making this method synchronized we create a local variable first
            // this guarantees that the map you get is not changed after return
            final Map<String, PropertyTypeDescriptor> tmpDescriptors = new HashMap<String, PropertyTypeDescriptor>();
            PropertyDescriptor[] dscrs = PropertyUtils.getPropertyDescriptors(this.getType());
            for (int i = 0; i < dscrs.length; i++) {
                PropertyDescriptor descriptor = dscrs[i];
                tmpDescriptors.put(descriptor.getName(), mapping.getPropertyTypeDescriptor(this.getType(), descriptor.getName()));
            }

            this.descriptors = Collections.unmodifiableMap(tmpDescriptors);
        }
        return this.descriptors;
    }

    /**
     * Can return a custom transformer. Otherwise null.
     */
    public Content2BeanTransformer getTransformer() {
        return transformer;
    }

    public void setTransformer(Content2BeanTransformer transformer) {
        this.transformer = transformer;
    }

}
