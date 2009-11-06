/**
 * This file Copyright (c) 2009 Magnolia International
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
import info.magnolia.cms.util.NodeDataUtil;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class Ops {
    public static NodeOperation addNode(final String name) {
        return new AbstractOp() {
            Content doExec(Content context) throws RepositoryException {
                return context.createContent(name);
            }
        };

    }

    public static NodeOperation addNode(final String name, final String type) {
        return new AbstractOp() {
            Content doExec(Content context) throws RepositoryException {
                return context.createContent(name, type);
            }
        };
    }

    public static NodeOperation addNode(final String name, final ItemType type) {
        return new AbstractOp() {
            Content doExec(Content context) throws RepositoryException {
                return context.createContent(name, type);
            }
        };
    }


    public static NodeOperation getNode(final String name) {
        return new AbstractOp() {
            Content doExec(Content context) throws RepositoryException {
                return context.getContent(name);
            }
        };
    }

    /**
     * Can remove either a node or property.
     */
    public static NodeOperation remove(final String name) {
        return new AbstractOp() {
            Content doExec(Content context) throws RepositoryException {
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
            Content doExec(Content context) throws RepositoryException {
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
            Content doExec(Content context) throws RepositoryException {
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
            Content doExec(Content context) throws RepositoryException {
                if (!context.hasNodeData(name)) {
                    throw new ItemNotFoundException(name);
                }
                final NodeData current = context.getNodeData(name);
                if (!expectedCurrentValue.equals(NodeDataUtil.getValueObject(current))) {
                    throw new RepositoryException("Expected " + expectedCurrentValue + " and found " + current.getString() + " instead.");
                }
                final Value value = NodeDataUtil.createValue(newValue, context.getJCRNode().getSession().getValueFactory());
                context.setNodeData(name, value);
                return context;
            }
        };

    }

    abstract static class AbstractOp implements NodeOperation {
        private NodeOperation[] childrenOps = {};

        public void exec(Content context) throws RepositoryException {
            context = doExec(context);
            for (NodeOperation childrenOp : childrenOps) {
                childrenOp.exec(context);
            }
        }

        /**
         * @param context the node that should now be used as the context for subsequent operations
         */
        abstract Content doExec(Content context) throws RepositoryException;

        public NodeOperation then(NodeOperation... childrenOps) {
            this.childrenOps = childrenOps;
            return this;
        }
    }
}
