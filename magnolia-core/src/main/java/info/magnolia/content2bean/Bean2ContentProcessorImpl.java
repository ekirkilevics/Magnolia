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
import info.magnolia.cms.util.NodeDataUtil;

import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * First uncomplete implementation.
 * @author philipp
 * @version $Id$
 *
 */
public class Bean2ContentProcessorImpl implements Bean2ContentProcessor {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Bean2ContentProcessorImpl.class);

    public void setNodeDatas(Content node, Object obj) throws Content2BeanException {
        setNodeDatas(node, propertiesAsMap(obj));
    }

    public void setNodeDatas(Content node, Map map) throws Content2BeanException {
        for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            try {
                NodeDataUtil.getOrCreate(node, name).setValue(map.get(name).toString());
            }
            catch (RepositoryException e) {
                throw new Content2BeanException("can't set/create nodedata [" + name + "] on node " + node.getHandle(), e);
            }
        }
    }

    public void setNodeDatas(Content node, Object bean, String[] excludes) throws Content2BeanException {
        Map properties = propertiesAsMap(bean);

        for (int i = 0; i < excludes.length; i++) {
            String exclude = excludes[i];
            properties.remove(exclude);
        }
        setNodeDatas(node, properties);
    }

    protected Map propertiesAsMap(Object bean) throws Content2BeanException {
        try {
            return BeanUtils.describe(bean);
        }
        catch (Exception e) {
            throw new Content2BeanException("can't read properties from bean", e);
        }
    }

}
