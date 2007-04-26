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
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Content.ContentFilter;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.api.HierarchyManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some easy to use methods to handle with Content objects.
 * @author philipp
 */
public class ContentUtil {


    static Logger log = LoggerFactory.getLogger(ContentUtil.class);

    /**
     * Content filter accepting everything
     */
    public static ContentFilter ALL_NODES_CONTENT_FILTER = new ContentFilter() {

        public boolean accept(Content content) {
            return true;
        }
    };

    /**
     * Content filter accepting everything exept nodes with namespace jcr (version and system store)
     */
    public static ContentFilter ALL_NODES_EXCEPT_JCR_CONTENT_FILTER = new ContentFilter() {

        public boolean accept(Content content) {
            return !content.getName().startsWith("jcr:");
        }
    };

    /**
     * Content filter accepting everything exept meta data and jcr:
     */
    public static ContentFilter EXCLUDE_META_DATA_CONTENT_FILTER = new ContentFilter() {
        public boolean accept(Content content) {
            return !content.getName().startsWith("jcr:") && !content.isNodeType(ItemType.NT_METADATA);
        }
    };

    /**
     * Content filter accepting all nodes with a nodetype of namespace mgnl
     */
    public static ContentFilter MAGNOLIA_FILTER = new ContentFilter() {
        public boolean accept(Content content) {

            try {
                String nodetype = content.getNodeType().getName();
                // export only "magnolia" nodes
                return nodetype.startsWith("mgnl:");
            }
            catch (RepositoryException e) {
                log.error("Unable to read nodetype for node {}", content.getHandle());
            }
            return false;
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
     */
    public static Collection getAllChildren(Content node){
        return node.getChildren(EXCLUDE_META_DATA_CONTENT_FILTER);
    }

    /**
     * Returns all children (not recursively) indpendent of there type
     */
    public static Collection getAllChildren(Content node, Comparator comp){
        return node.getChildren(EXCLUDE_META_DATA_CONTENT_FILTER, comp);
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

    /**
     * Uses the passed comperater to create the jcr ordering of the children
     * @throws RepositoryException
     */
    public static void orderNodes(Content node, Comparator comparator) throws RepositoryException {
        Collection children = ContentUtil.getAllChildren(node, comparator);
        String[] names = new String[children.size()];

        int i = 0;
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            Content childNode = (Content) iter.next();
            names[i] = childNode.getName();
            i++;
        }
        orderNodes(node, names);
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
        return createPath(hm, path, false);
    }

    public static Content createPath(HierarchyManager hm, String path, boolean save) throws AccessDeniedException,
        PathNotFoundException, RepositoryException {
        return ContentUtil.createPath(hm, path, ItemType.CONTENT, save);
    }

    public static Content createPath(HierarchyManager hm, String path, ItemType type) throws AccessDeniedException,
        PathNotFoundException, RepositoryException {
        return createPath(hm, path, type, false);
    }

    public static Content createPath(HierarchyManager hm, String path, ItemType type, boolean save) throws AccessDeniedException,
        PathNotFoundException, RepositoryException {
        Content node = hm.getRoot();
        return createPath(node, path, type, save);
    }

    public static Content createPath(Content node, String path, ItemType type) throws RepositoryException,
        PathNotFoundException, AccessDeniedException {
        return createPath(node, path, type, false);
    }

    public static Content createPath(Content node, String path, ItemType type, boolean save) throws RepositoryException,
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
                if(save){
                    node.save();
                }
            }
        }
        return node;
    }

    /**
     * Transforms the nodes data into a map containting the names and values.
     * @param node
     * @return a flat map
     * @deprecated Use Content2BeanUtil instead
     */
    public static Map toMap(Content node) {
        try {
            return Content2BeanUtil.toMap(node);
        }
        // we ignore it to not change the method signatur
        catch (Content2BeanException e) {
            log.error("exception catched in deprecated method", e);
        }
        return null;
    }

    /**
     * Takes a nodes data and and sets the beans properties which follow the naming of the nodes nodedatas.
     * @param bean the bean you like to populate
     * @param node the node containing the data
     * @return the bean
     * @deprecated Use Use Content2BeanUtil instead
     */
    public static Object setProperties(Object bean, Content node) {
        try {
            return Content2BeanUtil.setProperties(bean, node);
        }
        catch (Content2BeanException e) {
            log.error("can't set properties", e);
        }
        return null;
    }

    /**
     * @deprecated Use Use Content2BeanUtil instead
     */
    public static void setNodeDatas(Content node, Object obj) throws RepositoryException {
        try {
            Content2BeanUtil.setNodeDatas(node, obj);
        }
        catch (Content2BeanException e) {
            log.error("can't set node datas", e);
        }
    }

    /**
     * @deprecated Use Content2BeanUtil instead
     */
    public static void setNodeDatas(Content node, Map map) throws RepositoryException {
        try {
            Content2BeanUtil.setNodeDatas(node, map);
        }
        catch (Content2BeanException e) {
            log.error("can't set node datas", e);
        }
    }

    /**
     * @deprecated Use Use Content2BeanUtil instead
     */
    public static void setNodeDatas(Content node, Object bean, String[] excludes) throws RepositoryException {
        try {
            Content2BeanUtil.setNodeDatas(node, bean, excludes);
        }
        catch (Content2BeanException e) {
            log.error("can't set node datas", e);
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