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

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.SystemContentWrapper;
import info.magnolia.jcr.iterator.FilteringNodeIterator;
import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.PropertyTypeDescriptor;
import info.magnolia.jcr.node2bean.TransformationState;
import info.magnolia.jcr.node2bean.TypeDescriptor;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.ComponentProvider;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/**
 * Concrete implementation using reflection, generics and setter methods.
 */
public class Node2BeanTransformerImpl implements Node2BeanTransformer {

    private static final Logger log = LoggerFactory.getLogger(Node2BeanTransformerImpl.class);

    private final BeanUtilsBean beanUtilsBean;

    private final Class<?> defaultListImpl;

    private final Class<?> defaultSetImpl;

    private final Class<?> defaultQueueImpl;

    @Inject
    public Node2BeanTransformerImpl() {
        this(LinkedList.class, HashSet.class, LinkedList.class);
    }

    public Node2BeanTransformerImpl(Class<?> defaultListImpl, Class<?> defaultSetImpl, Class<?> defaultQueueImpl) {
        this.defaultListImpl = defaultListImpl;
        this.defaultSetImpl = defaultSetImpl;
        this.defaultQueueImpl = defaultQueueImpl;

        // We use non-static BeanUtils conversion, so we can
        // * use our custom ConvertUtilsBean
        // * control converters (convertUtilsBean.register()) - we can register them here, locally, as opposed to a
        // global ConvertUtils.register()
        final EnumAwareConvertUtilsBean convertUtilsBean = new EnumAwareConvertUtilsBean();

        // de-register the converter for Class, we do our own conversion in convertPropertyValue()
        convertUtilsBean.deregister(Class.class);

        convertUtilsBean.register(new Converter() {
            @Override
            public Object convert(Class type, Object value) {
                return new SimpleUrlPattern((String) value);
            }
        }, SimpleUrlPattern.class);

        convertUtilsBean.register(new Converter() {
            @Override
            public Object convert(Class type, Object value) {
                    return new MessageFormat((String) value);
            }
        }, MessageFormat.class);

        convertUtilsBean.register(new Converter() {
            @Override
            public Object convert(Class type, Object value) {
                return Pattern.compile((String) value);
            }
        }, Pattern.class);

        this.beanUtilsBean = new BeanUtilsBean(convertUtilsBean, new PropertyUtilsBean());
    }

    @Override
    public TransformationState newState() {
        return new TransformationStateImpl();
    }

    @Override
    public TypeDescriptor resolveType(TypeMapping typeMapping, TransformationState state, ComponentProvider componentProvider) throws ClassNotFoundException, RepositoryException {
        TypeDescriptor typeDscr = null;
        Node node = state.getCurrentNode();

        try {
            if (node.hasProperty("class")) {
                String className = node.getProperty("class").getString();
                if (StringUtils.isBlank(className)) {
                    log.warn("Cannot resolve type for node [" + node + "] because class property has empty value.");
                } else {
                    Class<?> clazz = Classes.getClassFactory().forName(className);
                    typeDscr = typeMapping.getTypeDescriptor(clazz);
                }
            }
        } catch (RepositoryException e) {
            log.warn("Can't read class property from node [{}]", node.getPath(), e);
        }

        if (typeDscr == null && state.getLevel() > 1) {
            TypeDescriptor parentTypeDscr = state.getCurrentType();
            PropertyTypeDescriptor propDscr;

            if (parentTypeDscr.isMap() || parentTypeDscr.isCollection()) {
                if (state.getLevel() > 2) {
                    // this is not necessarily the parent node of the current
                    String mapProperyName = state.peekNode(1).getName();
                    propDscr = state.peekType(1).getPropertyTypeDescriptor(mapProperyName, typeMapping);
                    if (propDscr != null) {
                        typeDscr = propDscr.getCollectionEntryType();
                    }
                }
            } else {
                propDscr = state.getCurrentType().getPropertyTypeDescriptor(node.getName(), typeMapping);
                if (propDscr != null) {
                    typeDscr = propDscr.getType();
                }
            }
        }

        typeDscr = onResolveType(typeMapping, state, typeDscr, componentProvider);

        if (typeDscr != null) {
            // might be that the factory util defines a default implementation for interfaces
            final Class<?> type = typeDscr.getType();
            typeDscr = typeMapping.getTypeDescriptor(componentProvider.getImplementation(type));

            // now that we know the property type we should delegate to the custom transformer if any defined
            Node2BeanTransformer customTransformer = typeDscr.getTransformer();
            if (customTransformer != null && customTransformer != this) {
                TypeDescriptor typeFoundByCustomTransformer = customTransformer.resolveType(typeMapping, state, componentProvider);
                // if no specific type has been provided by the
                // TODO - TypeDescriptor - equals and hashCode impl and use
                // not equals instead of !=
                if (typeFoundByCustomTransformer != TypeMapping.MAP_TYPE) {
                    // might be that the factory util defines a default implementation for interfaces
                    Class<?> implementation = componentProvider.getImplementation(typeFoundByCustomTransformer.getType());
                    typeDscr = typeMapping.getTypeDescriptor(implementation);
                }
            }
        }

        if (typeDscr == null || typeDscr.needsDefaultMapping()) {
            if (typeDscr == null) {
                log.debug("Was not able to resolve type for node [{}] will use a map", node);
            }
            typeDscr = TypeMapping.MAP_TYPE;
        }
        log.debug("Resolved type [{}] for node [{}]", typeDscr.getType(), node.getPath());

        return typeDscr;
    }

    @Override
    public NodeIterator getChildren(Node node) throws RepositoryException {
        // TODO create predicate into separate class, <? extends Item> ItemHidingPredicate (regexp)
        return new FilteringNodeIterator(node.getNodes(), new AbstractPredicate<Node>() {
            @Override
            public boolean evaluateTyped(Node t) {
                try {
                    return !(t.getName().startsWith(MgnlNodeType.JCR_PREFIX) ||
                            t.getName().startsWith(MgnlNodeType.MGNL_PREFIX) ||
                            t.isNodeType(MgnlNodeType.NT_METADATA));
                } catch (RepositoryException e) {
                    return false;
                }
            }
        });
    }

    @Override
    public Object newBeanInstance(TransformationState state, Map<String, Object> values, ComponentProvider componentProvider) throws Node2BeanException {
        // we try first to use conversion (Map --> primitive type)
        // this is the case when we flattening the hierarchy?
        final Object bean = convertPropertyValue(state.getCurrentType().getType(), values);
        // were the properties transformed?
        if (bean == values) {
            try {
                // is this property remove necessary?
                values.remove("class");
                final Class<?> type = state.getCurrentType().getType();
                if (LinkedHashMap.class.equals(type)) {
                    // TODO - as far as I can tell, "bean" and "properties" are already the same instance of a
                    // LinkedHashMap, so what are we doing in here ?
                    return new LinkedHashMap();
                } else if (Map.class.isAssignableFrom(type)) {
                    // TODO ?
                    log.warn("someone wants another type of map ? " + type);
                } else if (Collection.class.isAssignableFrom(type)) {
                    // someone wants specific collection
                    return type.newInstance();
                }
                return componentProvider.newInstance(type);
            } catch (Throwable e) {
                throw new Node2BeanException(e);
            }
        }
        return bean;
    }

    @Override
    public void initBean(TransformationState state, Map values) throws Node2BeanException {
        Object bean = state.getCurrentBean();

        Method init;
        try {
            init = bean.getClass().getMethod("init", new Class[] {});
            try {
                init.invoke(bean); // no parameters
            } catch (Exception e) {
                throw new Node2BeanException("can't call init method", e);
            }
        } catch (SecurityException e) {
            return;
        } catch (NoSuchMethodException e) {
            return;
        }
        log.debug("{} is initialized", bean);

    }

    @Override
    public Object convertPropertyValue(Class<?> propertyType, Object value) throws Node2BeanException {
        if (Class.class.equals(propertyType)) {
            try {
                return Classes.getClassFactory().forName(value.toString());
            } catch (ClassNotFoundException e) {
                log.error("Can't convert property. Class for type [{}] not found.", propertyType);
                throw new Node2BeanException(e);
            }
        }

        if (Locale.class.equals(propertyType)) {
            if (value instanceof String) {
                String localeStr = (String) value;
                if (StringUtils.isNotEmpty(localeStr)) {
                    return LocaleUtils.toLocale(localeStr);
                }
            }
        }

        if ((Collection.class.equals(propertyType)) && (value instanceof Map)) {
            // TODO never used ?
            return ((Map) value).values();
        }

        // this is mainly the case when we are flattening node hierarchies
        if ((String.class.equals(propertyType)) && (value instanceof Map) && (((Map) value).size() == 1)) {
            return ((Map) value).values().iterator().next();
        }

        return value;
    }

    /**
     * Called once the type should have been resolved. The resolvedType might be
     * null if no type has been resolved. Every subclass should override this method.
     */
    protected TypeDescriptor onResolveType(TypeMapping typeMapping, TransformationState state, TypeDescriptor resolvedType, ComponentProvider componentProvider) {
        return resolvedType;
    }

    @Override
    public void setProperty(TypeMapping mapping, TransformationState state, PropertyTypeDescriptor descriptor, Map<String, Object> values) throws RepositoryException {
        String propertyName = descriptor.getName();
        if (propertyName.equals("class")) {
            return;
        }
        Object value = values.get(propertyName);
        Object bean = state.getCurrentBean();

        if (propertyName.equals("content") && value == null) {
            // TODO this should be changed to node but this would require to
            // rewrite some classes to use node instead of content
            value = new SystemContentWrapper(ContentUtil.asContent((state.getCurrentNode())));
        } else if (propertyName.equals("name") && value == null) {
            value = state.getCurrentNode().getName();
        } else if (propertyName.equals("className") && value == null) {
            value = values.get("class");
        }

        // do no try to set a bean-property that has no corresponding node-property
        if (value == null) {
            return;
        }

        log.debug("try to set {}.{} with value {}", new Object[] {bean, propertyName, value});
        // if the parent bean is a map, we can't guess the types.
        if (!(bean instanceof Map)) {
            try {
                PropertyTypeDescriptor dscr = mapping.getPropertyTypeDescriptor(bean.getClass(), propertyName);
                if (dscr.getType() != null) {
                    // try to use a setter method for a Collection property of
                    // the bean
                    if (dscr.isCollection() || dscr.isMap() || dscr.isArray()) {
                        log.debug("{} is of type collection, map or array", propertyName);
                        Method method = dscr.getWriteMethod();

                        if (method != null) {
                            log.debug("clearing the current content of the collection/map");
                            try {
                                Object col = PropertyUtils.getProperty(bean, propertyName);
                                if (col != null) {
                                    MethodUtils.invokeExactMethod(col, "clear", new Object[] {});
                                }
                            } catch (Exception e) {
                                log.debug("no clear method found on collection {}", propertyName);
                            }

                            if (dscr.isMap()) {
                                method.invoke(bean, value);
                            } else if (dscr.isArray()){
                                Class<?> entryClass = dscr.getCollectionEntryType().getType();
                                Collection<Object> list = new LinkedList<Object>(((Map<Object, Object>) value).values());

                                Object[] arr = (Object[]) Array.newInstance(entryClass, list.size());
                                for (int i = 0; i < arr.length; i++) {
                                    arr[i] = Iterables.get(list, i);
                                }
                                method.invoke(bean, new Object[] {arr});
                            } else if (dscr.isCollection()) {
                                if (value instanceof Map) {
                                    value = createCollectionFromMap((Map<Object, Object>) value, dscr.getType().getType());
                                }
                                method.invoke(bean, value);
                            }
                            return;
                        }
                        if (dscr.isCollection()) {
                            log.debug("transform the values to a collection", propertyName);
                            value = ((Map<Object, Object>) value).values();
                        }
                    } else {
                        value = convertPropertyValue(dscr.getType().getType(), value);
                    }
                }
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Can't set property [{}] to value [{}] in bean [{}] for node {} due to {}",
                        new Object[] { propertyName, value, bean.getClass().getName(),
                            state.getCurrentNode().getPath(), e.toString() });
                } else {
                    log.error("Can't set property [{}] to value [{}] in bean [{}] for node {} due to {}",
                        new Object[] {propertyName, value, bean.getClass().getName(),
                                state.getCurrentNode().getPath(), e.toString()});
                }
            }
        }

        try {
            // This uses the converters registered in beanUtilsBean.convertUtilsBean (see constructor of this class)
            // If a converter is registered, beanutils will convert value.toString(), not the value object as-is.
            // If no converter is registered, then the value Object is set as-is.
            // If convertPropertyValue() already converted this value, you'll probably want to unregister the beanutils
            // converter.
            // some conversions like string to class. Performance of PropertyUtils.setProperty() would be better
            beanUtilsBean.setProperty(bean, propertyName, value);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Can't set property [{}] to value [{}] in bean [{}] for node {} due to {}",
                    new Object[] { propertyName, value, bean.getClass().getName(),
                        state.getCurrentNode().getPath(), e.toString() });
            } else {
                log.error("Can't set property [{}] to value [{}] in bean [{}] for node {} due to {}",
                    new Object[] {propertyName, value, bean.getClass().getName(),
                            state.getCurrentNode().getPath(), e.toString()});
            }
        }
    }

    /**
     * Creates collection from map. Collection type depends on passed class parameter. If passed class parameter is
     * interface, then default implementation will be used for creating collection.<br/>
     * By default
     * <ul>
     * <li>{@link LinkedList} is used for creating List and Queue collections.</li>
     * <li>{@link HashSet} is used for creating Set collection.</li>
     * </ul>
     * If passed class parameter is an implementation of any collection type, then this method will create
     * this implementation and returns it.
     * @param map a map which values will be converted to a collection
     * @param clazz collection type
     * @return Collection of elements or null.
     */
    protected Collection<?> createCollectionFromMap(Map<?, ?> map, Class<?> clazz) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Collection<?> collection = null;
        Constructor<?> constructor = null;
        if (clazz.isInterface()) {
            // class is an interface, we need to decide which implementation of interface we will use
            if (List.class.isAssignableFrom(clazz)) {
                constructor = defaultListImpl.getConstructor(Collection.class);
            } else if (clazz.isAssignableFrom(Queue.class)) {
                constructor = defaultQueueImpl.getConstructor(Collection.class);
            } else if (Set.class.isAssignableFrom(clazz)) {
                constructor = defaultSetImpl.getConstructor(Collection.class);
            }
        } else {
            if (Collection.class.isAssignableFrom(clazz)) {
                constructor = clazz.getConstructor(Collection.class);
            }
        }
        if (constructor != null) {
            collection = (Collection<?>) constructor.newInstance(map.values());
        }
        return collection;
    }

}