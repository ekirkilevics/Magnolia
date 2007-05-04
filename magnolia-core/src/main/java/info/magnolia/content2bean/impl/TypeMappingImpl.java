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
package info.magnolia.content2bean.impl;

import info.magnolia.cms.util.ClassUtil;
import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.TypeMapping;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class TypeMappingImpl implements TypeMapping {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(TypeMappingImpl.class);

    /**
     * Property types already resolved
     */
    protected static Map propertyTypes = new HashMap();

    /**
     * Descriptors for types
     **/
    protected static Map types = new HashMap();

    /**
     * Get a adder method. Transforms name to singular
     */
    public Method getAddMethod(Class type, String name) {
        name = StringUtils.capitalize(name);
        Method method = getExactMethod(type, "add" + name);
        if (method == null) {
            method = getExactMethod(type, "add" + StringUtils.removeEnd(name, "s"));
        }

        if (method == null) {
            method = getExactMethod(type, "add" + StringUtils.removeEnd(name, "es"));
        }

        if (method == null) {
            method = getExactMethod(type, "add" + StringUtils.removeEnd(name, "ren"));
        }

        if (method == null) {
            method = getExactMethod(type, "add" + StringUtils.removeEnd(name, "ies") + "y");
        }
        return method;
    }

    /**
     * Cache the already resolved types
     *
     */
    public PropertyTypeDescriptor getPropertyTypeDescriptor(Class beanClass, String propName) {
        PropertyTypeDescriptor dscr;
        String key = beanClass.getName() + "." + propName;

        dscr = (PropertyTypeDescriptor) propertyTypes.get(key);
        if(dscr != null){
            return dscr;
        }
        TypeMapping defaultMapping = TypeMapping.Factory.getDefaultMapping();

        if (this != defaultMapping) {
            dscr = defaultMapping.getPropertyTypeDescriptor(beanClass, propName);
            if (dscr.getType()  != null) {
                return dscr;
            }
        }

        dscr = new PropertyTypeDescriptor();
        dscr.setName(propName);

        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(beanClass);
        for (int i = 0; i < descriptors.length; i++) {
            PropertyDescriptor descriptor = descriptors[i];
            if(descriptor.getName().equals(propName)){
                dscr.setType(getTypeDescriptor(descriptor.getPropertyType()));
                break;
            }
        }

        if(dscr.getType() != null){
            if(dscr.isMap() || dscr.isCollection()){
                Method method = getAddMethod(beanClass, propName);
                if(method != null){
                    dscr.setAddMethod(method);
                    if(dscr.isMap()){
                        dscr.setCollectionKeyType(getTypeDescriptor(method.getParameterTypes()[0]));
                        dscr.setCollectionEntryType(getTypeDescriptor(method.getParameterTypes()[1]));
                    }
                    else{
                        dscr.setCollectionEntryType(getTypeDescriptor(method.getParameterTypes()[0]));
                    }
                }
            }
        }

        // remember me
        propertyTypes.put(key, dscr);

        return dscr;
    }

    public void addPropertyTypeDescriptor(Class beanClass, String propName, PropertyTypeDescriptor dscr) {
        propertyTypes.put(beanClass.getName() + "." + propName, dscr);
    }

    public void addTypeDescriptor(Class beanClass, TypeDescriptor dscr) {
        types.put(beanClass, dscr);
    }

    public TypeDescriptor getTypeDescriptor(Class beanClass) {
        TypeDescriptor dscr = (TypeDescriptor) types.get(beanClass);
        if(dscr != null){
            return dscr;
        }
        dscr = new TypeDescriptor();
        dscr.setType(beanClass);
        dscr.setMap(ClassUtil.isSubClass(beanClass, Map.class));
        dscr.setCollection(beanClass.isArray() || ClassUtil.isSubClass(beanClass, Collection.class));

        types.put(beanClass, dscr);
        return dscr;
    }

    /**
     * Find a method
     */
    protected Method getExactMethod(Class type, String name) {
        Method[] methods = type.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

}
