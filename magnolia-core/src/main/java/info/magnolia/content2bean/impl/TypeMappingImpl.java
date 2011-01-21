/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.content2bean.impl;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.TypeMapping;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import info.magnolia.objectfactory.ClassFactory;
import info.magnolia.objectfactory.Classes;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Basic type mapping implementation.
 * @author philipp
 * @version $Id$
 *
 */
public class TypeMappingImpl implements TypeMapping {

    private static Logger log = LoggerFactory.getLogger(TypeMappingImpl.class);

    /**
     * Property types already resolved.
     */
    private final Map<String, PropertyTypeDescriptor> propertyTypes = new HashMap<String, PropertyTypeDescriptor>();

    /**
     * Descriptors for types.
     **/
    private final Map<Class<?>, TypeDescriptor> types = new HashMap<Class<?>, TypeDescriptor>();

    /**
     * Get a adder method. Transforms name to singular.
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
     * Cache the already resolved types.
     *
     */
    public PropertyTypeDescriptor getPropertyTypeDescriptor(Class<?> beanClass, String propName) {
        PropertyTypeDescriptor dscr;
        String key = beanClass.getName() + "." + propName;

        dscr = propertyTypes.get(key);
        if(dscr != null){
            return dscr;
        }

        //TypeMapping defaultMapping = TypeMapping.Factory.getDefaultMapping();
        // TODO - is this used - or is the comparison correct ?
//        if (this != defaultMapping) {
//            dscr = defaultMapping.getPropertyTypeDescriptor(beanClass, propName);
//            if (dscr.getType()  != null) {
//                return dscr;
//            }
//        }

        dscr = new PropertyTypeDescriptor();
        dscr.setName(propName);

        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(beanClass);
        for (int i = 0; i < descriptors.length; i++) {
            PropertyDescriptor descriptor = descriptors[i];
            if (descriptor.getName().equals(propName)) {
                Class<?> propertytype = descriptor.getPropertyType(); // may be null for indexed properties
                if (propertytype != null) {
                    dscr.setType(getTypeDescriptor(propertytype));
                }
                break;
            }
        }

        if(dscr.getType() != null){
            if(dscr.isMap() || dscr.isCollection()){
                int numberOfParameters = dscr.isMap() ? 2 : 1;
                Method method = getAddMethod(beanClass, propName, numberOfParameters);
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

    public void addPropertyTypeDescriptor(Class<?> beanClass, String propName, PropertyTypeDescriptor dscr) {
        propertyTypes.put(beanClass.getName() + "." + propName, dscr);
    }

    public void addTypeDescriptor(Class<?> beanClass, TypeDescriptor dscr) {
        types.put(beanClass, dscr);
    }

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
            Content2BeanTransformer transformer = null; // TODO ? transformerProvider.getTransformerFor(beanClass);
            try {
                if (transformer == null) {
                    transformer = findTransformerByNamingConvention(beanClass);
                }
                if (transformer == null) {
                    transformer = findTransformerViaProperty(beanClass);
                }
            } catch (Exception e) {
                // this is fine because having a transformer class is optional
                log.debug("No custom transformer class {}Transformer class found", beanClass.getName());
            }
            dscr.setTransformer(transformer);
        }
        return dscr;
    }

    /**
     * @deprecated since 5.0, transformers should be explicitly registered via the module descriptor.
     */
    protected Content2BeanTransformer findTransformerByNamingConvention(Class<?> beanClass) {
        final String transformerClassName = beanClass.getName() + "Transformer";
        try {
            return instantiateTransformer(beanClass, transformerClassName);
        } catch (ClassNotFoundException e) {
            log.debug("No transformer found by naming convention for {} (attempted to load {})", beanClass, transformerClassName);
            return null;
        }
    }

    /**
     * This was originally implemented by info.magnolia.content2bean.impl.PropertiesBasedTypeMapping
     * @deprecated since 5.0, transformers should be explicitly registered via the module descriptor.
     */
    protected Content2BeanTransformer findTransformerViaProperty(Class<?> beanClass) throws ClassNotFoundException {
        final String property = SystemProperty.getProperty(beanClass.getName() + ".transformer");
        if (property != null) {
            return instantiateTransformer(beanClass,property);
        }
        return null;
    }

    protected Content2BeanTransformer instantiateTransformer(Class<?> beanClass, String transformerClassName) throws ClassNotFoundException {
        final ClassFactory classFactory = Classes.getClassFactory();
        final Class<Content2BeanTransformer> transformerClass = classFactory.forName(transformerClassName);

        if (Content2BeanTransformer.class.isAssignableFrom(transformerClass)) {
            try {
                log.debug("Found a custom transformer [{" + transformerClass + "}] for [{" + beanClass + "}]");
                // TODO use components ?
                return classFactory.newInstance(transformerClass);
            } catch (Exception e) {
                log.error("Can't instantiate custom transformer [{" + transformerClass + "}] for [{" + beanClass + "}]", e);
            }
        }
        return null;
    }

    /**
     * Find a method.
     * @param numberOfParameters
     */
    protected Method getExactMethod(Class<?> type, String name, int numberOfParameters) {
        Method[] methods = type.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(name)) {
                if(method.getParameterTypes().length == numberOfParameters){
                    return method;
                }
            }
        }
        return null;
    }

}
