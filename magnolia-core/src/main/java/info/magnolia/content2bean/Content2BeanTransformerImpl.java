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
     * Stack of beans
     */
    protected ArrayStack beanStack = new ArrayStack();

    /**
     * Mappings to use
     */
    protected Map mapPropertyMapping = new HashMap();

    /**
     * Resolves in this order
     * <ul>
     * <li> checks the class property of the node
     * <li> calls onResolve subclasses should override
     * <li> reflection on the parent bean
     * <li> in case of a collection/map type call getClassForCollectionProperty
     * <li> otherwise return null (means transform to a map)
     * </ul>
     */
    public Class resolveClass(Content node) throws ClassNotFoundException {
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
            if (!beanStack.isEmpty()) {
                if (beanStack.peek() instanceof Map) {
                    if (beanStack.size() >= 2) {
                        Object mapContainingBean = beanStack.peek(1);
                        String mapProperyName = node.getParent().getName();
                        return getClassForCollectionProperty(mapContainingBean.getClass(), mapProperyName);
                    }
                }
                else {
                    Class klass = PropertyUtils
                        .getPropertyDescriptor(beanStack.peek(), node.getName())
                        .getPropertyType();
                    if (!ClassUtil.isSubClass(klass, Map.class) && !ClassUtil.isSubClass(klass, Collection.class)) {
                        return klass;
                    }
                    else {
                        // a map is created
                        return null;
                    }
                }
            }
        }
        catch (Exception e) {
            Content2BeanProcessorImpl.log.error("can't resolve type by beans property type", e);
        }

        return null;
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

    public void pushBean(Object bean) {
        beanStack.push(bean);
    }

    public void popBean() {
        beanStack.pop();
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
            return;
        }

        Class type;
        try {
            type = PropertyUtils.getPropertyType(bean, propertyName);
            if (ClassUtil.isSubClass(type, Collection.class)) {
                value = ((Map) value).entrySet();
                Method method = getAddMethod(bean.getClass(), propertyName);
                if (method != null) {
                    for (Iterator iter = ((Collection) value).iterator(); iter.hasNext();) {
                        method.invoke(bean, new Object[]{iter.next()});
                    }
                }
                return;
            }

            // try to use an adder method
            if (ClassUtil.isSubClass(type, Map.class)) {
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
        catch (Exception e) {
            // do it better
            log.error("can't set property", e);
        }

        try {
            PropertyUtils.setProperty(bean, propertyName, value);
        }
        catch (Exception e) {
            // do it better
            log.error("can't set property", e);
        }

    }

    /**
     * Use the factory util to instantiate. This is usefull to get default implementation of interfaces
     */
    public Object newBeanInstance(Content node, Class klass) {
        return FactoryUtil.newInstance(klass);
    }

}