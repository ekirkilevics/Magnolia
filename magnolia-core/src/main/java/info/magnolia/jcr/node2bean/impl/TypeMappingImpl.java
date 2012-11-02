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

import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.PropertyTypeDescriptor;
import info.magnolia.jcr.node2bean.TypeDescriptor;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.objectfactory.Components;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.getName().equals(propName)) {
                // may be null for indexed properties
                Class<?> propertyType = descriptor.getPropertyType();
                if (propertyType != null) {
                    dscr.setType(getTypeDescriptor(propertyType));
                }
                // set write method
                dscr.setWriteMethod(descriptor.getWriteMethod());
                // set add method
                int numberOfParameters = dscr.isMap() ? 2 : 1;
                dscr.setAddMethod(getAddMethod(beanClass, propName, numberOfParameters));

                break;
            }
        }

        if (dscr.getType() != null) {
            // we have discovered type for property
            if (dscr.isMap() || dscr.isCollection()) {
                Type[] typeArgs = new Type[] {}; // this will contain collection types (for map key/value type, for collection value type)
                if (dscr.getWriteMethod() != null) {
                    // now we have to find out if generics are set
                    Type[] parameterTypes = dscr.getWriteMethod().getGenericParameterTypes();
                    if (parameterTypes.length > 0) {
                        // we have parameter with generics
                        for (Type genericParameterType : parameterTypes) {
                            // check if we have parameterized type e.g. Collection<String>
                            if (genericParameterType instanceof ParameterizedType) {
                                ParameterizedType type = (ParameterizedType) genericParameterType;
                                for (Type t : type.getActualTypeArguments()) {
                                    if (t instanceof ParameterizedType) {
                                        // this the case when parameterized type looks like this: Collection<List<String>>
                                        // we care only for raw type List
                                        typeArgs = (Type[]) ArrayUtils.add(typeArgs, ((ParameterizedType) t).getRawType());
                                    } else {
                                        typeArgs = (Type[]) ArrayUtils.add(typeArgs, t);
                                    }
                                }
                            }
                        }
                    }
                }
                if (dscr.getAddMethod() != null && typeArgs.length == 0) {
                    // here we know we don't have setter or setter doesn't have parameterized type
                    // but we have add method so we take parameters from it
                    typeArgs = dscr.getAddMethod().getParameterTypes();
                    // rather set it to null because when we are here we will use add method
                    dscr.setWriteMethod(null);
                }
                if (typeArgs.length > 0) {
                    // we resolved types
                    if (dscr.isMap()) {
                        dscr.setCollectionKeyType(getTypeDescriptor((Class<?>) typeArgs[0]));
                        dscr.setCollectionEntryType(getTypeDescriptor((Class<?>) typeArgs[1]));
                    } else {
                        // collection
                        dscr.setCollectionEntryType(getTypeDescriptor((Class<?>) typeArgs[0]));
                    }
                }
            } else if (dscr.isArray()) {
                // for arrays we don't need to discover its parameter from set/add method
                // we just take it via Class#getComponentType() method
                dscr.setCollectionEntryType(getTypeDescriptor(dscr.getType().getType().getComponentType()));
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
        dscr.setCollection(Collection.class.isAssignableFrom(beanClass));
        dscr.setArray(beanClass.isArray());
        types.put(beanClass, dscr);

        if (!beanClass.isArray() && !beanClass.isPrimitive()) { // don't bother looking for a transformer if the property is an array or a primitive type
            Node2BeanTransformer transformer = null;
            try {
                @SuppressWarnings("unchecked")
                Class<Node2BeanTransformer> clazz = (Class<Node2BeanTransformer>) Class.forName(beanClass.getName() + "Transformer");
                transformer = Components.getComponent(clazz);
            } catch (Exception e) {
                log.debug("No custom transformer class {}Transformer class found", beanClass.getName());
            }
            dscr.setTransformer(transformer);
        }
        return dscr;
    }

    /**
     * Get a adder method. Transforms name to singular.
     * @deprecated since 5.0 - use setters
     */
    public Method getAddMethod(Class<?> type, String name, int numberOfParameters) {
        name = StringUtils.capitalize(name);
        Method method = getExactMethod(type, "add" + name, numberOfParameters);
        if (method == null) {
            method = getExactMethod(type, "add" + StringUtils.removeEnd(name, "s"), numberOfParameters);
        }

        if (method == null) {
            method = getExactMethod(type, "add" + StringUtils.removeEnd(name, "es"), numberOfParameters);
        }

        if (method == null) {
            method = getExactMethod(type, "add" + StringUtils.removeEnd(name, "ren"), numberOfParameters);
        }

        if (method == null) {
            method = getExactMethod(type, "add" + StringUtils.removeEnd(name, "ies") + "y", numberOfParameters);
        }
        return method;
    }

    /**
     * Find a method.
     *
     * @param numberOfParameters
     * @deprecated since 5.0 - use setters
     */
    protected Method getExactMethod(Class<?> type, String name, int numberOfParameters) {
        Method[] methods = type.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(name)) {
                // TODO - CAUTION: in case there's several methods with the same
                // name and the same numberOfParameters
                // this method might pick the "wrong" one. We should think about
                // adding a check and throw an exceptions
                // if there's more than one match!
                if (method.getParameterTypes().length == numberOfParameters) {
                    return method;
                }
            }
        }
        return null;
    }

}
