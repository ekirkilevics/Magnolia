/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.content2bean;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In case you do not have to customize the transformation, you should use one of these methods
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
    private static final Logger log = LoggerFactory.getLogger(Content2BeanUtil.class);

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
            this.defaultType = getTypeMapping().getTypeDescriptor(defaultClass);
        }

        protected TypeDescriptor onResolveType(TransformationState state, TypeDescriptor resolvedType) {
            return resolvedType==null? defaultType : resolvedType;
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

        setNodeDatas(node, properties);
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

