/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.jcr.node2bean.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.jcr.node2bean.PropertyTypeDescriptor;
import info.magnolia.jcr.node2bean.TypeDescriptor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.objectfactory.Components;

/**
 * Basic type mapping implementation.
 */
public class TypeMappingImpl implements TypeMapping {

    private static Logger log = LoggerFactory.getLogger(TypeMappingImpl.class);

    private final Map<String, PropertyTypeDescriptor> propertyTypes = new HashMap<String, PropertyTypeDescriptor>();
    private final Map<Class<?>, TypeDescriptor> types = new HashMap<Class<?>, TypeDescriptor>();

    @Override
    public PropertyTypeDescriptor getPropertyTypeDescriptor(Class<?> beanClass, String propName) {
        PropertyTypeDescriptor dscr = null;
        String key = beanClass.getName() + "." + propName;

        dscr = propertyTypes.get(key);

        if (dscr != null) {
            return dscr;
        }

        dscr = new PropertyTypeDescriptor();
        dscr.setName(propName);

        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(beanClass);
        String methodName = null;

        for (int i = 0; i < descriptors.length; i++) {
            PropertyDescriptor descriptor = descriptors[i];
            if (descriptor.getName().equals(propName)) {
                Class<?> propertytype = descriptor.getPropertyType(); // may be null for indexed properties
                if (propertytype != null) {
                    dscr.setType(getTypeDescriptor(propertytype));
                }
                methodName = descriptor.getWriteMethod().getName();
                break;
            }
        }

        if (dscr.getType() != null) {
            if (dscr.isMap() || dscr.isCollection()) {
                Class<?> classType = dscr.isMap() ? Map.class : Collection.class;
                if (methodName != null) { // TODO log warn/error if method name is null?
                    Method method = null;
                    Type[] parameterArgs = new Type[] {};
                    try {
                        method = beanClass.getMethod(methodName, classType);
                        Type[] parameterTypes = method.getGenericParameterTypes();
                        for (Type genericParameterType : parameterTypes) {
                            if (genericParameterType instanceof ParameterizedType) {
                                ParameterizedType type = (ParameterizedType) genericParameterType;
                                parameterArgs = type.getActualTypeArguments();
                            }
                        }

                        if (parameterArgs.length > 0) {
                            if (dscr.isMap()) {
                                dscr.setCollectionKeyType(getTypeDescriptor((Class<?>) parameterArgs[0]));
                                dscr.setCollectionEntryType(getTypeDescriptor((Class<?>) parameterArgs[1]));
                            } else if (dscr.isCollection()){
                                dscr.setCollectionEntryType(getTypeDescriptor((Class<?>) parameterArgs[0]));
                            }
                        }

                    } catch (NoSuchMethodException e1) {
                        // TODO just log a warning or ... ?
                    }
                }
            }
        }
        propertyTypes.put(key, dscr);

        return dscr;
    }

    @Override
    public TypeDescriptor getTypeDescriptor(Class<?> beanClass) {
        TypeDescriptor dscr = types.get(beanClass);
        // eh, we know about this type, don't bother resolving any further.
        if(dscr != null){
            return dscr;
        }
        dscr = new TypeDescriptor();
        dscr.setType(beanClass);
        dscr.setMap(Map.class.isAssignableFrom(beanClass));
        dscr.setCollection(beanClass.isArray() || Collection.class.isAssignableFrom(beanClass));
        types.put(beanClass, dscr);

        if (!beanClass.isArray() && !beanClass.isPrimitive()) { // don't bother looking for a transformer if the property is an array or a primitive type
            Node2BeanTransformer transformer = null;
            try {
                Class<?> clazz = Class.forName(beanClass.getName() + "Transformer");
                transformer = (Node2BeanTransformer) Components.getComponent(clazz);
            } catch (Exception e) {
                log.debug("No custom transformer class {}Transformer class found", beanClass.getName());
            }
            dscr.setTransformer(transformer);
        }
        return dscr;
    }

}
