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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultContent;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some easy to use methods to handle with Content objects.
 */
public class ContentUtil {
    private final static Logger log = LoggerFactory.getLogger(ContentUtil.class);

    /**
     * Content filter accepting everything.
     */
    public static ContentFilter ALL_NODES_CONTENT_FILTER = new ContentFilter() {

        @Override
        public boolean accept(Content content) {
            return true;
        }
    };

    /**
     * Content filter accepting everything exept nodes with namespace jcr (version and system store).
     */
    public static ContentFilter ALL_NODES_EXCEPT_JCR_CONTENT_FILTER = new ContentFilter() {

        @Override
        public boolean accept(Content content) {
            return !content.getName().startsWith("jcr:");
        }
    };

    /**
     * Content filter accepting everything except meta data and jcr types.
     */
    public static ContentFilter EXCLUDE_META_DATA_CONTENT_FILTER = new ContentFilter() {
        @Override
        public boolean accept(Content content) {
            return !content.getName().startsWith("jcr:") && !content.isNodeType(ItemType.NT_METADATA);
        }
    };

    /**
     * Content filter accepting all nodes with a nodetype of namespace mgnl.
     */
    public static ContentFilter MAGNOLIA_FILTER = new ContentFilter() {
        @Override
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
     * Used in {@link #visit(Content)} to visit the hierarchy.
     * @version $Id$
     */
    // TODO : throws RepositoryException or none, but not Exception !?
    public static interface Visitor {
        void visit(Content node) throws Exception;
    }

    /**
     * Used in {@link #visit(Content)} if the visitor wants to use post order.
     */
    public static interface PostVisitor extends Visitor {
        void postVisit(Content node) throws Exception;
    }


    /**
     * Returns a Content object of the named repository or null if not existing.
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
     * Get the node or null if not exists.
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
        for (Content child : node.getChildren(ALL_NODES_CONTENT_FILTER)) {
            if (child.getName().toLowerCase().equals(name)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Get all children recursively (content and contentnode).
     */
    public static List<Content> collectAllChildren(Content node) {
        List<Content> nodes = new ArrayList<Content>();
        return collectAllChildren(nodes, node, new ItemType[]{ItemType.CONTENT, ItemType.CONTENTNODE});
    }

    /**
     * Get all children using a filter.
     * @param node
     * @param filter
     * @return list of all found nodes
     */
    public static List<Content> collectAllChildren(Content node, ContentFilter filter) {
        List<Content> nodes = new ArrayList<Content>();
        return collectAllChildren(nodes, node, filter);
    }

    /**
     * Get the children using a filter.
     * @param nodes collection of already found nodes
     */
    private static List<Content> collectAllChildren(List<Content> nodes, Content node, ContentFilter filter) {
        // get filtered sub nodes first
        Collection<Content> children = node.getChildren(filter);
        for (Content child : children) {
            nodes.add(child);
        }

        // get all children to find recursively
        Collection<Content> allChildren = node.getChildren(EXCLUDE_META_DATA_CONTENT_FILTER);

        // recursion
        for (Content child : allChildren) {
            collectAllChildren(nodes, child, filter);
        }

        return nodes;
    }

    /**
     * Get all children of a particular type.
     */
    public static List<Content> collectAllChildren(Content node, ItemType type) {
        List<Content> nodes = new ArrayList<Content>();
        return collectAllChildren(nodes, node, new ItemType[]{type});
    }

    /**
     * Returns all children (not recursively) independent of there type.
     */
    public static Collection<Content> getAllChildren(Content node){
        return node.getChildren(EXCLUDE_META_DATA_CONTENT_FILTER);
    }

    /**
     * Returns all children (not recursively) independent of there type.
     */
    public static Collection<Content> getAllChildren(Content node, Comparator<Content> comp){
        return node.getChildren(EXCLUDE_META_DATA_CONTENT_FILTER, comp);
    }

    /**
     * Get all children of a particular type.
     */
    public static List<Content> collectAllChildren(Content node, ItemType[] types) {
        List<Content> nodes = new ArrayList<Content>();
        return collectAllChildren(nodes, node, types);
    }

    /**
     * Get all subnodes recursively and add them to the nodes collection.
     */
    private static List<Content> collectAllChildren(List<Content> nodes, Content node, ItemType[] types) {
        for (int i = 0; i < types.length; i++) {
            ItemType type = types[i];

            Collection<Content> children = node.getChildren(type);
            for (Content child : children) {
                nodes.add(child);
                collectAllChildren(nodes, child, types);
            }
        }
        return nodes;
    }

    /**
     * Convenient method to order a node before a target node.
     */
    public static void orderBefore(Content nodeToMove, String targetNodeName) throws RepositoryException{
        nodeToMove.getParent().orderBefore(nodeToMove.getName(), targetNodeName);
    }

    /**
     * Convenient method for ordering a node after a specific target node. This is not that simple as jcr only supports ordering before a node.
     */
    public static void orderAfter(Content nodeToMove, String targetNodeName) throws RepositoryException{
        Content parent = nodeToMove.getParent();
        Boolean readyToMove = false;

        Collection<Content> children = new ArrayList<Content>(ContentUtil.getAllChildren(parent));
        for (Content child : children) {
            if(readyToMove){
                parent.orderBefore(nodeToMove.getName(), child.getName());
                readyToMove = false;
                break;
            }

            if(child.getName().equals(targetNodeName)){
                readyToMove = true;
            }
        }

        if(readyToMove){
            for (Content child : children){
                if(!nodeToMove.getName().equals(child.getName())){
                    parent.orderBefore(child.getName(), nodeToMove.getName());
                }
            }
        }
    }

    public static void orderNodes(Content node, String[] nodes) throws RepositoryException{
        for (int i = nodes.length - 1; i > 0; i--) {
            node.orderBefore(nodes[i-1], nodes[i]);
        }
    }

    /**
     * Uses the passed comparator to create the jcr ordering of the children.
     */
    public static void orderNodes(Content node, Comparator<Content> comparator) throws RepositoryException {
        Collection<Content> children = ContentUtil.getAllChildren(node, comparator);
        String[] names = new String[children.size()];

        int i = 0;
        for (Content childNode : children) {
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
        for (Content content : node.getChildren(filter)) {
            visit(content, visitor, filter);
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

    public static void rename(Content node, String newName) throws RepositoryException{
        Content parent = node.getParent();
        String placedBefore = null;
        for (Iterator<Content> iter = parent.getChildren(node.getNodeTypeName()).iterator(); iter.hasNext();) {
            Content child = iter.next();
            if (child.getUUID().equals(node.getUUID())) {
                if (iter.hasNext()) {
                    child = iter.next();
                    placedBefore = child.getName();
                }
            }
        }

        moveInSession(node, PathUtil.createPath(node.getParent().getHandle(), newName));

        // now set at the same place as before
        if (placedBefore != null) {
            parent.orderBefore(newName, placedBefore);
            parent.save();
        }
    }

    /**
     * Utility method to change the <code>jcr:primaryType</code> value of a node.
     * @param node - {@link Content} the node whose type has to be changed
     * @param newType - {@link ItemType} the new node type to be assigned
     * @param replaceAll - boolean when <code>true</code> replaces all occurrences
     * of the old node type. When <code>false</code> replaces only the first occurrence.
     * @throws RepositoryException
     */
    public static void changeNodeType(Content node, ItemType newType, boolean replaceAll) throws RepositoryException{
        if(node == null){
            throw new IllegalArgumentException("Content can't be null");
        }
        if(newType == null){
            throw new IllegalArgumentException("ItemType can't be null");
        }
        final String oldTypeName = node.getNodeTypeName();
        final String newTypeName = newType.getSystemName();
        if(newTypeName.equals(oldTypeName)){
            log.info("Old node type and new one are the same {}. Nothing to change.", newTypeName);
            return;
        }
        final Pattern nodeTypePattern = Pattern.compile("(<sv:property\\s+sv:name=\"jcr:primaryType\"\\s+sv:type=\"Name\"><sv:value>)("+oldTypeName+")(</sv:value>)");
        final String replacement = "$1"+newTypeName+"$3";

        log.debug("pattern is {}", nodeTypePattern.pattern());
        log.debug("replacement string is {}", replacement);
        log.debug("replaceAll? {}", replaceAll);

        final String destParentPath = StringUtils.defaultIfEmpty(StringUtils.substringBeforeLast(node.getHandle(), "/"), "/");
        final Session session = node.getWorkspace().getSession();
        FileOutputStream outStream = null;
        FileInputStream inStream = null;
        File file = null;

        try {
            file = File.createTempFile("mgnl", null, Path.getTempDirectory());
            outStream = new FileOutputStream(file);
            session.exportSystemView(node.getHandle(), outStream, false, false);
            outStream.flush();
            final String fileContents = FileUtils.readFileToString(file);
            log.debug("content string is {}", fileContents);
            final Matcher matcher = nodeTypePattern.matcher(fileContents);
            String replaced = null;

            log.debug("starting find&replace...");
            long start = System.currentTimeMillis();
            if(matcher.find()) {
                log.debug("{} will be replaced", node.getHandle());
                if(replaceAll){
                    replaced = matcher.replaceAll(replacement);
                } else {
                    replaced = matcher.replaceFirst(replacement);
                }
                log.debug("replaced string is {}", replaced);
            } else {
                log.debug("{} won't be replaced", node.getHandle());
                return;
            }
            log.debug("find&replace operations took {}ms" + (System.currentTimeMillis() - start) / 1000);

            FileUtils.writeStringToFile(file, replaced);
            inStream = new FileInputStream(file);
            session.importXML(
                destParentPath,
                inStream,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RepositoryException("Can't replace node " + node.getHandle(), e);
        } finally {
            IOUtils.closeQuietly(outStream);
            IOUtils.closeQuietly(inStream);
            FileUtils.deleteQuietly(file);
        }
    }

    public static Content asContent(Node content) {
        // FIXME try to do better and make sure we use the same session and permissions
        Session session;
        try {
            session = content.getSession();
            final HierarchyManager hm = MgnlContext.getHierarchyManager(session.getWorkspace().getName());
            if(!hm.getWorkspace().getSession().equals(session)){
                throw new IllegalStateException("Won't create a Content object, because the session of the passed node and the one used by the hierarchy manager are NOT the same. This could lead to various issues.");
            }
            return hm.getContent(content.getPath());
        } catch (RepositoryException e) {
            // TODO dlipp - apply consistent ExceptionHandling
            throw new RuntimeException(e);
        }
    }

    public static Content wrapAsContent(Node node) throws RepositoryException {
        return new DefaultContent(node, null);
    }

}
