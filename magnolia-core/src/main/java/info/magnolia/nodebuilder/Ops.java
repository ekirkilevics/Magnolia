/**
 * This file Copyright (c) 2009-2010 Magnolia International
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
package info.magnolia.nodebuilder;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.NodeTypeFilter;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * Factory methods for most common NodeOperation implementations.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class Ops {
    public static NodeOperation addNode(final String name) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                return context.createContent(name);
            }
        };
    }

    public static NodeOperation addNode(final String name, final String type) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                return context.createContent(name, type);
            }
        };
    }

    public static NodeOperation addNode(final String name, final ItemType type) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                return context.createContent(name, type);
            }
        };
    }

    public static NodeOperation getNode(final String name) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                return context.getContent(name);
            }
        };
    }

    /**
     * Can remove either a node or property.
     */
    public static NodeOperation remove(final String name) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                context.delete(name);
                return context;
            }
        };
    }

    /**
     * Adds a currently non-existing property. Throws an ItemExistsException if the property already exists.
     */
    public static NodeOperation addProperty(final String name, final Object value) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                if (context.hasNodeData(name)) {
                    // throw new ItemExistsException("Property " + name + " already exists at " + context.getHandle());
                    throw new ItemExistsException(name);
                }
                context.createNodeData(name, value);
                return context;
            }
        };
    }

    /**
     * Sets the value of an existing property, ignoring its current value.
     * @throws ItemNotFoundException if the property does not exist.
     */
    public static NodeOperation setProperty(final String name, final Object newValue) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                if (!context.hasNodeData(name)) {
                    throw new ItemNotFoundException(name);
                }
                final Value value = NodeDataUtil.createValue(newValue, context.getJCRNode().getSession().getValueFactory());
                context.setNodeData(name, value);
                return context;
            }
        };
    }

    /**
     * Sets the value of an existing property, only if the actual current value matches the given expected current value.
     * @throws ItemNotFoundException if the property does not exist.
     * @throws RepositoryException if the current value does not match the expected one.
     */
    public static NodeOperation setProperty(final String name, final Object expectedCurrentValue, final Object newValue) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                if (!context.hasNodeData(name)) {
                    throw new ItemNotFoundException(name);
                }
                final NodeData current = context.getNodeData(name);
                if (!expectedCurrentValue.equals(NodeDataUtil.getValueObject(current))) {
                    errorHandler.report("Expected " + expectedCurrentValue + " at " + current.getHandle() + " but found " + current.getString() + " instead; can't set value to " + newValue + ".");
                    return context;
                }

                final Value value = NodeDataUtil.createValue(newValue, context.getJCRNode().getSession().getValueFactory());
                current.setValue(value);
                return context;
            }
        };
    }

    /**
     * Renames the node defined by the name parameter.
     */
    public static NodeOperation renameNode(final String name, final String newName) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                ContentUtil.rename(context.getContent(name), newName);
                return context;
            }
        };
    }

    /**
     * Renames a property by creating a new one and copying the value.
     */
    public static NodeOperation renameProperty(final String name, final String newName) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                if (!context.hasNodeData(name)) {
                    throw new ItemNotFoundException(name);
                }
                if (context.hasNodeData(newName)) {
                    //throw new ItemExistsException("Property " + newName + " already exists at " + context.getHandle());
                    throw new ItemExistsException(newName);
                }
                final Value value = context.getNodeData(name).getValue();
                context.setNodeData(newName, value);
                context.deleteNodeData(name);
                return context;
            }
        };
    }

    /**
     * Moves the node defined by the name parameter in the session.
     */
    public static NodeOperation moveNode(final String name, final String dest) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                ContentUtil.moveInSession(context.getContent(name), dest);
                return context;
            }
        };
    }

    /**
     * Copies the node defined by the name parameter in the session.
     */
    public static NodeOperation copyNode(final String name, final String dest) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                ContentUtil.copyInSession(context.getContent(name), dest);
                return context;
            }
        };
    }

    /**
     * Executes the operation for each child node excluding meta data and jcr base node.
     */
    public static NodeOperation onChildNodes(final NodeOperation... childrenOps) {
        return onChildNodes(ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER, childrenOps);
    }

    /**
     * Executes the operation for each child node of a certain type.
     */
    public static NodeOperation onChildNodes(final String type, final NodeOperation... childrenOps) {
        return onChildNodes(new NodeTypeFilter(type), childrenOps);
    }

    /**
     * Executes the operation for each child node of a certain type.
     */
    public static NodeOperation onChildNodes(final ItemType type, final NodeOperation... childrenOps) {
        return onChildNodes(new NodeTypeFilter(type), childrenOps);
    }

    /**
     * Executes the operation for each child node matching the filter.
     */
    public static NodeOperation onChildNodes(final Content.ContentFilter filter, final NodeOperation... childrenOps) {
        return new AbstractOp() {
            // TODO shouldn't this implement NodeOperation directly instead? it has no business doing with the then() method anyway
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                for (Content subNode : context.getChildren(filter)) {
                    for (NodeOperation nodeOperation : childrenOps) {
                        nodeOperation.exec(subNode, errorHandler);
                    }
                }
                return context;
            }
        };
    }

    /**
     * No operation; can be useful in ternary expression, for instance.
     */
    public static NodeOperation noop() {
        return new NodeOperation() {
            public NodeOperation then(NodeOperation... childrenOps) {
                return null;
            }

            public void exec(Content context, ErrorHandler errorHandler) {
            }
        };
    }

    /**
     * Abstract implementation of NodeOperation.
     * TODO: extract and make public ?
     */
    abstract static class AbstractOp implements NodeOperation {
        private NodeOperation[] childrenOps = {};

        public void exec(Content context, ErrorHandler errorHandler) {
            try {
                context = doExec(context, errorHandler);
            } catch (RepositoryException e) {
                errorHandler.handle(e, context);
            }

            for (NodeOperation childrenOp : childrenOps) {
                childrenOp.exec(context, errorHandler);
            }

        }

        /**
         * @return the node that should now be used as the context for subsequent operations
         */
        abstract Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException;

        public NodeOperation then(NodeOperation... childrenOps) {
            this.childrenOps = childrenOps;
            return this;
        }
    }
}
