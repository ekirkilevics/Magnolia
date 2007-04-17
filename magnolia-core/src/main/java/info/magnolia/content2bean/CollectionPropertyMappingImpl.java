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

import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.FactoryUtil;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
public class CollectionPropertyMappingImpl implements CollectionPropertyMapping {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(CollectionPropertyMappingImpl.class);

    /**
     * Property types already resolved
     */
    protected static Map propertyTypes = new HashMap();

    /**
     * Mappings to use
     */
    protected Map mapPropertyMapping = new HashMap();

    public void addCollectionPropertyClass(Class type, String name, Class mappedType) {
        mapPropertyMapping.put(type.getName() + "." + name, mappedType);
    }

    /**
     * Resolves in this order
     * <ul>
     * <li> added definitions
     * <li> definitions kept by the default transformer
     * <li> adder methods type
     * </ul>
     */
    public Class getClassForCollectionProperty(Class beanKlass, String name) {
        String key = beanKlass.getName() + "." + name;
        if (mapPropertyMapping.containsKey(key)) {
            return (Class) mapPropertyMapping.get(key);
        }

        CollectionPropertyMapping defaultTransformer = CollectionPropertyMapping.Factory.getDefaultMapping();

        if (this != defaultTransformer) {
            Class klass = defaultTransformer.getClassForCollectionProperty(beanKlass, name);
            if (klass != null) {
                return klass;
            }
        }

        Method addMethod = getAddMethod(beanKlass, name);
        if (addMethod != null) {
            Class klass = null;
            // map
            if (addMethod.getParameterTypes().length == 2) {
                klass = addMethod.getParameterTypes()[1];
            }
            else if (addMethod.getParameterTypes().length == 1) {
                klass = addMethod.getParameterTypes()[0];
            }
            if (klass != null) {
                // remember
                this.addCollectionPropertyClass(beanKlass, name, klass);
                return klass;
            }
        }

        return null;
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

    /**
     * Get a adder method. Transforms name to singular
     */
    protected Method getAddMethod(Class type, String name) {
        name = StringUtils.capitalize(name);
        Method method = getExactMethod(type, "add" + name);
        if (method == null) {
            method = getExactMethod(type, "add" + StringUtils.removeEnd(name, "s"));
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
    protected Class resolvePropertyType(Class beanClass, String propName) {
        String key = beanClass.getName() + "." + propName;
        if(propertyTypes.containsKey(key)){
            return (Class) propertyTypes.get(key);
        }

        Class klass = null;

        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(beanClass);
        for (int i = 0; i < descriptors.length; i++) {
            PropertyDescriptor descriptor = descriptors[i];
            if(descriptor.getName().equals(propName)){
                klass = descriptor.getPropertyType();
                break;
            }
        }
        if (klass == null || ClassUtil.isSubClass(klass, Map.class) || ClassUtil.isSubClass(klass, Collection.class)) {
            // a map is created
            klass = LinkedHashMap.class;
        }
        // remember me
        propertyTypes.put(key, klass);

        return klass;
    }

    public TransformationState newState() {
        return (TransformationState) FactoryUtil.newInstance(TransformationState.class);
    }
}
