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
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataUtil;

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

        public Class resolveClass(TransformationState state) throws ClassNotFoundException {
            return HashMap.class;
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

        Class klass = null;
        try {
            klass = transformer.resolveClass(state);
        }
        catch (ClassNotFoundException e) {
            throw new Content2BeanException("can't resolve class for node " +  node.getHandle(), e);
        }

        state.pushClass(klass);

        Map properties = toMap(node);

        if(recursive){
            Collection children = node.getChildren(transformer);
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                final Content childNode = (Content) iter.next();
                // in case the the class can not get resolved we can use now
                // the parent bean to resolve the class

                Object childBean = toBean(childNode, true, transformer, state);
                properties.put(childNode.getName(), childBean);
            }
        }


        Object bean = transformer.newBeanInstance(state, properties);

        state.pushBean(bean);

        if(!(bean instanceof Map) && !properties.containsKey("content")){
            properties.put("content", node);
        }

        if(!(bean instanceof Map) && !properties.containsKey("name")){
            properties.put("name", node.getName());
        }

        setProperties(properties, transformer, state);

        transformer.initBean(state, properties);

        state.popClass();
        state.popBean();
        state.popContent();

        return bean;
    }

    public Object setProperties(final Object bean, Content node, boolean recursive, final Content2BeanTransformer transformer) throws Content2BeanException {
        TransformationState state = transformer.newState();
        state.pushBean(bean);
        state.pushClass(bean.getClass());

        Map properties = toMap(node, recursive);

        setProperties(properties, transformer, state);

        state.popBean();
        state.popClass();

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

    protected void setProperties(Map properties, final Content2BeanTransformer transformer, TransformationState state) throws Content2BeanException {
        for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
            String propertyName = (String) iter.next();
            transformer.setProperty(state, propertyName, properties.get(propertyName));
        }
    }

    public Content2BeanTransformerImpl getDefaultContentToBeanTransformer() {
        return defaultTransformer;
    }
}
