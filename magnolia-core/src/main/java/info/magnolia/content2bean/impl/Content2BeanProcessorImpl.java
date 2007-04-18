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
import info.magnolia.content2bean.TypeMapping;
import info.magnolia.content2bean.TypeMapping.Factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
     * Transforms all nodes to a map
     */
    public static final Content2BeanTransformerImpl TO_MAP_TRANSFORMER = new Content2BeanTransformerImpl() {

        public TypeDescriptor resolveType(TransformationState state) throws ClassNotFoundException {
            return TypeMapping.MAP_TYPE;
        }
    };

    /**
     * Logger.
     */
    static Logger log = LoggerFactory.getLogger(Content2BeanProcessorImpl.class);

    protected Content2BeanTransformerImpl defaultTransformer = new Content2BeanTransformerImpl();

   /**
     * Transforms the nodes data into a map containting the names and values. In case recursive is true the subnodes are
     * transformed to maps as well
     * @param node
     * @return a flat map
     */
    public Map toMap(Content node, boolean recursive) throws Content2BeanException {
        return (Map) toBean(node, recursive, TO_MAP_TRANSFORMER);
    }

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
            throw new Content2BeanException("can't resolve class for node " +  node.getHandle(), e);
        }

        state.pushType(type);

        Map values = toMap(node);

        if(recursive){
            Collection children = node.getChildren(transformer);
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                final Content childNode = (Content) iter.next();
                // in case the the class can not get resolved we can use now
                // the parent bean to resolve the class

                Object childBean = toBean(childNode, true, transformer, state);
                values.put(childNode.getName(), childBean);
            }
        }


        Object bean = transformer.newBeanInstance(state, values);

        state.pushBean(bean);

        setProperties(values, transformer, state);

        transformer.initBean(state, values);

        state.popType();
        state.popBean();
        state.popContent();

        return bean;
    }

    public Object setProperties(final Object bean, Content node, boolean recursive, final Content2BeanTransformer transformer) throws Content2BeanException {
        TransformationState state = transformer.newState();
        state.pushBean(bean);
        // TODO this is ugly
        state.pushType(TypeMapping.Factory.getDefaultMapping().getTypeDescriptor(bean.getClass()));

        Map values = toMap(node, recursive);

        setProperties(values, transformer, state);

        transformer.initBean(state, values);

        state.popBean();
        state.popType();

        return bean;
    }


    protected Map toMap(Content node) throws Content2BeanException {
        Map map = new HashMap();
        for (Iterator iter = node.getNodeDataCollection().iterator(); iter.hasNext();) {
            NodeData nd = (NodeData) iter.next();
            Object val = NodeDataUtil.getValueObject(nd);
            if (val != null) {
                map.put(nd.getName(), val);
            }
        }
        return map;
    }

    protected void setProperties(Map values, final Content2BeanTransformer transformer, TransformationState state) throws Content2BeanException {
        Object bean = state.getCurrentBean();
        if(bean instanceof Map){
            ((Map)bean).putAll(values);
        }
        else{
            Collection dscrs = state.getCurrentType().getPropertyDescriptors().values();
            for (Iterator iter = dscrs.iterator(); iter.hasNext();) {
                PropertyTypeDescriptor descriptor = (PropertyTypeDescriptor) iter.next();
                transformer.setProperty(state, descriptor, values);
            }
        }
    }

}
