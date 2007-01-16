/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Content.ContentFilter;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some easy to use methods to handle with Content objects.
 * @author philipp
 */
public class ContentUtil {

    private static Logger log = LoggerFactory.getLogger(ContentUtil.class);

    /**
     * Content filter accepting everything
     */
    public static ContentFilter ALL_NODES_CONTENT_FILTER = new ContentFilter() {

        public boolean accept(Content content) {
            return true;
        }
    };
    
    /**
     * @author Philipp Bracher
     * @version $Id$
     *
     */
    public interface Visitor {
        void visit(Content node) throws Exception;
    }
    
    /**
     * Content filter accepting everything
     */
    public static ContentFilter EXCLUDE_META_DATA_CONTENT_FILTER = new ContentFilter() {
        public boolean accept(Content content) {
            return !content.getName().startsWith("jcr:") && !content.isNodeType(ItemType.NT_METADATA);
        }
    };

    /**
     * Returns a Content object of the named repository or null if not existing.
     * @param repository
     * @param path
     * @return null if not found
     */
    public static Content getContent(String repository, String path) {
        try {
            return MgnlContext.getHierarchyManager(repository).getContent(path);
        }
        catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * Get the node or null if not exists
     * @param node
     * @param name
     * @return the sub node
     */
    public static Content getContent(Content node, String name) {
        try {
            return node.getContent(name);
        }
        catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * If the node doesn't exist just create it.
     * @param node
     * @param name
     * @param contentType
     * @return
     * @throws AccessDeniedException
     * @throws RepositoryException
     */
    public static Content getOrCreateContent(Content node, String name, ItemType contentType)
        throws AccessDeniedException, RepositoryException {
        Content res = null;
        try {
            res = node.getContent(name);
        }
        catch (PathNotFoundException e) {
            res = node.createContent(name, contentType);
        }
        return res;
    }

    /**
     * Get a subnode case insensitive.
     * @param node
     * @param name
     * @param type
     * @return
     */
    public static Content getCaseInsensitive(Content node, String name) {
        if (name == null || node == null) {
            return null;
        }
        name = name.toLowerCase();
        for (Iterator iter = node.getChildren(ALL_NODES_CONTENT_FILTER).iterator(); iter.hasNext();) {
            Content child = (Content) iter.next();
            if (child.getName().toLowerCase().equals(name)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Get all children recursively (content and contentnode)
     */
    public static List collectAllChildren(Content node) {
        List nodes = new ArrayList();
        return collectAllChildren(nodes, node, new ItemType[]{ItemType.CONTENT, ItemType.CONTENTNODE});
    }

    /**
     * Get all children using a filter
     * @param node
     * @param filter
     * @return list of all found nodes
     */
    public static List collectAllChildren(Content node, ContentFilter filter) {
        List nodes = new ArrayList();
        return collectAllChildren(nodes, node, filter);
    }

    /**
     * Get the children using a filter
     * @param nodes collection of already found nodes
     * @param node
     * @param filter the filter to use
     * @return
     */
    private static List collectAllChildren(List nodes, Content node, ContentFilter filter) {
        // get filtered sub nodes first
        Collection children = node.getChildren(filter);
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            Content child = (Content) iter.next();
            nodes.add(child);
        }

        // get all children to find recursively
        Collection allChildren = node.getChildren(EXCLUDE_META_DATA_CONTENT_FILTER);

        // recursion
        for (Iterator iter = allChildren.iterator(); iter.hasNext();) {
            Content child = (Content) iter.next();
            collectAllChildren(nodes, child, filter);
        }

        return nodes;
    }

    /**
     * Get all children of a particular type
     * @param node
     * @param type
     * @return
     */
    public static List collectAllChildren(Content node, ItemType type) {
        List nodes = new ArrayList();
        return collectAllChildren(nodes, node, new ItemType[]{type});
    }
    
    /**
     * Returns all children (not recursively) indpendent of there type
     * @param node
     * @return
     */
    public static Collection getAllChildren(Content node){
        return node.getChildren(EXCLUDE_META_DATA_CONTENT_FILTER);
    }

    /**
     * Get all children of a particular type
     * @param node
     * @param type
     * @return
     */
    public static List collectAllChildren(Content node, ItemType[] types) {
        List nodes = new ArrayList();
        return collectAllChildren(nodes, node, types);
    }

    /**
     * Get all subnodes recursively and add them to the nodes collection.
     * @param nodes
     * @param node
     * @param types
     * @return the list
     */
    private static List collectAllChildren(List nodes, Content node, ItemType[] types) {
        for (int i = 0; i < types.length; i++) {
            ItemType type = types[i];

            Collection children = node.getChildren(type);
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                Content child = (Content) iter.next();
                nodes.add(child);
                collectAllChildren(nodes, child, types);
            }
        }
        return nodes;
    }
    
    public static void orderNodes(Content node, String[] nodes) throws RepositoryException{
        for (int i = nodes.length - 1; i > 0; i--) {
            node.orderBefore(nodes[i-1], nodes[i]);
        }
        node.save();
    }
    
    public static void visit(Content node, Visitor visitor) throws Exception{
        visit(node, visitor, EXCLUDE_META_DATA_CONTENT_FILTER);
    }
    
    public static void visit(Content node, Visitor visitor, ContentFilter filter) throws Exception{
        visitor.visit(node);
        for (Iterator iter = node.getChildren(filter).iterator(); iter.hasNext();) {
            visit((Content) iter.next(), visitor);
        }
    }

    public static Content createPath(HierarchyManager hm, String path) throws AccessDeniedException,
        PathNotFoundException, RepositoryException {
        return ContentUtil.createPath(hm, path, ItemType.CONTENT);
    }

    public static Content createPath(HierarchyManager hm, String path, ItemType type) throws AccessDeniedException,
        PathNotFoundException, RepositoryException {
        Content node = hm.getRoot();
        return createPath(node, path, type);
    }

    /**
     * @param node
     * @param path
     * @param type
     * @return
     * @throws RepositoryException
     * @throws PathNotFoundException
     * @throws AccessDeniedException
     */
    public static Content createPath(Content node, String path, ItemType type) throws RepositoryException,
        PathNotFoundException, AccessDeniedException {
        // remove leading /
        path = StringUtils.removeStart(path, "/");

        if (StringUtils.isEmpty(path)) {
            return node;
        }

        String[] names = path.split("/"); //$NON-NLS-1$

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (node.hasContent(name)) {
                node = node.getContent(name);
            }
            else {
                node = node.createContent(name, type);
            }
        }
        return node;
    }

    /**
     * Transforms the nodes data into a map containting the names and values.
     * @param node
     * @return a flat map
     */
    public static Map toMap(Content node) {
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

    /**
     * Takes a nodes data and and sets the beans properties which follow the naming of the nodes nodedatas.
     * @param bean the bean you like to populate
     * @param node the node containing the data
     * @return the bean
     */
    public static Object setProperties(Object bean, Content node) {
        try {
            BeanUtils.populate(bean, toMap(node));
        }
        catch (IllegalAccessException e) {
            log.error("can't set properties", e);
        }
        catch (InvocationTargetException e) {
            log.error("can't set properties", e);
        }
        return bean;
    }

    /**
     * Takes a nodes data and and sets the beans properties which follow the naming of the nodes nodedatas.
     * @param bean the bean you like to populate
     * @param repository
     * @param path
     * @return the bean
     */
    public static Object setProperties(Object bean, String repository, String path) {
        Content node = getContent(repository, path);
        if (node != null) {
            setProperties(bean, node);
        }
        return bean;
    }

    public static void setNodeDatas(Content node, Object obj) throws RepositoryException {
        try {
            setNodeDatas(node, BeanUtils.describe(obj));
        }
        catch (InvocationTargetException e) {
            log.error("can't persist", e);
        }
        catch (NoSuchMethodException e) {
            log.error("can't persist", e);
        }
        catch (IllegalAccessException e) {
            log.error("can't persist", e);
        }
    }

    public static void setNodeDatas(Content node, Map map) throws RepositoryException {
        for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            NodeDataUtil.getOrCreate(node, name).setValue(map.get(name).toString());
        }
    }

    public static void setNodeDatas(Content node, Object bean, String[] excludes) throws RepositoryException {
        try {
            Map properties = BeanUtils.describe(bean);
            for (int i = 0; i < excludes.length; i++) {
                String exclude = excludes[i];
                properties.remove(exclude);
            }
            setNodeDatas(node, properties);
        }
        catch (InvocationTargetException e) {
            log.error("can't persist", e);
        }
        catch (NoSuchMethodException e) {
            log.error("can't persist", e);
        }
        catch (IllegalAccessException e) {
            log.error("can't persist", e);
        }
    }
    
    public static String uuid2path(String repository, String uuid){
        if(StringUtils.isNotEmpty(uuid)){
            HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
            try {
                Content node = hm.getContentByUUID(uuid);
                return node.getHandle();
            }
            catch (Exception e) {
                // return the uuid
            }
            
        }
        return uuid;
    }

}