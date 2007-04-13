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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang.StringUtils;
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
     * Property types already resolved
     */
    protected static Map propertyTypes = new HashMap();

    /**
     * Stack of classes
     */
    protected ArrayStack classStack = new ArrayStack();

    protected ArrayStack nodeStack = new ArrayStack();

    /**
     * Mappings to use
     */
    protected Map mapPropertyMapping = new HashMap();

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
    public Class resolveClass() throws ClassNotFoundException {
        Content node = (Content) nodeStack.peek();
        try {
            if (node.hasNodeData("class")) {
                String className = node.getNodeData("class").getString();
                return ClassUtil.classForName(className);
            }
            else {
                Class klass = onResolveClass(node);
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
            if (!classStack.isEmpty()) {
                if (ClassUtil.isSubClass((Class)classStack.peek(), Map.class)) {
                    if (classStack.size() >= 2) {
                        // this is not necesserely the parent node of the current
                        String mapProperyName = ((Content)nodeStack.peek(1)).getName();
                        return getClassForCollectionProperty((Class)classStack.peek(1), mapProperyName);
                    }
                }
                else {
                    return resolvePropertyType((Class)classStack.peek(), node.getName());

                }
            }
        }
        catch (Exception e) {
            Content2BeanProcessorImpl.log.error("can't resolve type by beans property type", e);
        }

        return HashMap.class;
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

    /**
     * Override for custom resolving. In case this method is called no class property was set on the node
     */
    protected Class onResolveClass(Content node) {
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

        Content2BeanTransformerImpl defaultTransformer = Content2BeanUtil
            .getContent2BeanProcessor()
            .getDefaultContentToBeanTransformer();

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

    public void addCollectionPropertyClass(Class type, String name, Class mappedType) {
        mapPropertyMapping.put(type.getName() + "." + name, mappedType);
    }

    public void pushClass(Class klass) {
        classStack.push(klass);
    }

    public void popClass() {
        classStack.pop();
    }

    public void pushContent(Content node) {
        nodeStack.push(node);
    }

    public void popContent() {
        nodeStack.pop();
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
    public void setProperty(Object bean, String propertyName, Object value) {
        if (propertyName.equals("class")) {
            propertyName = "className";
        }

        if(!(bean instanceof Map)){
            try {
                Class type = resolvePropertyType(bean.getClass(), propertyName);
                if(type != null){
                    if (ClassUtil.isSubClass(type, Collection.class)) {
                        value = ((Map) value).entrySet();
                        Method method = getAddMethod(bean.getClass(), propertyName);
                        if (method != null) {
                            for (Iterator iter = ((Collection) value).iterator(); iter.hasNext();) {
                                method.invoke(bean, new Object[]{iter.next()});
                            }
                            return;
                        }
                    }
                    // try to use an adder method
                    else if (ClassUtil.isSubClass(type, Map.class)) {
                        Method method = getAddMethod(bean.getClass(), propertyName);
                        if (method != null) {
                            for (Iterator iter = ((Map) value).keySet().iterator(); iter.hasNext();) {
                                Object key = iter.next();
                                method.invoke(bean, new Object[]{key, ((Map) value).get(key)});
                            }
                            return;
                        }
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

    /**
     * Use the factory util to instantiate. This is usefull to get default implementation of interfaces
     */
    public Object newBeanInstance(Class klass, Map properties) {
        return FactoryUtil.newInstance(klass);
    }

    /**
     * Call init method if present
     */
    public void initBean(Object bean, Map properties) throws Content2BeanException {
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
            init.invoke(bean);
        }
        catch (Exception e) {
            throw new Content2BeanException("can't call init method",e);
        }
    }

}