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
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanProcessor;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Contains the logic for traversing the hierarchy and do the calls to the transformer
 * @author philipp
 * @version $Id$
 *
 */
public class Content2BeanProcessorImpl implements Content2BeanProcessor {

    /**
     * Logger.
     */
    static Logger log = LoggerFactory.getLogger(Content2BeanProcessorImpl.class);

    boolean forceCreation = true;

    protected Content2BeanTransformerImpl defaultTransformer = new Content2BeanTransformerImpl();

    public Object toBean(Content node, boolean recursive, final Content2BeanTransformer transformer) throws Content2BeanException{
       return toBean(node, recursive, transformer, transformer.newState());
    }

    protected Object toBean(Content node, boolean recursive, final Content2BeanTransformer transformer, TransformationState state) throws Content2BeanException{

        state.pushContent(node);

        TypeDescriptor type = null;
        try {
            type = transformer.resolveType(state);
        }
        catch (ClassNotFoundException e) {
            if(isForceCreation()){
                log.warn("can't resolve class for node " +  node.getHandle(), e);
            }
            else{
                throw new Content2BeanException("can't resolve class for node " +  node.getHandle(), e);
            }
        }

        Object bean = null;
        if(type != null){
            state.pushType(type);

            Map values = toMap(node, recursive, transformer, state);

            bean = transformer.newBeanInstance(state, values);
            if(bean != null){
                state.pushBean(bean);

                setProperties(values, transformer, state);

                transformer.initBean(state, values);

                state.popBean();
            }
            else{
                if(forceCreation){
                    log.warn("can't instantiate bean of type " + type.getType().getName() + " for node " + node.getHandle());
                }
                else{
                    throw new Content2BeanException("can't instantiate bean of type " + type.getType().getName());
                }
            }

            state.popType();
        }
        state.popContent();

        return bean;
    }

    public Object setProperties(final Object bean, Content node, boolean recursive, Content2BeanTransformer transformer) throws Content2BeanException {
        TransformationState state = transformer.newState();
        state.pushBean(bean);
        state.pushContent(node);

        state.pushType(transformer.getTypeMapping().getTypeDescriptor(bean.getClass()));

        Map values = toMap(node, recursive, transformer, state);

        setProperties(values, transformer, state);

        transformer.initBean(state, values);

        state.popBean();
        state.popType();
        state.popContent();

        return bean;
    }

    /**
     * toBean() is used to build children
     */
    protected Map toMap(Content node, boolean recursive, Content2BeanTransformer transformer, TransformationState state) throws Content2BeanException {
        Map map = new LinkedHashMap();
        for (Iterator iter = node.getNodeDataCollection().iterator(); iter.hasNext();) {
            NodeData nd = (NodeData) iter.next();
            Object val = NodeDataUtil.getValueObject(nd);
            if (val != null) {
                map.put(nd.getName(), val);
            }
        }

        if(recursive){
            Collection children = node.getChildren(transformer);
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                final Content childNode = (Content) iter.next();
                // in case the the class can not get resolved we can use now
                // the parent bean to resolve the class

                Object childBean = toBean(childNode, true, transformer, state);
                // can be null if forceCreation is true
                if(childBean != null){
                    map.put(childNode.getName(), childBean);
                }
            }
        }

        return map;
    }

    /**
     * Populates the values to the bean
     * @todo in case the bean is a map / collection the transfomer.setProperty() method should be called too
     * @todo if the bean has not a certain property but a value is present, transformer.setProperty() should be called with a fake property descriptor
     */
    protected void setProperties(Map values, final Content2BeanTransformer transformer, TransformationState state) throws Content2BeanException {
        Object bean = state.getCurrentBean();
        log.debug("will populate bean {} with the values {}", bean.getClass().getName(), values);

        if(bean instanceof Map){
            ((Map)bean).putAll(values);
        }

        if(bean instanceof Collection){
            ((Collection)bean).addAll(values.values());
        }

        else{
            TypeDescriptor beanTypeDescriptor = transformer.getTypeMapping().getTypeDescriptor(bean.getClass());
            Collection dscrs = beanTypeDescriptor.getPropertyDescriptors().values();
            for (Iterator iter = dscrs.iterator(); iter.hasNext();) {
                PropertyTypeDescriptor descriptor = (PropertyTypeDescriptor) iter.next();
                transformer.setProperty(state, descriptor, values);
            }
        }
    }


    public boolean isForceCreation() {
        return this.forceCreation;
    }


    public void setForceCreation(boolean handleExceptions) {
        this.forceCreation = handleExceptions;
    }

}
