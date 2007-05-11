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
import info.magnolia.cms.i18n.LanguageDefinition;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.BeanUtils;
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
     * Transforms all nodes to a map
     */
    public static final Content2BeanTransformerImpl TO_MAP_TRANSFORMER = new Content2BeanTransformerImpl() {

        public TypeDescriptor resolveType(TransformationState state) throws ClassNotFoundException {
            return TypeMapping.MAP_TYPE;
        }
    };

    /**
     * Provide a default class.
     */
    public static final class DefaultClassTransformer extends Content2BeanTransformerImpl {

        private TypeDescriptor defaultType;

        public DefaultClassTransformer(Class defaultClass) {
            this.defaultType = TypeMapping.Factory.getDefaultMapping().getTypeDescriptor(defaultClass);
        }

        protected TypeDescriptor onResolveClass(TransformationState state) {
            return this.defaultType;
        }
    }

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
     * Get the current mapping
     */
    public static TypeMapping getTypeMapping() {
        return TypeMapping.Factory.getDefaultMapping();
    }

    /**
     * Get the current transformer
     */
    public static Content2BeanTransformer getContent2BeanTransformer() {
        return Content2BeanTransformer.Factory.getDefaultTransformer();
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
        return toBean(node, false, new DefaultClassTransformer(defaultClass));
    }

    /**
     * @see Content2BeanProcessor
     */
    public static Object toBean(Content node, boolean recursive, final Class defaultClass) throws Content2BeanException {
        return toBean(node, recursive, new DefaultClassTransformer(defaultClass));
    }

    /**
     * @see Content2BeanProcessor
     */
    public static Object toBean(Content node, boolean recursive) throws Content2BeanException {
        return toBean(node, recursive, getContent2BeanTransformer());
    }

    /**
     * @see Content2BeanProcessor
     */
    public static Object toBean(Content node, boolean recursive, Content2BeanTransformer transformer) throws Content2BeanException {
        return getContent2BeanProcessor().toBean(node, recursive, transformer);
    }

    /**
     * Transforms the nodes data into a map containting the names and values.
     * @param node
     * @return a flat map
     */
    public static Map toMap(Content node) throws Content2BeanException {
        return toMap(node, false);
    }


    /**
     * Transforms the nodes data into a map containting the names and values. In case recursive is true the subnodes are
     * transformed to beans using the transformer. To avoid that use toMaps() instead
     */
    public static Map toMap(Content node, boolean recursive) throws Content2BeanException {
        return (Map) setProperties(new LinkedHashMap(), node, recursive);
    }

    public static Map toMap(Content node, boolean recursive, Class defaultClass) throws Content2BeanException {
        return (Map) setProperties(new LinkedHashMap(), node, recursive, defaultClass);
    }

    /**
     * Transforms the nodes data into a map containting the names and values. In case recursive is true the subnodes are
     * transformed to maps as well
     */
    public static Map toPureMaps(Content node, boolean recursive) throws Content2BeanException {
        return (Map) toBean(node, recursive, TO_MAP_TRANSFORMER);
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
        return setProperties(bean, node, recursive, getContent2BeanTransformer());
    }

    public static Object setProperties(Object bean, Content node, boolean recursive, Class defaultClass) throws Content2BeanException {
        return setProperties(bean, node, recursive, new DefaultClassTransformer(defaultClass));
    }
    /**
     * @see Content2BeanProcessor
     */
    public static Object setProperties(Object bean, Content node, boolean recursive, Content2BeanTransformer transformer) throws Content2BeanException {
        return getContent2BeanProcessor().setProperties(bean, node, recursive, transformer);
    }

    /**
     * @see Content2BeanProcessor
     */
    public static void addCollectionPropertyMapping(Class type, String name, Class mappedType) {
        TypeMapping mapping = getTypeMapping();
        mapping.getPropertyTypeDescriptor(type, name).setCollectionEntryType(mapping.getTypeDescriptor(mappedType));
    }

    /**
     * @todo use the Bean2ContentProcessor
     */
    public static void setNodeDatas(Content node, Object bean, String[] excludes) throws Content2BeanException {
        Map properties = toMap(bean);

        for (int i = 0; i < excludes.length; i++) {
            String exclude = excludes[i];
            properties.remove(exclude);
        }
    }

    /**
     * @todo use the Bean2ContentProcessor
     */
    public static void setNodeDatas(Content node, Object obj) throws Content2BeanException {
        setNodeDatas(node, toMap(obj));
    }

    /**
     * @todo use the Bean2ContentProcessor
     */
    public static void setNodeDatas(Content node, Map map) throws Content2BeanException {
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

    /**
     * Used to fake the setNodeDatas() methods
     */
    static private Map toMap(Object bean) throws Content2BeanException {
        try {
            return BeanUtils.describe(bean);
        }
        catch (Exception e) {
            throw new Content2BeanException("can't read properties from bean", e);
        }
    }



}

