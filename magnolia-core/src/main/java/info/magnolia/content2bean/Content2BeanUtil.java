/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
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

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import org.apache.commons.beanutils.BeanUtils;

/**
 * Utility class for content to bean transformations.
 * In case you do not have to customize the transformation, you should use one of these methods:
 * <ul>
 * <li><code>toMap</code> is used to build a map from a node
 * <li><code>toBean</code> transforms the nodes to beans
 * <li><code>setProperties</code> tries to set the properties on the bean passed to the method
 * <li><code>setNodeData</code> set the nodedatas based on the bean you pass
 * </ul>
 * @author philipp
 * @version $Id$
 */
public class Content2BeanUtil {

    /**
     * Transforms all nodes to a map.
     * @deprecated since 5.0 - use {@link ToMapTransformer}.
     */
    public static final Content2BeanTransformerImpl TO_MAP_TRANSFORMER = new ToMapTransformer();

    /**
     * Provide a default class.
     */
    public static final class DefaultClassTransformer extends Content2BeanTransformerImpl {

//        private TypeDescriptor defaultType;
        private final Class defaultClass;
        public DefaultClassTransformer(Class defaultClass) {
            // TODO - can't do this anymore since typeMapping is @Inject or passed, i.e not here yet.
            // this.defaultType = getTypeMapping().getTypeDescriptor(defaultClass);
            this.defaultClass = defaultClass;
        }

        protected TypeDescriptor onResolveType(TypeMapping typeMapping, TransformationState state, TypeDescriptor resolvedType) {
            // return resolvedType==null? defaultType : resolvedType;
            return resolvedType==null? typeMapping.getTypeDescriptor(defaultClass) : resolvedType;
        }
    }

    /**
     * A {@link Content2BeanTransformer} transforming all nodes to Maps.
     */
    public static class ToMapTransformer extends Content2BeanTransformerImpl {
        public ToMapTransformer() {
        }

        @Override
        public TypeDescriptor resolveType(TypeMapping typeMapping, TransformationState state) throws ClassNotFoundException {
            return TypeMapping.MAP_TYPE;
        }
    }

    /**
     * Get the current processor.
     * @deprecated since 5.0, use IoC. - TODO only used locally
     */
    public static Content2BeanProcessor getContent2BeanProcessor() {
        return Components.getSingleton(Content2BeanProcessor.class);
    }

    /**
     * Get the current processor.
     * @deprecated since 5.0 - unused, Bean2Content is not implemented yet.
     */
    public static Bean2ContentProcessor getBean2ContentProcessor() {
        return Bean2ContentProcessor.Factory.getProcessor();
    }

    /**
     * Get the current mapping.
     * @deprecated since 5.0, use IoC.
     */
    public static TypeMapping getTypeMapping() {
        return Components.getSingleton(TypeMapping.class);
    }

    /**
     * Get the current transformer.
     * @deprecated since 5.0, use IoC.
     */
    public static Content2BeanTransformer getContent2BeanTransformer() {
        return Components.getSingleton(Content2BeanTransformer.class);
    }

    /**
     * @see Content2BeanProcessor
     * @deprecated since 5.0 - only used in tests - use {@link Content2Bean}
     */
    public static Object toBean(Content node) throws Content2BeanException {
        return toBean(node, false);
    }

    /**
     * @see Content2BeanProcessor
     * @deprecated since 5.0 - only used in tests - use {@link Content2Bean}
     */
    public static Object toBean(Content node, final Class defaultClass) throws Content2BeanException {
        return toBean(node, false, new DefaultClassTransformer( defaultClass));
    }

    /**
     * @see Content2BeanProcessor
     * @deprecated since 5.0 - TODO used in FilterManagerImpl, ParagraphManager and TemplateManager - use {@link Content2Bean}
     */
    public static Object toBean(Content node, boolean recursive, final Class defaultClass) throws Content2BeanException {
        return toBean(node, recursive, new DefaultClassTransformer(defaultClass));
    }

    /**
     * @see Content2BeanProcessor
     * @deprecated since 5.0- only used in DelegateVoter - use {@link Content2Bean}
     */
    public static Object toBean(Content node, boolean recursive) throws Content2BeanException {
        return toBean(node, recursive, getContent2BeanTransformer());
    }

    /**
     * @see Content2BeanProcessor
     * @deprecated since 5.0- only used in DelegateVoter - use {@link Content2Bean}
     */
    public static Object toBean(Content node, boolean recursive, ComponentProvider componentProvider) throws Content2BeanException {
        return toBean(node, recursive, getContent2BeanTransformer(), componentProvider);
    }

    /**
     * @see Content2BeanProcessor
     * @deprecated since 5.0 use {@link Content2Bean}
     * TODO -- this method has a bunch of usage points
     */
    public static Object toBean(Content node, boolean recursive, Content2BeanTransformer transformer) throws Content2BeanException {
        return toBean(node, recursive, transformer, Components.getComponentProvider());
    }

    /**
     * @see Content2BeanProcessor
     * @deprecated since 5.0 use {@link Content2Bean}
     * TODO -- this method has a bunch of usage points
     */
    public static Object toBean(Content node, boolean recursive, Content2BeanTransformer transformer, ComponentProvider componentProvider) throws Content2BeanException {
        return getContent2BeanProcessor().toBean(node, recursive, transformer, componentProvider);
    }


    /**
     * Transforms the nodes data into a map containing the names and values.
     * @return a flat map
     * @deprecated since 5.0 - not used - use {@link Content2Bean}
     */
    public static Map toMap(Content node) throws Content2BeanException {
        return toMap(node, false);
    }


    /**
     * Transforms the nodes data into a map containing the names and values. In case recursive is true the sub-nodes are
     * transformed to beans using the transformer. To avoid that use toMaps() instead
     * @deprecated since 5.0 - only used in info.magnolia.setup.for3_5.UpdateI18nConfiguration - use {@link Content2Bean}
     */
    public static Map toMap(Content node, boolean recursive) throws Content2BeanException {
        return (Map) setProperties(new LinkedHashMap(), node, recursive);
    }

    /**
     * @deprecated since 5.0 - TODO only used in DefaultMessagesManager - use {@link Content2Bean}
     */
    public static <K, V> Map<K, V> toMap(Content node, boolean recursive, Class defaultClass) throws Content2BeanException {
        return (Map<K, V>) setProperties(new LinkedHashMap(), node, recursive, defaultClass);
    }

    /**
     * Transforms the nodes data into a map containing the names and values. In case recursive is true the sub-nodes are
     * transformed to maps as well
     * @deprecated since 5.0 - TODO only used in info.magnolia.setup.for3_5.CheckAndUpdateExistingFilters - use {@link Content2Bean}
     */
    public static Map toPureMaps(Content node, boolean recursive) throws Content2BeanException {
        return (Map) toBean(node, recursive, new ToMapTransformer());
    }

    /**
     * @see Content2BeanProcessor
     * @deprecated since 5.0 - unused - use {@link Content2Bean}
     */
    public static Object setProperties(Object bean, Content node) throws Content2BeanException {
        return setProperties(bean, node, false);
    }

    /**
     * @see Content2BeanProcessor
     * @deprecated since 5.0 - TODO - only used locally and by ModuleManagerImpl and TreeHandlerManager
     */
    public static Object setProperties(Object bean, Content node, boolean recursive) throws Content2BeanException {
        return setProperties(bean, node, recursive, getContent2BeanTransformer());
    }

    /**
     * @deprecated since 5.0 - TODO - only used locally - use {@link Content2Bean}
     */
    public static Object setProperties(Object bean, Content node, boolean recursive, Class defaultClass) throws Content2BeanException {
        return setProperties(bean, node, recursive, new DefaultClassTransformer( defaultClass));
    }

    /**
     * @see Content2BeanProcessor
     * @deprecated since 5.0 - use {@link Content2Bean}
     */
    public static Object setProperties(Object bean, Content node, boolean recursive, Content2BeanTransformer transformer) throws Content2BeanException {
        return setProperties(bean, node, recursive, transformer, Components.getComponentProvider());
    }

    /**
     * @see Content2BeanProcessor
     * @deprecated since 5.0 - use {@link Content2Bean}
     */
    public static Object setProperties(Object bean, Content node, boolean recursive, Content2BeanTransformer transformer, ComponentProvider componentProvider) throws Content2BeanException {
        return getContent2BeanProcessor().setProperties(bean, node, recursive, transformer, componentProvider);
    }


    /**
     * @see Content2BeanProcessor
     * @deprecated since 5.0 - only used in tests
     */
    public static void addCollectionPropertyMapping(Class type, String name, Class mappedType) {
        TypeMapping mapping = getTypeMapping();
        mapping.getPropertyTypeDescriptor(type, name).setCollectionEntryType(mapping.getTypeDescriptor(mappedType));
    }

    /**
     * TODO use the <code>Bean2ContentProcessor</code>.
     * @deprecated since 5.0 - unused - use {@link Content2Bean}
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
     * TODO use the <code>Bean2ContentProcessor</code>.
     * @deprecated since 5.0 - unused - use {@link Content2Bean}
     */
    public static void setNodeDatas(Content node, Object obj) throws Content2BeanException {
        setNodeDatas(node, toMap(obj));
    }

    /**
     * TODO use the <code>Bean2ContentProcessor</code>.
     * @deprecated since 5.0 - TODO - only used locally - use {@link Content2Bean}
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
     * Used to fake the <code>setNodeDatas()</code> methods.
     * @deprecated since 5.0 - TODO - only used locally - use {@link Content2Bean}
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

