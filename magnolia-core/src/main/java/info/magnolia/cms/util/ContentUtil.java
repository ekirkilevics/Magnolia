/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.Content.ContentFilter;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some easy to use methods to handle with Content objects.
 * @author philipp
 */
public class ContentUtil {
    private final static Logger log = LoggerFactory.getLogger(ContentUtil.class);

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
     */ // TODO : throws RepositoryException or none, but not Exception !?
    public static interface Visitor {
        void visit(Content node) throws Exception;
    }

    public static interface PostVisitor extends Visitor {
        void postVisit(Content node) throws Exception;
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
     * @return null if not found
     */
    public static Content getContentByUUID(String repository, String uuid) {
        try {
            return MgnlContext.getHierarchyManager(repository).getContentByUUID(uuid);
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
     * If the node doesn't exist just create it. Attention the method does not save the newly created node.
     */
    public static Content getOrCreateContent(Content node, String name, ItemType contentType) throws AccessDeniedException, RepositoryException{
        return getOrCreateContent(node, name, contentType, false);
    }

    /**
     * If the node doesn't exist just create it. If the parameter save is true the parent node is saved.
     */
    public static Content getOrCreateContent(Content node, String name, ItemType contentType, boolean save)
        throws AccessDeniedException, RepositoryException {
        Content res = null;
        try {
            res = node.getContent(name);
        }
        catch (PathNotFoundException e) {
            res = node.createContent(name, contentType);
            if(save){
                res.getParent().save();
            }
        }
        return res;
    }

    /**
     * Get a subnode case insensitive.
     * @param node
     * @param name
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
     * Returns all children (not recursively) independent of there type
     */
    public static Collection getAllChildren(Content node){
        return node.getChildren(EXCLUDE_META_DATA_CONTENT_FILTER);
    }

    /**
     * Returns all children (not recursively) independent of there type
     */
    public static Collection getAllChildren(Content node, Comparator comp){
        return node.getChildren(EXCLUDE_META_DATA_CONTENT_FILTER, comp);
    }

    /**
     * Get all children of a particular type
     * @param node
     * @param types
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
            visit((Content) iter.next(), visitor, filter);
        }
        if(visitor instanceof PostVisitor){
            ((PostVisitor)visitor).postVisit(node);
        }
    }

    public static Content createPath(HierarchyManager hm, String path) throws AccessDeniedException,
        PathNotFoundException, RepositoryException {
        return createPath(hm, path, false);
    }

    public static Content createPath(HierarchyManager hm, String path, boolean save) throws AccessDeniedException,
        PathNotFoundException, RepositoryException {
        return createPath(hm, path, ItemType.CONTENT, save);
    }

    public static Content createPath(HierarchyManager hm, String path, ItemType type) throws AccessDeniedException,
        PathNotFoundException, RepositoryException {
        return createPath(hm, path, type, false);
    }

    public static Content createPath(HierarchyManager hm, String path, ItemType type, boolean save) throws AccessDeniedException,
        PathNotFoundException, RepositoryException {
        Content root = hm.getRoot();
        return createPath(root, path, type, save);
    }

    public static Content createPath(Content parent, String path, ItemType type) throws RepositoryException,
        PathNotFoundException, AccessDeniedException {
        return createPath(parent, path, type, false);
    }

    public static Content createPath(Content parent, String path, ItemType type, boolean save) throws RepositoryException,
        PathNotFoundException, AccessDeniedException {
        // remove leading /
        path = StringUtils.removeStart(path, "/");

        if (StringUtils.isEmpty(path)) {
            return parent;
        }

        String[] names = path.split("/"); //$NON-NLS-1$

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (parent.hasContent(name)) {
                parent = parent.getContent(name);
            }
            else {
                final Content newNode = parent.createContent(name, type);
                if(save){
                    parent.save();
                }
                parent = newNode;
            }
        }
        return parent;
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

    public static String path2uuid(String repository, String path) {
        if(StringUtils.isNotEmpty(path)){
            HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
            try {
                Content node = hm.getContent(path);
                return node.getUUID();
            }
            catch (Exception e) {
                // return the uuid
            }

        }
        return path;
    }

    public static void deleteAndRemoveEmptyParents(Content node) throws PathNotFoundException, RepositoryException,
    AccessDeniedException {
        deleteAndRemoveEmptyParents(node, 0);
    }

    public static void deleteAndRemoveEmptyParents(Content node, int level) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Content parent = null;
        if(node.getLevel() != 0){
             parent = node.getParent();
        }
        node.delete();
        if(parent != null && parent.getLevel()>level && parent.getChildren(ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER).size()==0){
            deleteAndRemoveEmptyParents(parent, level);
        }
    }

    /**
     * Session based copy operation. As JCR only supports workspace based copies this operation is performed
     * by using export import operations.
     */
    public static void copyInSession(Content src, String dest) throws RepositoryException {
        final String destParentPath = StringUtils.defaultIfEmpty(StringUtils.substringBeforeLast(dest, "/"), "/");
        final String destNodeName = StringUtils.substringAfterLast(dest, "/");
        final Session session = src.getWorkspace().getSession();
        try{
            final File file = File.createTempFile("mgnl", null, Path.getTempDirectory());
            final FileOutputStream outStream = new FileOutputStream(file);
            session.exportSystemView(src.getHandle(), outStream, false, false);
            outStream.flush();
            IOUtils.closeQuietly(outStream);
            FileInputStream inStream = new FileInputStream(file);
            session.importXML(
                destParentPath,
                inStream,
                ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
            IOUtils.closeQuietly(inStream);
            file.delete();
            if(!StringUtils.equals(src.getName(), destNodeName)){
                String currentPath;
                if(destParentPath.equals("/")){
                    currentPath = "/" + src.getName();
                }
                else{
                    currentPath = destParentPath + "/" + src.getName();
                }
                session.move(currentPath, dest);
            }
        }
        catch (IOException e) {
            throw new RepositoryException("Can't copy node " + src + " to " + dest, e);
        }
    }

    /**
     * Magnolia uses by default workspace move operation to move nodes. This is a util method to move a node inside a session.
     */
    public static void moveInSession(Content src, String dest) throws RepositoryException {
        src.getWorkspace().getSession().move(src.getHandle(), dest);
    }

}
