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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.TypeMapping;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Concrete implementation using reflection and adder methods.
 * @author philipp
 * @version $Id$
 */
public class Content2BeanTransformerImpl implements Content2BeanTransformer {

    private static Logger log = LoggerFactory.getLogger(Content2BeanTransformerImpl.class);

    /**
     * Resolves in this order
     * <ul>
     * <li> checks the class property of the current node
     * <li> calls onResolve subclasses should override
     * <li> reflection on the parent bean
     * <li> in case of a collection/map type call getClassForCollectionProperty
     * <li> otherwise use a Map
     * </ul>
     */
    public TypeDescriptor resolveType(TransformationState state) throws ClassNotFoundException {
        TypeDescriptor typeDscr = null;
        Content node = state.getCurrentContent();

        try {
            if (node.hasNodeData("class")) {
                String className = node.getNodeData("class").getString();
                Class clazz = ClassUtil.classForName(className);
                typeDscr = getTypeMapping().getTypeDescriptor(clazz);
            }
            else {
                typeDscr = onResolveClass(state);
            }
        }
        catch (RepositoryException e) {
            // ignore
            Content2BeanProcessorImpl.log.warn("can't read class property", e);
        }

        if (typeDscr == null && state.getLevel() > 1) {
            TypeDescriptor parentTypeDscr = state.getCurrentType();
            PropertyTypeDescriptor propDscr;

            if (parentTypeDscr.isMap() || parentTypeDscr.isCollection()) {
                if (state.getLevel() > 2) {
                    // this is not necesserely the parent node of the current
                    String mapProperyName = state.peekContent(1).getName();
                    propDscr = state.peekType(1).getPropertyTypeDescriptor(mapProperyName);
                    typeDscr = propDscr.getCollectionEntryType();
                }
            }
            else {
                propDscr = state.getCurrentType().getPropertyTypeDescriptor(node.getName());
                if(propDscr != null){
                    typeDscr = propDscr.getType();
                }
            }
        }

        if (typeDscr == null || typeDscr.isMap() || typeDscr.isCollection()) {
            if(typeDscr== null && log.isDebugEnabled()){
                log.debug("was not able to resolve type for node []", node );
            }
            typeDscr = TypeMapping.MAP_TYPE;
        }

        return typeDscr;
    }

    /**
     * Override for custom resolving. In case this method is called no class property was set on the node
     */
    protected TypeDescriptor onResolveClass(TransformationState state) {
        return null;
    }

    /**
     * Process all nodes except metadata
     */
    public boolean accept(Content content) {
        return ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER.accept(content);
    }

    /**
     * Do not set class property. In case of a map/collection try to use adder method.
     */
    public void setProperty(TransformationState state, PropertyTypeDescriptor descriptor, Map values) {
        String propertyName = descriptor.getName();
        if(propertyName.equals("class")){
            return;
        }
        Object value = values.get(propertyName);
        TypeMapping mapping = getTypeMapping();
        Object bean = state.getCurrentBean();

        if (propertyName.equals("content") && value == null) {
            value = state.getCurrentContent();
        }

        else if (propertyName.equals("name") && value == null) {
            value = state.getCurrentContent().getName();
        }

        else if (propertyName.equals("className") && value == null) {
            value = values.get("class");
        }

        // do no try to set a bean-property that has no correspoding node-property
        //else if (!values.containsKey(propertyName)) {
        if (value == null) {
            return;
        }

        // if the parent bean is a map, we can't guess the types.
        if (!(bean instanceof Map)) {
            try {
                PropertyTypeDescriptor dscr = mapping.getPropertyTypeDescriptor(bean.getClass(), propertyName);
                if (dscr.getType() != null) {

                    // try to use an adder method for a Collection property of the bean
                    if (dscr.isCollection() || dscr.isMap()) {
                        Method method = dscr.getAddMethod();

                        if (method != null) {
                            Class entryClass = dscr.getCollectionEntryType().getClass();

                            for (Iterator iter = ((Map) value).keySet().iterator(); iter.hasNext();) {
                                Object key = iter.next();
                                Object entryValue = ((Map) value).get(key);
                                entryValue = convertPropertyValue(entryClass, entryValue);
                                if (dscr.isCollection()) {
                                    method.invoke(bean, new Object[]{entryValue});
                                }
                                // is a map
                                else {
                                    method.invoke(bean, new Object[]{key, entryValue});
                                }
                            }

                            return;
                        }
                    }
                    else {
                        value = convertPropertyValue(dscr.getType().getType(), value);
                    }
                }
            }
            catch (Exception e) {
                // do it better
                log.error("can't set property", e);
            }
        }

        try{
            // this does some conversions like string to class. Performance of PropertyUtils.setProperty() would be better
            BeanUtils.setProperty(bean, propertyName, value);
            //PropertyUtils.setProperty(bean, propertyName, value);
        }
        catch (Exception e) {
            // do it better
            log.error("can't set property [" + propertyName + "] in bean [" + bean.getClass().getName() + "]");
            if(log.isDebugEnabled()){
                log.debug("stacktrace", e);
            }
        }

    }

    /**
     * Most of the conversion is done by the BeanUtils.
     */
    public Object convertPropertyValue(Class propertyType, Object value) throws Content2BeanException {
        return value;
    }

    /**
     * Use the factory util to instantiate. This is usefull to get default implementation of interfaces
     */
    public Object newBeanInstance(TransformationState state, Map properties) {
        return FactoryUtil.newInstance(state.getCurrentType().getType());
    }

    /**
     * Call init method if present
     */
    public void initBean(TransformationState state, Map properties) throws Content2BeanException {
        Object bean = state.getCurrentBean();

        Method init;
        try {
            init = bean.getClass().getMethod("init", new Class[]{});
        }
        catch (SecurityException e) {
            return;
        }
        catch (NoSuchMethodException e) {
            return;
        }
        try {
            init.invoke(bean, null);
        }
        catch (Exception e) {
            throw new Content2BeanException("can't call init method", e);
        }
    }

    public TransformationState newState() {
        return (TransformationState) FactoryUtil.newInstance(TransformationState.class);
    }

    /**
     * Returns the dafault mapping
     */
    public TypeMapping getTypeMapping() {
        return TypeMapping.Factory.getDefaultMapping();
    }

}