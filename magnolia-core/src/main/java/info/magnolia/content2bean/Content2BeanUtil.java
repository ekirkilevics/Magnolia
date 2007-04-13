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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * In case you do not have to customize the transformation you should use one of this methods
 * <ul>
 * <li><code>toMap</code> is used to build a map from a node
 * <li><code>toBean<code> transforms the nodes to beans
 * <li><code>setProperties<code> tries to set the properties on the bean passed to the method
 * <li><code>setNodeData<code> set the nodedatas based on the bean you pass
 * </ul>
 * @author philipp
 * @version $Id$
 */
public class Content2BeanUtil {

    private static Logger log = LoggerFactory.getLogger(Content2BeanUtil.class);

    /**
     * Get the current processor
     */
    public static Content2BeanProcessor getContent2BeanProcessor() {
        return Content2BeanProcessor.Factory.getProcessor();
    }

    /**
     * Get the current processor
     */
    public static Bean2ContentProcessor getBean2ContentProcessor() {
        return Bean2ContentProcessor.Factory.getProcessor();
    }

    /**
     * @see Content2BeanProcessor
     */
    public static Object toBean(Content node) throws Content2BeanException {
        return toBean(node, false);
    }

    /**
     * @see Content2BeanProcessor
     */
    public static Object toBean(Content node, final Class defaultClass) throws Content2BeanException {
        return toBean(node, false, new Content2BeanTransformerImpl() {

            protected Class onResolveClass(Content node) {
                return defaultClass;
            }
        });
    }

    /**
     * @see Content2BeanProcessor
     */
    public static Object toBean(Content node, boolean recursive) throws Content2BeanException {
        return toBean(node, recursive, Content2BeanUtil.getContent2BeanProcessor().getDefaultContentToBeanTransformer());
    }

    /**
     * @see Content2BeanProcessor
     */
    public static Object toBean(Content node, boolean recursive, Content2BeanTransformer transformer) throws Content2BeanException {
        return getContent2BeanProcessor().toBean(node, recursive, transformer);
    }

    /**
     * @see Content2BeanProcessor
     */
    public static Map toMap(Content node) {
        return toMap(node, false);
    }

    /**
     * @see Content2BeanProcessor
     */
    public static Map toMap(Content node, boolean recursive) {
        return getContent2BeanProcessor().toMap(node, recursive);
    }

    /**
     * @see Content2BeanProcessor
     */
    public static Object setProperties(Object bean, Content node) throws Content2BeanException {
        return setProperties(bean, node, false);
    }

    /**
     * @see Content2BeanProcessor
     */
    public static Object setProperties(Object bean, Content node, boolean recursive) throws Content2BeanException {
        return getContent2BeanProcessor().setProperties(bean, node, recursive, Content2BeanUtil.getContent2BeanProcessor().getDefaultContentToBeanTransformer());
    }

    /**
     * @see Content2BeanProcessor
     */
    public static void addMapPropertyType(Class type, String name, Class mappedType) {
        getContent2BeanProcessor().getDefaultContentToBeanTransformer().addCollectionPropertyClass(type, name, mappedType);
    }

    /**
     * @see Bean2ContentProcessor
     */
    public static void setNodeDatas(Content node, Map map) throws Content2BeanException {
        getBean2ContentProcessor().setNodeDatas(node, map);
    }

    /**
     * @see Bean2ContentProcessor
     */
    public static void setNodeDatas(Content node, Object bean, String[] excludes) throws Content2BeanException {
        getBean2ContentProcessor().setNodeDatas(node, bean, excludes);
    }

    /**
     * @see Bean2ContentProcessor
     */
    public static void setNodeDatas(Content node, Object obj) throws Content2BeanException {
        getBean2ContentProcessor().setNodeDatas(node, obj);
    }

}
