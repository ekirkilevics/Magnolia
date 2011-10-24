/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.jcr.util;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.PermissionUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.wrapper.DelegateNodeWrapper;
import info.magnolia.jcr.iterator.FilteringNodeIterator;
import info.magnolia.jcr.iterator.NodeIterableAdapter;
import info.magnolia.jcr.predicate.Predicate;
import info.magnolia.jcr.wrapper.JCRPropertiesFilteringNodeWrapper;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utility methods to collect data from JCR repository.
 *
 * @version $Id$
 */
public class NodeUtil {

    private static final Logger log = LoggerFactory.getLogger(NodeUtil.class);

    /**
     * Node filter accepting everything.
     */
    public static Predicate<Node> ALL_NODES_FILTER = new Predicate<Node>() {

        @Override
        public boolean evaluate(Node node) {
            return true;
        }
    };

    /**
     * Node filter accepting everything except nodes with namespace jcr (version and system store).
     */
    public static Predicate<Node> ALL_NODES_EXCEPT_JCR_FILTER = new Predicate<Node>() {

        @Override
        public boolean evaluate(Node node) {
            try {
                return !node.getName().startsWith(MgnlNodeType.JCR_PREFIX);
            } catch (RepositoryException e) {
                return false;
            }
        }
    };

    /**
     * Node filter accepting everything except meta data and jcr types.
     */
    public static Predicate<Node> EXCLUDE_META_DATA_FILTER = new Predicate<Node>() {

        @Override
        public boolean evaluate(Node node) {
            try {
                return !node.getName().startsWith(MgnlNodeType.JCR_PREFIX) && !NodeUtil.isNodeType(node, MgnlNodeType.NT_METADATA);
            } catch (RepositoryException e) {
                return false;
            }
        }
    };

    /**
     * Node filter accepting all nodes of a type with namespace mgnl.
     */
    public static Predicate<Node> MAGNOLIA_FILTER = new Predicate<Node>() {

        @Override
        public boolean evaluate(Node node) {
            try {
                return node.getPrimaryNodeType().getName().startsWith(MgnlNodeType.MGNL_PREFIX);
            } catch (RepositoryException e) {
                log.error("Unable to read nodetype for node {}", getNodePathIfPossible(node));
                return false;
            }
        }
    };

    /**
     * Get a Node by identifier.
     */
    public static Node getNodeByIdentifier(String workspace, String identifier) throws RepositoryException {
        if (workspace == null || identifier == null) {
            return null;
        }
        Session jcrSession = MgnlContext.getJCRSession(workspace);
        if (jcrSession == null) {
            return null;
        }
        return jcrSession.getNodeByIdentifier(identifier);
    }

    /**
     * from default content.
     */
    public static boolean hasMixin(Node node, String mixinName) throws RepositoryException {
        if (StringUtils.isBlank(mixinName)) {
            throw new IllegalArgumentException("Mixin name can't be empty.");
        }
        for (NodeType type : node.getMixinNodeTypes()) {
            if (mixinName.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * TODO dlipp: better name? Clear javadoc! Move to MetaDataUtil, do not assign method-param!
     * TODO cringele : shouldn't @param nodeType be aligned to JCR API? There it is nodeTypeName, nodeType is used for NodeType object
     */
    public static boolean isNodeType(Node node, String type) throws RepositoryException {
        node = NodeUtil.deepUnwrap(node, JCRPropertiesFilteringNodeWrapper.class);
        final String actualType = node.getProperty(MgnlNodeType.JCR_PRIMARY_TYPE).getString();
        // if the node is frozen, and we're not looking specifically for frozen nodes, then we compare with the original
        // node type
        if (MgnlNodeType.NT_FROZENNODE.equals(actualType) && !(MgnlNodeType.NT_FROZENNODE.equals(type))) {
            final Property p = node.getProperty(MgnlNodeType.JCR_FROZEN_PRIMARY_TYPE);
            final String s = p.getString();
            return s.equalsIgnoreCase(type);

            // FIXME this method does not consider mixins when the node is frozen
        }
        return node.isNodeType(type);
    }

    public static Node unwrap(Node node) throws RepositoryException {
        Node unwrappedNode = node;
        while (unwrappedNode instanceof DelegateNodeWrapper) {
            unwrappedNode = ((DelegateNodeWrapper) unwrappedNode).getWrappedNode();
        }
        return unwrappedNode;
    }

    /**
     * Removes a wrapper by type. The wrapper can be deep in a chain of wrappers in which case wrappers before it will
     * be cloned creating a new chain that leads to the same real node.
     */
    public static Node deepUnwrap(Node node, Class<? extends DelegateNodeWrapper> wrapper) throws RepositoryException {
        if (node instanceof DelegateNodeWrapper)
            return ((DelegateNodeWrapper)node).deepUnwrap(wrapper);
        return node;
    }

    /**
     * Convenience - delegate to {@link Node#orderBefore(String, String)}.
     */
    public static void orderBefore(Node node, String siblingName) throws RepositoryException {
        node.getParent().orderBefore(node.getName(), siblingName);
    }

    /**
     * Orders the node directly after a given sibling. If no sibling is specified the node is placed first.
     *
     * @param node        the node to order
     * @param siblingName the name of the sibling which the name should be after or null if the node should be first
     * @throws RepositoryException
     */
    public static void orderAfter(Node node, String siblingName) throws RepositoryException {

        if (siblingName == null) {
            orderFirst(node);
            return;
        }

        Node parent = node.getParent();
        Node sibling = parent.getNode(siblingName);
        Node siblingAfter = getSiblingAfter(sibling);

        if (siblingAfter == null) {
            orderLast(node);
            return;
        }

        // Move the node before the sibling directly after the target sibling
        parent.orderBefore(node.getName(), siblingAfter.getName());
    }

    /**
     * Orders the node first among its siblings.
     *
     * @param node the node to order
     * @throws RepositoryException
     */
    public static void orderFirst(Node node) throws RepositoryException {
        Node parent = node.getParent();
        NodeIterator siblings = parent.getNodes();
        Node firstSibling = siblings.nextNode();
        if (!firstSibling.isSame(node)) {
            parent.orderBefore(node.getName(), firstSibling.getName());
        }
    }

    /**
     * Orders the node last among its siblings.
     *
     * @param node the node to order
     * @throws RepositoryException
     */
    public static void orderLast(Node node) throws RepositoryException {
        node.getParent().orderBefore(node.getName(), null);
    }

    /**
     * Orders the node up one step among its siblings. If the node is the only sibling or the first sibling this method
     * has no effect.
     *
     * @param node the node to order
     * @throws RepositoryException
     */
    public static void orderNodeUp(Node node) throws RepositoryException {
        Node siblingBefore = getSiblingBefore(node);
        if (siblingBefore != null) {
            node.getParent().orderBefore(node.getName(), siblingBefore.getName());
        }
    }

    /**
     * Orders the node down one step among its siblings. If the node is the only sibling or the last sibling this method
     * has no effect.
     *
     * @param node the node to order
     * @throws RepositoryException
     */
    public static void orderNodeDown(Node node) throws RepositoryException {
        Node siblingAfter = getSiblingAfter(node);
        if (siblingAfter != null) {
            node.getParent().orderBefore(siblingAfter.getName(), node.getName());
        }
    }

    public static Node getSiblingBefore(Node node) throws RepositoryException {
        Node parent = node.getParent();
        NodeIterator siblings = parent.getNodes();
        Node previousSibling = null;
        while (siblings.hasNext()) {
            Node sibling = siblings.nextNode();
            if (isSame(node, sibling)) {
                return previousSibling;
            }
            previousSibling = sibling;
        }
        return null;
    }

    public static Node getSiblingAfter(Node node) throws RepositoryException {
        Node parent = node.getParent();
        NodeIterator siblings = parent.getNodes();
        while (siblings.hasNext()) {
            Node sibling = siblings.nextNode();
            if (isSame(node, sibling)) {
                break;
            }
        }
        return siblings.hasNext() ? siblings.nextNode() : null;
    }

    public static void moveNode(Node nodeToMove, Node newParent) throws RepositoryException {
        if (!isSame(newParent, nodeToMove.getParent())) {
            String newPath = combinePathAndName(newParent.getPath(), nodeToMove.getName());
            nodeToMove.getSession().move(nodeToMove.getPath(), newPath);
        }
    }

    public static void moveNodeBefore(Node nodeToMove, Node target) throws RepositoryException {
        Node targetParent = target.getParent();
        moveNode(nodeToMove, targetParent);
        targetParent.orderBefore(nodeToMove.getName(), target.getName());
    }

    public static void moveNodeAfter(Node nodeToMove, Node target) throws RepositoryException {
        Node targetParent = target.getParent();
        moveNode(nodeToMove, targetParent);
        orderAfter(nodeToMove, target.getName());
    }

    public static boolean isFirstSibling(Node node) throws RepositoryException {
        Node parent = node.getParent();
        NodeIterator nodes = parent.getNodes();
        return isSame(nodes.nextNode(), node);
    }

    public static boolean isLastSibling(Node node) throws RepositoryException {
        Node parent = node.getParent();
        NodeIterator nodes = parent.getNodes();
        Node last = null;
        while (nodes.hasNext()) {
            last = nodes.nextNode();
        }
        return isSame(last, node);
    }

    public static void renameNode(Node node, String newName) throws RepositoryException {
        Node parent = node.getParent();
        String newPath = combinePathAndName(parent.getPath(), newName);
        node.getSession().move(node.getPath(), newPath);
    }

    /**
     * @return Whether the provided node as the provided permission or not.
     * @throws RuntimeException in case of RepositoryException.
     */
    public static boolean isGranted(Node node, long permissions) {
        try {
            return PermissionUtil.isGranted(node.getSession(), node.getPath(), PermissionUtil.convertPermissions(permissions));
        } catch (RepositoryException e) {
            // TODO dlipp - apply consistent ExceptionHandling
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if both arguments represents the same node. In case the nodes are wrapped the comparison is done one
     * the actual nodes behind the wrappers.
     */
    private static boolean isSame(Node lhs, Node rhs) throws RepositoryException {
        return unwrap(lhs).isSame(unwrap(rhs));
    }

    private static String combinePathAndName(String path, String name) {
        if ("/".equals(path)) {
            return "/" + name;
        }
        return path + "/" + name;
    }

    /**
     * Creates a node under the specified parent and relative path, then returns it. Should the node already exist, the method will simply return it.
     */
    public static Node createPath(Node parent, String relPath, String primaryNodeTypeName) throws RepositoryException, PathNotFoundException, AccessDeniedException {
        return createPath(parent, relPath, primaryNodeTypeName, false);
    }

    /**
     * Creates a node under the specified parent and relative path, then returns it. Should the node already exist, the method will simply return it.
     */
    public static Node createPath(Node parent, String relPath, String primaryNodeTypeName, boolean save) throws RepositoryException, PathNotFoundException, AccessDeniedException {
        // remove leading /
        String currentPath = StringUtils.removeStart(relPath, "/");

        if (StringUtils.isEmpty(currentPath)) {
            return parent;
        }

        Node root = parent;
        String[] names = currentPath.split("/");

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (root.hasNode(name)) {
                root = root.getNode(name);
            } else {
                final Node newNode = root.addNode(name, primaryNodeTypeName);
                if (save) {
                    root.save();
                }
                root = newNode;
            }
        }
        return root;
    }

    /**
     * Visits the given node and then all of nodes beneath it except for metadata nodes and nodes of jcr type.
     */
    public static void visit(Node node, NodeVisitor visitor) throws RepositoryException {
        visit(node, visitor, EXCLUDE_META_DATA_FILTER);
    }

    public static void visit(Node node, NodeVisitor visitor, Predicate<Node> predicate) throws RepositoryException {
        visitor.visit(node);
        for (Node child : getNodes(node, predicate)) {
            visit(child, visitor, predicate);
        }
        if (visitor instanceof PostNodeVisitor) {
            ((PostNodeVisitor) visitor).postVisit(node);
        }
    }

    public static Iterable<Node> getNodes(Node parent, Predicate<Node> predicate) throws RepositoryException {
        return asIterable(new FilteringNodeIterator(parent.getNodes(), predicate));
    }

    public static Iterable<Node> getNodes(Node parent) throws RepositoryException {
        return getNodes(parent, EXCLUDE_META_DATA_FILTER);
    }

    public static Iterable<Node> getNodes(Node parent, String nodeTypeName) throws RepositoryException {
        return getNodes(parent, new NodeTypePredicate(nodeTypeName));
    }

    public static Iterable<Node> asIterable(NodeIterator iterator) {
        return new NodeIterableAdapter(iterator);
    }

    public static List<Node> asList(Iterable<Node> nodes) {
        List<Node> nodesList = new ArrayList<Node>();
        for (Node node : nodes) {
            nodesList.add(node);
        }
        return nodesList;
    }

    /**
     * This method return the node's name on success, otherwise it handles the {@link RepositoryException} by throwing
     * {@link RuntimeRepositoryException}.
     *
     * @param node Node to get the name from.
     * @return the name of the node passed.
     */
    public static String getName(Node node){
        try {
            return node.getName();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    /**
     * Used for building exception messages where we want to avoid handling another exception inside a throws clause.
     */
    public static String getNodeIdentifierIfPossible(Node node) {
        try {
            return node.getIdentifier();
        } catch (RepositoryException e) {
            return "<not available>";
        }
    }

    public static String getNodePathIfPossible(Node node) {
        try {
            return node.getPath();
        } catch (RepositoryException e) {
            return "<not available>";
        }
    }

    /**
     * Return the Path of the node.
     *
     * @return the path for the node or an empty String in case of exception
     */
    public static String getHandleIfPossible(Node node) {
        try {
            return node.getPath();
        } catch (RepositoryException e) {
            log.error("Failed to get handle: " + e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }
}
