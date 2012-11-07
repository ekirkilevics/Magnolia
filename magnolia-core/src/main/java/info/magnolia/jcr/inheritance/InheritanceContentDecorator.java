/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.jcr.inheritance;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.decoration.AbstractContentDecorator;
import info.magnolia.jcr.decoration.ContentDecoratorNodeWrapper;
import info.magnolia.jcr.iterator.ChainedNodeIterator;
import info.magnolia.jcr.iterator.FilteringNodeIterator;
import info.magnolia.jcr.iterator.RangeIteratorImpl;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.util.NodeUtil;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Provides inheritance on JCR level by applying wrapper objects. The inheritance destination node gets nodes and
 * properties from a list of source nodes. Subclasses can customize which nodes are inherited and how the inherited
 * nodes are ordered.
 *
 * <h3>Limitations:</h3>
 * <ul><li>The inherited nodes are returned while querying for them on the destination node. The will not be visible when
 * queried for on other nodes or the session.</li>
 * <li>The inherited nodes will *not* have their path or depth adjusted to match the destination node</li>
 * <li>When inheritance results in multiple nodes that have the same name the index on nodes are not adjusted</li>
 * </ul>
 *
 * @version $Id$
 */
public class InheritanceContentDecorator extends AbstractContentDecorator {

    // The node to which we inherit
    private final Node destination;

    // The nodes we inherit from in bottom-up order
    private List<Node> sources = new ArrayList<Node>();

    private AbstractPredicate<Node> childInheritancePredicate = new AbstractPredicate<Node>() {
        @Override
        public boolean evaluateTyped(Node node) {
            try {
                return isSourceChildInherited(node);
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }
    };

    public InheritanceContentDecorator(Node destination) throws RepositoryException {
        this.destination = destination;
    }

    @Override
    public Node wrapNode(Node node) {
        try {
            if (NodeUtil.isSame(destination, node)) {
                return new DestinationNodeInheritanceNodeWrapper(node);
            } else {
                return new OtherNodeInheritanceNodeWrapper(node);
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public Node getDestination() {
        return destination;
    }

    /**
     * Returns a predicate that delegates to {@link #isSourceChildInherited(Node)}.
     */
    private AbstractPredicate<Node> getChildInheritancePredicate() {

        if (childInheritancePredicate != null) {
            return childInheritancePredicate;
        }

        childInheritancePredicate = new AbstractPredicate<Node>() {
            @Override
            public boolean evaluateTyped(Node node) {
                try {
                    return isSourceChildInherited(node);
                } catch (RepositoryException e) {
                    throw new RuntimeRepositoryException(e);
                }
            }
        };

        return childInheritancePredicate;
    }

    public void addSource(Node source) {
        this.sources.add(source);
    }

    /**
     * Decides if a node inherits child nodes. By default always returns true.
     *
     * @param node the destination node or a source node
     * @return true if the node inherits nodes
     * @throws RepositoryException
     */
    protected boolean inheritsNodes(Node node) throws RepositoryException {
        return true;
    }

    /**
     * Decides if a node inherits properties. By default always returns true.
     *
     * @param node the destination node or a source node
     * @return true if the node inherits properties
     * @throws RepositoryException
     */
    protected boolean inheritsProperties(Node node) throws RepositoryException {
        return true;
    }

    /**
     * Decides if a specific child node of one of the source should be inherited. By default always returns true.
     *
     * @param node a child of one of the source nodes
     * @return true if the node is inherited
     * @throws RepositoryException
     */
    protected boolean isSourceChildInherited(Node node) throws RepositoryException {
        return true;
    }

    /**
     * Sorts the inherited nodes and provides a {@link NodeIterator} representing that order. By default orders nodes
     * from the top-most source first and nodes from the destination last.
     *
     * @param destinationChildren children of the destination node
     * @param sourceChildren children of each of the source nodes in bottom-up order
     * @return
     * @throws javax.jcr.RepositoryException
     */
    protected NodeIterator sortInheritedNodes(NodeIterator destinationChildren, List<NodeIterator> sourceChildren) throws RepositoryException {
        Collections.reverse(sourceChildren);
        sourceChildren.add(destinationChildren);
        return new FilteringNodeIterator(new ChainedNodeIterator(sourceChildren), getChildInheritancePredicate());
    }

    /**
     * Combines the inherited properties and provides them as a {@link PropertyIterator}. By default properties in the
     * destination node overrides properties from source nodes. Properties on source node have preference in a bottom to
     * top order.
     *
     * @param destinationProperties properties of the destination node
     * @param sourceProperties properties of the source nodes in bottom-up order
     * @return
     * @throws RepositoryException
     */
    protected PropertyIterator combinePropertyIterators(PropertyIterator destinationProperties, List<PropertyIterator> sourceProperties) throws RepositoryException {
        HashSet<String> names = new HashSet<String>();
        ArrayList<Property> properties = new ArrayList<Property>();
        while (destinationProperties.hasNext()) {
            Property property = destinationProperties.nextProperty();
            names.add(property.getName());
            properties.add(property);
        }
        for (PropertyIterator propertyIterator : sourceProperties) {
            while (propertyIterator.hasNext()) {
                Property property = (Property) propertyIterator.next();
                if (!names.contains(property.getName())) {
                    names.add(property.getName());
                    properties.add(property);
                }
            }
        }
        return new PropertyIteratorImpl(properties);
    }

    /**
     * Wrapper applied to nodes other than the destination node.
     * 
     * @version $Id$
     */
    private class OtherNodeInheritanceNodeWrapper extends ContentDecoratorNodeWrapper implements InheritanceNodeWrapper {

        public OtherNodeInheritanceNodeWrapper(Node node) {
            super(node, InheritanceContentDecorator.this);
        }

        @Override
        public boolean isInherited() {
            try {

                Node wrappedNode = getWrappedNode();

                if (isChildOf(wrappedNode, destination)) {
                    return false;
                }

                Node n = destination;
                Iterator<Node> iterator = sources.iterator();
                while (iterator.hasNext() && inheritsNodes(n)) {
                    n = iterator.next();
                    if (isChildOf(wrappedNode, n) && isSourceChildInherited(wrappedNode)) {
                        return true;
                    }
                }

                return false;
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }

        private boolean isChildOf(Node child, Node parent) {
            try {
                return parent.getDepth() == 0 || child.getPath().startsWith(parent.getPath() + "/");
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }
    }

    /**
     * Wrapper applied to the destination node. Uses
     *
     * @version $Id$
     */
    public class DestinationNodeInheritanceNodeWrapper extends ContentDecoratorNodeWrapper implements InheritanceNodeWrapper {

        public DestinationNodeInheritanceNodeWrapper(Node node) {
            super(node, InheritanceContentDecorator.this);
        }

        @Override
        public boolean hasNode(String relPath) throws RepositoryException {
            if (super.hasNode(relPath)) {
                return true;
            }
            Node current = getWrappedNode();
            Iterator<Node> iterator = sources.iterator();
            while (iterator.hasNext() && inheritsNodes(current)) {
                current = iterator.next();
                if (current.hasNode(relPath) && isSourceChildInherited(current.getNode(relPath))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
            if (super.hasNode(relPath)) {
                return super.getNode(relPath);
            }
            Node current = getWrappedNode();
            Iterator<Node> iterator = sources.iterator();
            while (iterator.hasNext() && inheritsNodes(current)) {
                current = iterator.next();
                if (current.hasNode(relPath)) {
                    Node node = current.getNode(relPath);
                    if (isSourceChildInherited(node)) {
                        return wrapNode(node);
                    }
                }
            }
            throw new PathNotFoundException(relPath);
        }

        @Override
        public NodeIterator getNodes() throws RepositoryException {
            List<NodeIterator> nodes = new ArrayList<NodeIterator>();
            Node current = getWrappedNode();
            Iterator<Node> iterator = sources.iterator();
            while (iterator.hasNext() && inheritsNodes(current)) {
                current = iterator.next();
                nodes.add(current.getNodes());
            }
            return super.wrapNodeIterator(sortInheritedNodes(getWrappedNode().getNodes(), nodes));
        }

        @Override
        public NodeIterator getNodes(String namePattern) throws RepositoryException {
            List<NodeIterator> nodes = new ArrayList<NodeIterator>();
            Node current = getWrappedNode();
            Iterator<Node> iterator = sources.iterator();
            while (iterator.hasNext() && inheritsNodes(current)) {
                current = iterator.next();
                nodes.add(current.getNodes(namePattern));
            }
            return super.wrapNodeIterator(sortInheritedNodes(getWrappedNode().getNodes(namePattern), nodes));
        }

        @Override
        public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
            List<NodeIterator> nodes = new ArrayList<NodeIterator>();
            Node current = getWrappedNode();
            Iterator<Node> iterator = sources.iterator();
            while (iterator.hasNext() && inheritsNodes(current)) {
                current = iterator.next();
                nodes.add(current.getNodes(nameGlobs));
            }
            return super.wrapNodeIterator(sortInheritedNodes(getWrappedNode().getNodes(nameGlobs), nodes));
        }

        @Override
        public boolean hasProperty(String relPath) throws RepositoryException {
            if (super.hasProperty(relPath)) {
                return true;
            }
            Node current = getWrappedNode();
            Iterator<Node> iterator = sources.iterator();
            while (iterator.hasNext() && inheritsProperties(current)) {
                current = iterator.next();
                if (current.hasProperty(relPath)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {
            if (super.hasProperty(relPath)) {
                return super.getProperty(relPath);
            }
            Node current = getWrappedNode();
            Iterator<Node> iterator = sources.iterator();
            while (iterator.hasNext() && inheritsProperties(current)) {
                current = iterator.next();
                if (current.hasProperty(relPath)) {
                    return wrapProperty(current.getProperty(relPath));
                }
            }
            throw new PathNotFoundException(relPath);
        }

        @Override
        public PropertyIterator getProperties() throws RepositoryException {
            ArrayList<PropertyIterator> properties = new ArrayList<PropertyIterator>();
            Node current = getWrappedNode();
            Iterator<Node> iterator = sources.iterator();
            while (iterator.hasNext() && inheritsProperties(current)) {
                current = iterator.next();
                properties.add(current.getProperties());
            }
            return super.wrapPropertyIterator(combinePropertyIterators(super.getProperties(), properties));
        }

        @Override
        public PropertyIterator getProperties(String namePattern) throws RepositoryException {
            ArrayList<PropertyIterator> properties = new ArrayList<PropertyIterator>();
            Node current = getWrappedNode();
            Iterator<Node> iterator = sources.iterator();
            while (iterator.hasNext() && inheritsProperties(current)) {
                current = iterator.next();
                properties.add(current.getProperties(namePattern));
            }
            return super.wrapPropertyIterator(combinePropertyIterators(super.getProperties(namePattern), properties));
        }

        @Override
        public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
            ArrayList<PropertyIterator> properties = new ArrayList<PropertyIterator>();
            Node current = getWrappedNode();
            Iterator<Node> iterator = sources.iterator();
            while (iterator.hasNext() && inheritsProperties(current)) {
                current = iterator.next();
                properties.add(current.getProperties(nameGlobs));
            }
            return super.wrapPropertyIterator(combinePropertyIterators(super.getProperties(nameGlobs), properties));
        }

        @Override
        public boolean isInherited() {
            return false;
        }
    }

    private static class PropertyIteratorImpl extends RangeIteratorImpl implements PropertyIterator {

        public PropertyIteratorImpl(Collection<Property> collection) {
            super(collection);
        }

        @Override
        public Property nextProperty() {
            return (Property) super.next();
        }
    }
}
