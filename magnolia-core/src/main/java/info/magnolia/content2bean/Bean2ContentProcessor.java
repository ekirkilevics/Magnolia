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
 * Transforms beans to nodes. Uncomplete.
 * @author philipp
 * @version $Id$
 *
 */
public interface Bean2ContentProcessor {

    public void setNodeDatas(Content node, Object bean) throws Content2BeanException;

    public void setNodeDatas(Content node, Map map) throws Content2BeanException;

    public void setNodeDatas(Content node, Object bean, String[] excludes) throws Content2BeanException;

    /**
     * Get your instance here
     */
    class Factory{
        public static Bean2ContentProcessor getProcessor(){
            return (Bean2ContentProcessor) FactoryUtil.getSingleton(Bean2ContentProcessor.class);
        }
    }
}