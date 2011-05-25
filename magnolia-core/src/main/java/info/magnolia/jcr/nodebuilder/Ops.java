/**
 * This file Copyright (c) 2009-2011 Magnolia International
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
package info.magnolia.jcr.nodebuilder;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.jcr.util.JCRUtil;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * Factory methods for most common NodeOperation implementations.
 *
 * @version $Id$
 */
public abstract class Ops {
    public static NodeOperation addNode(final String name) {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                // TODO dlipp: Caution - addNode does not create/update Metadata (in contrast to Content#createContent! To be checked!
                return context.addNode(name);
            }
        };
    }

    public static NodeOperation addNode(final String name, final String type) {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                return context.addNode(name, type);
            }
        };
    }

    /**
     * TODO dlipp: check whether we want to keep that method - or ItemType itself...
     */
    public static NodeOperation addNode(final String name, final ItemType type) {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                return context.addNode(name, type.getSystemName());
            }
        };
    }

    public static NodeOperation getNode(final String name) {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                return context.getNode(name);
            }
        };
    }

    /**
     * Adds property. Throws an ItemExistsException if the property already exists.
     */
    public static NodeOperation addProperty(final String name, final String newValue) {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                if (context.hasProperty(name)) {
                    throw new ItemExistsException("Property " + name + " already exists at " + context.getPath());
                }
                final Value value = NodeDataUtil.createValue(newValue, context.getSession().getValueFactory());
                context.setProperty(name, value);
                return context;
            }
        };
    }

    /**
     * Sets the value of an existing property, ignoring its current value.
     * @throws ItemNotFoundException if the property does not exist.
     */
    public static NodeOperation setProperty(final String name, final Object newValue) {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                if (!context.hasProperty(name)) {
                    throw new ItemNotFoundException(name);
                }
                final Value value = NodeDataUtil.createValue(newValue, context.getSession().getValueFactory());
                context.setProperty(name, value);
                return context;
            }
        };
    }

    /**
     * Renames a node.
     */
    public static NodeOperation renameNode(final String currentName, final String newName) {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                JCRUtil.renameNode(context.getNode(currentName), newName);
                return context;
            }
        };
    }

    /**
     * Renames a property by creating a new one and copying the value.
     */
    public static NodeOperation renameProperty(final String name, final String newName) {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                if (!context.hasProperty(name)) {
                    throw new ItemNotFoundException(name);
                }
                if (context.hasProperty(newName)) {
                    //throw new ItemExistsException("Property " + newName + " already exists at " + context.getHandle());
                    throw new ItemExistsException(newName);
                }
                final Value value = context.getProperty(name).getValue();
                context.setProperty(newName, value);
                context.getProperty(name).remove();
                return context;
            }
        };
    }

    /**
     * Moves a node, using session-scoped operation.
     */
    public static NodeOperation moveNode(final String nodeName, final String dest) {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                JCRUtil.moveNode(context.getNode(nodeName), context.getNode(dest));
                return context;
            }
        };
    }

    /**
     * No operation; can be useful in ternary expression, for instance.
     */
    public static NodeOperation noop() {
        return new NodeOperation() {
            @Override
            public NodeOperation then(NodeOperation... childrenOps) {
                return null;
            }

            @Override
            public void exec(Node context, ErrorHandler errorHandler) {
            }
        };
    }
}
