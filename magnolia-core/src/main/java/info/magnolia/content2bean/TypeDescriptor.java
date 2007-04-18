/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.content2bean;

import info.magnolia.cms.core.ItemType;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;


/**
 * @author philipp
 * @version $Id$
 * @todo
 */
public class TypeDescriptor {

    private Class type;

    private ItemType itemType;

    private boolean isMap;

    private boolean isCollection;

    private Map descriptors;

    public ItemType getItemType() {
        return this.itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public Class getType() {
        return this.type;
    }

    public void setType(Class type) {
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

    public PropertyTypeDescriptor getPropertyTypeDescriptor(String mapProperyName) {
        return (PropertyTypeDescriptor) getPropertyDescriptors().get(mapProperyName);
    }

    /**
     *
     */
    public Map getPropertyDescriptors() {
        if(descriptors == null){
            // TODO this breaks the usage of a custom mapping
            TypeMapping mapping = TypeMapping.Factory.getDefaultMapping();

            descriptors = new HashMap();
            PropertyDescriptor[] dscrs = PropertyUtils.getPropertyDescriptors(this.getType());
            for (int i = 0; i < dscrs.length; i++) {
                PropertyDescriptor descriptor = dscrs[i];
                descriptors.put(descriptor.getName(), mapping.getPropertyTypeDescriptor(this.getType(), descriptor.getName()));

            }
        }
        return descriptors;
    }

}
