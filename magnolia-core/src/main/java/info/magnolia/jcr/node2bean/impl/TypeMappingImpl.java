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

import info.magnolia.jcr.node2bean.N2B;
import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.PropertyTypeDescriptor;
import info.magnolia.jcr.node2bean.TypeDescriptor;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.objectfactory.Components;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
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
        Method writeMethod = null;
        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.getName().equals(propName)) {
                // may be null for indexed properties
                Class<?> propertyType = descriptor.getPropertyType();
                writeMethod = descriptor.getWriteMethod();
                if (propertyType != null) {
                    dscr.setType(getTypeDescriptor(propertyType, writeMethod));
                }
                // set write method
                dscr.setWriteMethod(writeMethod);
                // set add method
                int numberOfParameters = dscr.isMap() ? 2 : 1;
                dscr.setAddMethod(getAddMethod(beanClass, propName, numberOfParameters));

                break;
            }
        }

        if (dscr.getType() != null) {
            // we have discovered type for property
            if (dscr.isMap() || dscr.isCollection()) {
                List<Class<?>> parameterTypes = new ArrayList<Class<?>>(); // this will contain collection types (for map key/value type, for collection value type)
                if (dscr.getWriteMethod() != null) {
                    parameterTypes = inferGenericTypes(dscr.getWriteMethod());
                }
                if (dscr.getAddMethod() != null && parameterTypes.size() == 0) {
                    // here we know we don't have setter or setter doesn't have parameterized type
                    // but we have add method so we take parameters from it
                    parameterTypes = Arrays.asList(dscr.getAddMethod().getParameterTypes());
                    // rather set it to null because when we are here we will use add method
                    dscr.setWriteMethod(null);
                }
                if (parameterTypes.size() > 0) {
                    // we resolved types
                    if (dscr.isMap()) {
                        dscr.setCollectionKeyType(getTypeDescriptor(parameterTypes.get(0)));
                        dscr.setCollectionEntryType(getTypeDescriptor(parameterTypes.get(1)));
                    } else {
                        // collection
                        dscr.setCollectionEntryType(getTypeDescriptor(parameterTypes.get(0)));
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

    private List<Class<?>> inferGenericTypes(Method method) {
        List<Class<?>> inferredTypes = new ArrayList<Class<?>>();
        Type[] parameterTypes = method.getGenericParameterTypes();
        for (Type parameterType : parameterTypes) {
            if (parameterType instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) parameterType;
                for (Type t : type.getActualTypeArguments()) {
                    if (t instanceof ParameterizedType) {
                        // this the case when parameterized type looks like this: Collection<List<String>>
                        // we care only for raw type List
                        inferredTypes.add((Class<?>) ((ParameterizedType) t).getRawType());
                    } else {
                        inferredTypes.add((Class<?>) t);
                    }
                }
            }
        }
        return inferredTypes;
    }

    /**
     * Resolves transformer from bean class or setter.
     */
    private Node2BeanTransformer resolveTransformer(Class<?> beanClass, Method writeMethod) throws Node2BeanException {
        if (!beanClass.isArray() && !beanClass.isPrimitive()) { // don't bother looking for a transformer if the property is an array or a primitive type
            Class<Node2BeanTransformer> transformerClass = null;
            Node2BeanTransformer transformer = null;
            if (writeMethod != null) {
                N2B transformerAnnotation = writeMethod.getAnnotation(N2B.class);
                transformerClass = transformerAnnotation == null ? null : (Class<Node2BeanTransformer>) transformerAnnotation.transformer();
                try {
                    transformer = transformerClass == null ? null : transformerClass.newInstance();
                } catch (InstantiationException e) {
                    throw new Node2BeanException("Can't instantiate transformer [" + transformerClass + "]", e);
                } catch (IllegalAccessException e) {
                    throw new Node2BeanException("Cant't instantiate transformer [" + transformerClass + "]. Is constructor visible?", e);
                }
            }
            if (transformer == null) {
                try {
                    transformerClass = (Class<Node2BeanTransformer>) Class.forName(beanClass.getName() + "Transformer");
                    transformer = Components.getComponent(transformerClass);
                } catch (ClassNotFoundException e) {
                    log.debug("No transformer found for bean [{}]", beanClass);
                }
            }
            return transformer;
        }
        return null;
    }

    /**
     * Gets type descriptor from bean class.
     */
    private TypeDescriptor getTypeDescriptor(Class<?> beanClass, Method method) {
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
        try {
            dscr.setTransformer(resolveTransformer(beanClass, method));
        } catch (Node2BeanException e) {
            log.error("Can't create transformer for bean [" + beanClass + "]", e);
        }

        types.put(beanClass, dscr);

        return dscr;
    }

    @Override
    public TypeDescriptor getTypeDescriptor(Class<?> beanClass) {
        return getTypeDescriptor(beanClass, null);
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
