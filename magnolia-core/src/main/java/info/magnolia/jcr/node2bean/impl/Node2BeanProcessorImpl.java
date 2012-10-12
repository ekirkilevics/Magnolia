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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.PropertyTypeDescriptor;
import info.magnolia.jcr.node2bean.TransformationState;
import info.magnolia.jcr.node2bean.TypeDescriptor;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.wrapper.ExtendingNodeWrapper;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;

/**
 * Contains the logic for traversing the hierarchy and do the calls to the transformer.
 */
@Singleton
public class Node2BeanProcessorImpl implements Node2BeanProcessor {
    private static final Logger log = LoggerFactory.getLogger(Node2BeanProcessorImpl.class);

    private final TypeMapping typeMapping;

    private Node2BeanTransformer transformer = Components.getComponent(Node2BeanTransformer.class);

    private boolean forceCreation = true;

    private boolean recursive = true;

    @Inject
    public Node2BeanProcessorImpl(TypeMapping typeMapping) {
        this.typeMapping = typeMapping;
    }

    @Override
    public Object toBean(Node node) throws Node2BeanException, RepositoryException {
        return toBean(new ExtendingNodeWrapper(node), recursive, transformer, transformer.newState(), Components.getComponentProvider());
    }

    @Override
    public Object toBean(Node node, boolean recursive, final Node2BeanTransformer transformer, ComponentProvider componentProvider) throws Node2BeanException, RepositoryException {
        return toBean(new ExtendingNodeWrapper(node), recursive, transformer, transformer.newState(), componentProvider);
    }

    protected Object toBean(Node node, boolean recursive, Node2BeanTransformer transformer, TransformationState state, ComponentProvider componentProvider) throws Node2BeanException, RepositoryException{

        state.pushNode(node);

        TypeDescriptor type = null;
        try {
            type = transformer.resolveType(typeMapping, state, componentProvider);
        }
        catch (Throwable e) {
            if(isForceCreation()){
                log.warn("can't resolve class for node " +  node.getPath(), e);
            }
            else{
                throw new Node2BeanException("can't resolve class for node " +  node.getPath(), e);
            }
        }

        Object bean = null;
        if(type != null){
            state.pushType(type);

            transformer = resolveTransformer(type, transformer);

            Map<String, Object> values = toMap(node, recursive, transformer, state, componentProvider);

            try {
                bean = transformer.newBeanInstance(state, values, componentProvider);
            }
            catch (Throwable e) {
                if(isForceCreation()){
                    log.warn("Can't instantiate bean for " +  node.getPath(), e);
                }
                else{
                    throw new Node2BeanException("Can't instantiate bean for " +  node.getPath(), e);
                }
            }

            if(bean != null){
                state.pushBean(bean);

                setProperties(values, transformer, state);

                transformer.initBean(state, values);

                bean = state.getCurrentBean();

                state.popBean();
            }
            else{
                if(forceCreation){
                    log.warn("can't instantiate bean of type " + type.getType().getName() + " for node " + node.getPath());
                }
                else{
                    throw new Node2BeanException("can't instantiate bean of type " + type.getType().getName());
                }
            }

            state.popType();
        }
        state.popNode();

        return bean;
    }

    @Override
    public Object setProperties(final Object bean, Node node, boolean recursive, Node2BeanTransformer transformer, ComponentProvider componentProvider) throws Node2BeanException, RepositoryException {
        // enable extending feature
        node = new ExtendingNodeWrapper(node);

        TransformationState state = transformer.newState();
        state.pushBean(bean);
        state.pushNode(node);

        // TODO -  MAGNOLIA-3525 TypeDescriptor type = transformer.getTypeMapping().getTypeDescriptor(bean.getClass());
        TypeDescriptor type = typeMapping.getTypeDescriptor(bean.getClass());

        state.pushType(type);

        transformer = resolveTransformer(type, transformer);

        Map<String, Object> values = toMap(node, recursive, transformer, state, componentProvider);

        setProperties(values, transformer, state);

        transformer.initBean(state, values);

        state.popBean();
        state.popType();
        state.popNode();

        return bean;
    }

    /**
     * Transforms the children of provided content into a map.
     * @throws RepositoryException
     */
    protected Map<String, Object> toMap(Node node, boolean recursive, Node2BeanTransformer transformer, TransformationState state, ComponentProvider componentProvider) throws Node2BeanException, RepositoryException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        PropertyIterator it = node.getProperties();
        while (it.hasNext()) {
            Property p = (Property) it.next();
            Object val = PropertyUtil.getValueObject(p.getValue());
            if (val != null) {
                map.put(p.getName(), val);
            }
        }
        if(recursive){
            final Collection<Node> children = transformer.getChildren(node);
            for (Node childNode : children) {
                // in case the the class can not get resolved we can use now
                // the parent bean to resolve the class

                Object childBean = toBean(childNode, true, transformer, state, componentProvider);
                // can be null if forceCreation is true
                if(childBean != null){
                    String name = childNode.getName();
                    try {
                        if(childNode.getIndex() > 1){
                            name += childNode.getIndex();
                        }
                    }
                    catch (RepositoryException e) {
                        log.error("can't read index of the node [" + childNode + "]", e);
                    }
                    map.put(name, childBean);
                }
            }
        }

        return map;
    }

    /**
     * Populates the properties of the bean with values from the map.
     * TODO in case the bean is a map / collection the transfomer.setProperty() method should be called too
     * TODO if the bean has not a certain property but a value is present, transformer.setProperty() should be called with a fake property descriptor
     * @throws RepositoryException
     */
    protected void setProperties(Map<String, Object> values, final Node2BeanTransformer transformer, TransformationState state) throws Node2BeanException, RepositoryException {
        Object bean = state.getCurrentBean();
        log.debug("will populate bean {} with the values {}", bean.getClass().getName(), values);

        if(bean instanceof Map){
            ((Map<String, Object>)bean).putAll(values);
        }

        if(bean instanceof Collection){
            ((Collection<Object>)bean).addAll(values.values());
        }

        else{
            // TypeDescriptor beanTypeDescriptor = transformer.getTypeMapping().getTypeDescriptor(bean.getClass());
            TypeDescriptor beanTypeDescriptor = typeMapping.getTypeDescriptor(bean.getClass());
            final Collection<PropertyTypeDescriptor> dscrs = beanTypeDescriptor.getPropertyDescriptors(typeMapping).values();

            for (PropertyTypeDescriptor descriptor : dscrs) {
                transformer.setProperty(typeMapping, state, descriptor, values);
            }
        }
    }

    protected Node2BeanTransformer resolveTransformer(TypeDescriptor type, Node2BeanTransformer transformer) {
        Node2BeanTransformer customTransformer = type.getTransformer();
        if(customTransformer != null){
            transformer = customTransformer;
        }
        return transformer;
    }

    public boolean isForceCreation() {
        return this.forceCreation;
    }

    public void setForceCreation(boolean forceCreation) {
        this.forceCreation = forceCreation;
    }
}
