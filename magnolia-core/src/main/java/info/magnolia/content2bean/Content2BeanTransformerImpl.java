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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Concrete implementation using reflection and adder methods.
 * @author philipp
 * @version $Id$
 */
public class Content2BeanTransformerImpl extends CollectionPropertyMappingImpl implements Content2BeanTransformer {

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
    public Class resolveClass(TransformationState state) throws ClassNotFoundException {
        Content node = state.getCurrentContent();
        try {
            if (node.hasNodeData("class")) {
                String className = node.getNodeData("class").getString();
                return ClassUtil.classForName(className);
            }
            else {
                Class klass = onResolveClass(state);
                if (klass != null) {
                    return klass;
                }
            }
        }
        catch (RepositoryException e) {
            // ignore
            Content2BeanProcessorImpl.log.warn("can't read class property", e);
        }

        try {
            if(state.getLevel() > 1){
                if (ClassUtil.isSubClass(state.getCurrentClass(), Map.class)) {
                    if (state.getLevel() > 2) {
                        // this is not necesserely the parent node of the current
                        String mapProperyName = state.peekContent(1).getName();
                        return getClassForCollectionProperty(state.peekClass(1), mapProperyName);
                    }
                }
                else {
                    return resolvePropertyType(state.getCurrentClass(), node.getName());

                }
            }
        }
        catch (Exception e) {
            Content2BeanProcessorImpl.log.error("can't resolve type by beans property type", e);
        }

        return HashMap.class;
    }


    /**
     * Override for custom resolving. In case this method is called no class property was set on the node
     */
    protected Class onResolveClass(TransformationState state) {
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
    public void setProperty(TransformationState state, String propertyName, Object value) {
        if (propertyName.equals("class")) {
            propertyName = "className";
        }

        Object bean = state.getCurrentBean();

        // if the parent bean is a map, we can't guess the types.
        if(!(bean instanceof Map)){
            try {
                Class type = resolvePropertyType(bean.getClass(), propertyName);
                if(type != null){
                    boolean isCollection = ClassUtil.isSubClass(type, Collection.class);
                    boolean isMap = isCollection == true? false : ClassUtil.isSubClass(type, Map.class);

                    // try to use an adder method for a Collection property of the bean
                    if (isCollection || isMap) {
                        Method method = getAddMethod(bean.getClass(), propertyName);

                        if (method != null) {
                            Class entryClass = getClassForCollectionProperty(bean.getClass(), propertyName);

                            for (Iterator iter = ((Map) value).keySet().iterator(); iter.hasNext();) {
                                Object key = iter.next();
                                Object entryValue = ((Map) value).get(key);
                                entryValue = convertPropertyValue(entryClass, entryValue);
                                if(isCollection){
                                    method.invoke(bean, new Object[]{entryValue});
                                }
                                // is a map
                                else{
                                    method.invoke(bean, new Object[]{key, entryValue});
                                }
                            }

                            return;
                        }
                    }
                    else{
                        value = convertPropertyValue(type, value);
                    }
                }
            }
            catch (Exception e) {
                // do it better
                log.error("can't set property", e);
            }
        }

        try {
            PropertyUtils.setProperty(bean, propertyName, value);
        }
        catch (NoSuchMethodException e){
            // ignore, this is not a property at all
        }
        catch (Exception e) {
            // do it better
            log.error("can't set property", e);
        }

    }

    protected Object convertPropertyValue(Class propertyType, Object value) throws ClassNotFoundException {
        if (Class.class.equals(propertyType)) {
            value = ClassUtil.classForName((String) value);
        }
        return value;
    }

    /**
     * Use the factory util to instantiate. This is usefull to get default implementation of interfaces
     */
    public Object newBeanInstance(TransformationState state, Map properties) {
        return FactoryUtil.newInstance(state.getCurrentClass());
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
            throw new Content2BeanException("can't call init method",e);
        }
    }

}