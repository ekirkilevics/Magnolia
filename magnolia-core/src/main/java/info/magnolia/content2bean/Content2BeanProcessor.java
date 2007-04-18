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
import info.magnolia.cms.util.FactoryUtil;

import java.util.Map;


/**
 * Transforms nodes to beans or maps. The transformer is use to resolve classes or to instantiate beans.
 * @author philipp
 * @version $Id$
 */
public interface Content2BeanProcessor {

    /**
     * Transforms the nodes data into a map containting the names and values. In case recursive is true the subnodes are
     * transformed to maps as well
     * @throws Content2BeanException
     */
    public Map toMap(Content node, boolean recursive) throws Content2BeanException;

    /**
     * Transforms the node to a bean using the passed transformer
     */
    public Object toBean(Content node, boolean recursive, final Content2BeanTransformer transformer)
        throws Content2BeanException;

    /**
     * Similar to <code>toBean()</code> but uses a passed bean as the root bean
     */
    public Object setProperties(final Object bean, Content node, boolean recursive, final Content2BeanTransformer transformer) throws Content2BeanException;

    /**
     * Get your instance here
     */
    class Factory {

        public static Content2BeanProcessor getProcessor() {
            return (Content2BeanProcessor) FactoryUtil.getSingleton(Content2BeanProcessor.class);
        }
    }

}