/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.jcr.wrapper;

import info.magnolia.jcr.iterator.RangeIteratorImpl;
import info.magnolia.jcr.util.NodeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This wrapper allows extending other nodes (mainly useful to extend configurations). A node can define
 * a property with the name 'extends'. Its value is either an absolute or relative path. The merge is then performed as follows:
 *
 * <ul>
 * <li>properties are merged and values are overwritten
 * <li>sub nodes are merged, the original order is guaranteed, new nodes are added at the end of the list
 * </ul>
 *
 * The mechanism supports multiple inheritances as such:
 * <ul>
 * <li>the node the current node inherits from can again extend a node
 * <li>nodes laying deeper in the hierarchy can extend an other node
 * </ul>
 *
 * @see {@link InheritanceNodeWrapper} a class supporting content inheritance.
 */
public class ExtendingNodeWrapper extends ChildWrappingNodeWrapper {

    protected static final String EXTENDING_NODE_PROPERTY = "extends";
    protected static final String EXTENDING_NODE_PROPERTY_OVERRIDE = "override";

    private static final Logger log = LoggerFactory.getLogger(ExtendingNodeWrapper.class);

    /**
     * Are we extending or not?
     */
    private boolean extending;

    /**
     * Node that is being extended.
     */
    private Node extendedNode;

    /**
     * Default constructor.
     * @param node
     */
    public ExtendingNodeWrapper(Node wrappedNode) {
        this(wrappedNode, false);
    }

    /**
     * Call directly for test purposes only.
     *
     * @param node
     * @param failOnError
     */
    public ExtendingNodeWrapper(Node wrappedNode, boolean failOnError) {
        super(wrappedNode);

        try {
            final boolean hasExtendingProperty = getWrappedNode().hasProperty(EXTENDING_NODE_PROPERTY);

            if (hasExtendingProperty) {
                extending = true;

                Property extendingNodeProperty = getWrappedNode().getProperty(EXTENDING_NODE_PROPERTY);

                String extendingNodePath = extendingNodeProperty.getString();

                if (StringUtils.isBlank(extendingNodePath) || extendingNodePath.equals(EXTENDING_NODE_PROPERTY_OVERRIDE)) {
                    extending = false;
                }

                if (extending) {
                    if (isExists(extendingNodePath, getWrappedNode())) {
                        // support multiple inheritance
                        if (extendingNodePath.startsWith("/")) {
                            extendedNode = getWrappedNode().getSession().getNode(extendingNodePath);
                        } else {
                            extendedNode = getWrappedNode().getNode(extendingNodePath);
                        }
                        if (!NodeUtil.isSame(getWrappedNode(), extendedNode)) {
                            extendedNode = wrapIfNeeded(extendedNode);
                        } else {
                            // nodes are the same so we will not extend.
                            extendedNode = null;
                            extending = false;
                            log.error("Node can't self-extend: " + getWrappedNode().getPath());
                        }
                    } else {
                        String message = "Can't find referenced node for value: " + wrapped;
                        log.error(message);
                        extending = false;
                        if (failOnError) {
                            throw new RuntimeException(message);
                        }
                    }
                }
            }

        } catch (RepositoryException e) {
            throw new RuntimeException("Can't wrap node [" + wrapped + "]", e);
        }
    }

    /**
     * Does not support the extends property but chains the two nodes directly. Each node is
     * wrapped internally to ensure that each of them support the extends property for themselves.
     * @param wrappedNode
     * @param extendedNode
     */
    protected ExtendingNodeWrapper(Node wrappedNode, Node extendedNode) {
        super(wrapIfNeeded(wrappedNode));
        extending = true;
        try {
            if (getWrappedNode().hasProperty(EXTENDING_NODE_PROPERTY)) {
                Property extendingNodeProperty = getWrappedNode().getProperty(EXTENDING_NODE_PROPERTY);

                // check if override is not forced
                extending = !extendingNodeProperty.getString().equals(EXTENDING_NODE_PROPERTY_OVERRIDE);
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException("Can't determine extends point for node [" + wrappedNode + "]", e);
        }
        // might extend further more
        this.extendedNode = wrapIfNeeded(extendedNode);
    }

    @Override
    public boolean hasNode(String relPath) throws RepositoryException {
        if(getWrappedNode().hasNode(relPath)) {
            return true;
        } else if(extending && extendedNode.hasNode(relPath)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        if (getWrappedNode().hasNode(relPath)) {
            return wrapNode(getWrappedNode().getNode(relPath));
        } else if (extending && extendedNode.hasNode(relPath)) {
            return(extendedNode.getNode(relPath));
        } else {
            throw new PathNotFoundException("Node does not exists: [" + relPath + "]");
        }
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return this.getNodes("*");
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return this.getNodes(StringUtils.join(nameGlobs, " | "));
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        Collection<Node> children = NodeUtil.getSortedCollectionFromNodeIterator(getWrappedNode().getNodes());
        if (extending) {

            Collection<Node> extendedNodeChildren = NodeUtil.getSortedCollectionFromNodeIterator(extendedNode.getNodes());
            Map<String, Node> merged = new LinkedHashMap<String, Node>();

            for (Node content : extendedNodeChildren) {
                merged.put(content.getName(), content);
            }
            for (Node content : children) {
                merged.put(content.getName(), content);
            }
            return new NodeIteratorImpl(wrapNodes(merged.values()));
        }
        return new NodeIteratorImpl(wrapNodes(children));
    }

    @Override
    public boolean hasProperty(String relPath) throws RepositoryException {
        // extending property should be hidden
        if (relPath.equals(EXTENDING_NODE_PROPERTY)) {
            return false;
        } else if (getWrappedNode().hasProperty(relPath)) {
            return true;
        } else if (extending && extendedNode.hasProperty(relPath)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {
        // extending property should be hidden
        if (relPath.equals(EXTENDING_NODE_PROPERTY)) {
            throw new PathNotFoundException("Cannont access property [" + getWrappedNode().getPath() + "." + relPath + "]");
        } else if (getWrappedNode().hasProperty(relPath)) {
            return getWrappedNode().getProperty(relPath);
        } else if (extending && extendedNode.hasProperty(relPath)) {
            return extendedNode.getProperty(relPath);
        } else {
            throw new RepositoryException("Can't read property from extended node [" + extendedNode + "]");
        }
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return this.getProperties("*");
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        return this.getProperties(StringUtils.join(nameGlobs, " | "));
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        Collection<Property> properties = getPropertiesAsList(getWrappedNode(), namePattern);

        if (extending) {
            Collection<Property> inheritedProperties = getPropertiesAsList(extendedNode, namePattern);

            Map<String, Property> merged = new TreeMap<String, Property>();

            for (Property prop : inheritedProperties) {
                merged.put(prop.getName(), prop);
            }

            for (Property prop : properties) {
                merged.put(prop.getName(), prop);
            }
            return new PropertyIteratorImpl(merged.values());
        }
        return new PropertyIteratorImpl(properties);
    }

    @Override
    public Node wrapNode(Node node) {
        // get the same subnode of the extended content
        try {
            if (extending && extendedNode.hasNode(node.getName())) {
                Node extendedSubNode = extendedNode.getNode(node.getName());
                return new ExtendingNodeWrapper(node, extendedSubNode);
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException("Can't wrap " + node, e);
        }
        return wrapIfNeeded(node);
    }

    @Override
    public Node getWrappedNode() {
        if (wrapped instanceof ExtendingNodeWrapper) {
            ExtendingNodeWrapper extendingNodeWrapper = (ExtendingNodeWrapper) wrapped;
            if (!extendingNodeWrapper.extending) {
                return extendingNodeWrapper.getWrappedNode();
            }
        }
        return wrapped;
    }

    @Override
    public void setWrappedNode(Node node) {
        // this wrapper can be used multiple times (multiple inheritance)
        super.wrapped = node;
    }

    /**
     * @return true if node extends another, false otherwise
     */
    public boolean isExtending() {
        return extending;
    }

    /**
     * Check if node exists.
     * @param nodePath
     * @param parent
     * @return True if exists, otherwise false.
     * @throws RepositoryException
     */
    private boolean isExists(String nodePath, Node parent) throws RepositoryException {
        if (nodePath.startsWith("/")) {
            return getWrappedNode().getSession().itemExists(nodePath);
        }
        return parent.hasNode(nodePath);
    }

    /**
     * Wraps node if needed.
     * @param node
     * @return wrapped node
     */
    private static Node wrapIfNeeded(Node node) {
        if (node instanceof ExtendingNodeWrapper) {
            return node;
        }
        return new ExtendingNodeWrapper(node);
    }

    /**
     *
     * @param values
     * @return Collection of wrapped nodes.
     */
    private Collection<Node> wrapNodes(Collection<Node> collection) {
        Collection<Node> wrappedNodes = new ArrayList<Node>();
        for (Node node : collection) {
            wrappedNodes.add(wrapNode(node));
        }
        return wrappedNodes;
    }

    /**
     * Gets all properties from node and returns them as {@link java.util.List}.
     * Also filters out "extends" property.
     * @param node
     * @param namePattern
     * @return List of node properties.
     * @throws RepositoryException
     */
    private static List<Property> getPropertiesAsList(Node node, String namePattern) throws RepositoryException {
        List<Property> properties = new ArrayList<Property>();
        PropertyIterator it = node.getProperties(namePattern);

        while(it.hasNext()) {
            Property prop = (Property) it.next();
            if (!prop.getName().equals(EXTENDING_NODE_PROPERTY)) {
                properties.add(prop);
            }
        }
        return properties;
    }

    private static class PropertyIteratorImpl extends RangeIteratorImpl<Property> implements PropertyIterator {

        public PropertyIteratorImpl(Collection<Property> properties) {
            super(properties);
        }

        @Override
        public Property nextProperty() {
            return super.next();
        }
    }

    private static class NodeIteratorImpl extends RangeIteratorImpl<Node> implements NodeIterator {

        public NodeIteratorImpl(Collection<Node> nodes) {
            super(nodes);
        }

        @Override
        public Node nextNode() {
            return super.next();
        }
    }

}
