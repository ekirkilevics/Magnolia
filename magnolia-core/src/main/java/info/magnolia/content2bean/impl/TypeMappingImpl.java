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
package info.magnolia.content2bean.impl;

import info.magnolia.cms.util.ClassUtil;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.TypeMapping;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import info.magnolia.objectfactory.Classes;
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
    protected static Map<String, PropertyTypeDescriptor> propertyTypes = new HashMap<String, PropertyTypeDescriptor>();

    /**
     * Descriptors for types
     **/
    protected static Map<Class<?>, TypeDescriptor> types = new HashMap<Class<?>, TypeDescriptor>();

    /**
     * Get a adder method. Transforms name to singular
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
     * Cache the already resolved types
     *
     */
    public PropertyTypeDescriptor getPropertyTypeDescriptor(Class<?> beanClass, String propName) {
        PropertyTypeDescriptor dscr;
        String key = beanClass.getName() + "." + propName;

        dscr = propertyTypes.get(key);
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
        if(dscr != null){
            return dscr;
        }
        dscr = new TypeDescriptor();
        dscr.setType(beanClass);
        dscr.setMap(Map.class.isAssignableFrom(beanClass));
        dscr.setCollection(beanClass.isArray() || Collection.class.isAssignableFrom(beanClass));
        types.put(beanClass, dscr);
        try {
            Class<?> transformerClass = Classes.getClassFactory().forName(beanClass.getName() + "Transformer");

            if(Content2BeanTransformer.class.isAssignableFrom(transformerClass)){
                try {
                    Content2BeanTransformer transformer = (Content2BeanTransformer) transformerClass.newInstance();
                    dscr.setTransformer(transformer);
                    log.debug("Found a custom transformer [{" + transformerClass + "}] for [{" + beanClass + "}]");
                } catch (Exception e) {
                    log.error("Can't instantiate custom transformer [{" + transformerClass + "}] for [{" + beanClass + "}]", e);
                }
            }
        } catch (Exception e) {
            // this is fine because having a transformer class is optional
            log.debug("No custom transformer class {}Transformer class found", beanClass.getName());
        }

        return dscr;
    }

    /**
     * Find a method
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
