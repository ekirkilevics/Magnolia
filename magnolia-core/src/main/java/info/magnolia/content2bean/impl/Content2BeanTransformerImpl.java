/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.content2bean.impl;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.SystemContentWrapper;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.TypeMapping;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation using reflection and adder methods.
 * @author philipp
 * @version $Id$
 */
public class Content2BeanTransformerImpl implements Content2BeanTransformer, Content.ContentFilter  {
    private static final Logger log = LoggerFactory.getLogger(Content2BeanTransformerImpl.class);

    public Content2BeanTransformerImpl() {
        try {
            Method onResolveClass = this.getClass().getDeclaredMethod("onResolveClass", new Class[]{TransformationState.class});
            log.error("onResolceClass(state) is not supported anymore please override onResolveType(state, resolvedType) instead: " + onResolveClass);
        } catch (NoSuchMethodException e) {
            // As the class should not define this method everything is fine
        }
    }

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
                    if(propDscr != null){
                        typeDscr = propDscr.getCollectionEntryType();
                    }
                }
            }
            else {
                propDscr = state.getCurrentType().getPropertyTypeDescriptor(node.getName());
                if(propDscr != null){
                    typeDscr = propDscr.getType();
                }
            }
        }

        typeDscr = onResolveType(state, typeDscr);

        if(typeDscr != null){
            // might be that the factory util defines a default implementation for interfaces
            typeDscr = getTypeMapping().getTypeDescriptor(FactoryUtil.getImplementation(typeDscr.getType()));

            // now that we know the property type we should delegate to the custom transformer if any defined
            Content2BeanTransformer customTransformer = typeDscr.getTransformer();
            if(customTransformer != null && customTransformer != this){
                TypeDescriptor typeFoundByCustomTransformer = customTransformer.resolveType(state);
                // if no specific type has been provided by the
                if(typeFoundByCustomTransformer != TypeMapping.MAP_TYPE){
                    // might be that the factory util defines a default implementation for interfaces
                    Class implementation = FactoryUtil.getImplementation(typeFoundByCustomTransformer.getType());
                    typeDscr = getTypeMapping().getTypeDescriptor(implementation);
                }
            }
        }

        if (typeDscr == null || typeDscr.isMap() || typeDscr.isCollection()) {
            if(typeDscr== null && log.isDebugEnabled()){
                log.debug("was not able to resolve type for node [{}] will use a map", node );
            }
            typeDscr = TypeMapping.MAP_TYPE;
        }

        log.debug("{} --> {}", node.getHandle(), typeDscr.getType());

        return typeDscr;
    }


    /**
     * Called once the type should have been resolved. The resolvedType might be
     * null if no type has been resolved. After the call the FactoryUtil and
     * custom transformers are used to get the final type.
     */
    protected TypeDescriptor onResolveType(TransformationState state, TypeDescriptor resolvedType) {
        return resolvedType;
    }

    public Collection getChildren(Content node) {
        return node.getChildren(this);
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
        TypeMapping mapping = getTypeMapping();

        String propertyName = descriptor.getName();
        if(propertyName.equals("class")){
            return;
        }
        Object value = values.get(propertyName);
        Object bean = state.getCurrentBean();

        if (propertyName.equals("content") && value == null) {
            value = new SystemContentWrapper(state.getCurrentContent());
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

        log.debug("try to set {}.{} with value {}", new Object[]{bean, propertyName, value});

        // if the parent bean is a map, we can't guess the types.
        if (!(bean instanceof Map)) {
            try {
                PropertyTypeDescriptor dscr = mapping.getPropertyTypeDescriptor(bean.getClass(), propertyName);
                if (dscr.getType() != null) {

                    // try to use an adder method for a Collection property of the bean
                    if (dscr.isCollection() || dscr.isMap()) {
                        log.debug("{} is of type collection, map or /array", propertyName );
                        Method method = dscr.getAddMethod();

                        if (method != null) {
                            log.debug("clearing the current content of the collection/map");
                            try {
                                Object col = PropertyUtils.getProperty(bean, propertyName);
                                if(col != null){
                                    MethodUtils.invokeExactMethod(col, "clear", new Object[]{});
                                }
                            }
                            catch (Exception e) {
                                log.debug("no clear method found on collection {}", propertyName);
                            }

                            Class entryClass = dscr.getCollectionEntryType().getType();

                            log.debug("will add values by using adder method {}", method.getName());
                            for (Iterator iter = ((Map) value).keySet().iterator(); iter.hasNext();) {
                                Object key = iter.next();
                                Object entryValue = ((Map) value).get(key);
                                entryValue = convertPropertyValue(entryClass, entryValue);
                                if (dscr.isCollection()) {
                                    log.debug("will add value {}", entryValue);
                                    method.invoke(bean, new Object[]{entryValue});
                                }
                                // is a map
                                else {
                                    log.debug("will add key {} with value {}", key, entryValue);
                                    method.invoke(bean, new Object[]{key, entryValue});
                                }
                            }

                            return;
                        }
                        else{
                            log.debug("no add method found for property {}", propertyName);
                            if(dscr.isCollection()){
                                log.debug("transform the valus to a collection", propertyName);
                                value = ((Map)value).values();
                            }
                        }
                    }
                    else {
                        value = convertPropertyValue(dscr.getType().getType(), value);
                    }
                }
            }
            catch (Exception e) {
                // do it better
                log.error("can't set property [" + propertyName + "] to value [" + value + "] in bean [" + bean.getClass().getName() + "]");
            }
        }

        try{
            // this does some conversions like string to class. Performance of PropertyUtils.setProperty() would be better
            BeanUtils.setProperty(bean, propertyName, value);
            //PropertyUtils.setProperty(bean, propertyName, value);
        }
        catch (Exception e) {
            // do it better
            log.error("can't set property [" + propertyName + "] to value [" + value + "] in bean [" + bean.getClass().getName() + "]");
            if(log.isDebugEnabled()){
                log.debug("stacktrace", e);
            }
        }

    }

    /**
     * Most of the conversion is done by the BeanUtils.
     * TODO don't use bean utils converion since it can't be used for the adder methods
     */
    public Object convertPropertyValue(Class propertyType, Object value) throws Content2BeanException {
        if(propertyType == Locale.class){
            if(value instanceof String){
                String localeStr = (String) value;
                if(StringUtils.isNotEmpty(localeStr)){
                    return LocaleUtils.toLocale(localeStr);
                }
            }
        }
        if(propertyType == Collection.class && value instanceof Map){
            return ((Map)value).values();
        }

        // this is mainly the case when we are flattening node hierarchies
        if(propertyType == String.class && value instanceof Map && ((Map)value).size() == 1){
            return ((Map)value).values().iterator().next();
        }
        return value;
    }

    /**
     * Use the factory util to instantiate. This is usefull to get default implementation of interfaces
     */
    public Object newBeanInstance(TransformationState state, Map properties) throws Content2BeanException{
        // we try first to use conversion (Map --> primitive tyoe)
        // this is the case when we flattening the hierarchy?
        Object bean = convertPropertyValue(state.getCurrentType().getType(), properties);
        // were the propertis transformed?
        if(bean == properties){
            bean = FactoryUtil.newInstance(state.getCurrentType().getType());
        }
        return bean;
    }

    /**
     * Call init method if present
     */
    public void initBean(TransformationState state, Map properties) throws Content2BeanException {
        Object bean = state.getCurrentBean();

        Method init;
        try {
            init = bean.getClass().getMethod("init", new Class[]{});
            try {
                init.invoke(bean, null);
            }
            catch (Exception e) {
                throw new Content2BeanException("can't call init method", e);
            }
        }
        catch (SecurityException e) {
            return;
        }
        catch (NoSuchMethodException e) {
            return;
        }
        log.debug("{} is initialized" , bean);
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
