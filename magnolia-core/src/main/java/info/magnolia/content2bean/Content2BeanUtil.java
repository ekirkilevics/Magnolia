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
 * @author philipp
 * @version $Id$
 *
 */
public class Content2BeanUtil {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Content2BeanUtil.class);


    public static Object toBean(Content node) throws Content2BeanException{
        return toBean(node, false);
    }

    public static Object toBean(Content node, final Class defaultClass) throws Content2BeanException{
        return Content2BeanProcessor.getInstance().toBean(node, false, new Content2BeanTransformerImpl(){
            protected Class onResolveClass(Content node) {
                return defaultClass;
            }
        });
    }

    public static Object toBean(Content node, boolean recursive) throws Content2BeanException{
        return Content2BeanProcessor.getInstance().toBean(node, recursive, Content2BeanProcessor.getInstance().getDefaultContentToBeanTransformer());
    }

    public static Map toMap(Content node) {
        return toMap(node, false);
    }

    public static Map toMap(Content node, boolean recursive) {
        return Content2BeanProcessor.getInstance().toMap(node, recursive);
    }

    public static void addMapPropertyType(Class type, String name, Class mappedType) {
        Content2BeanProcessor.getInstance().addMapPropertyType(type, name, mappedType);
    }

    public static Object setProperties(Object bean, Content node, boolean recursive) throws Content2BeanException {
        return Content2BeanProcessor.getInstance().setProperties(bean, node, recursive, Content2BeanProcessor.getInstance().getDefaultContentToBeanTransformer());
    }

    public static Object setProperties(Object bean, Content node) throws Content2BeanException {
        return setProperties(bean, node, false);
    }

    public static void setNodeDatas(Content node, Map map) throws Content2BeanException {
       Bean2ContentProcessor.getInstance().setNodeDatas(node, map);
    }

    public static void setNodeDatas(Content node, Object bean, String[] excludes) throws Content2BeanException {
        Bean2ContentProcessor.getInstance().setNodeDatas(node, bean, excludes);
    }

    public static void setNodeDatas(Content node, Object obj) throws Content2BeanException {
        Bean2ContentProcessor.getInstance().setNodeDatas(node, obj);
    }

}
