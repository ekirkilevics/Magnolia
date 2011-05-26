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

import info.magnolia.nodebuilder.NodeOperationException;

import javax.jcr.Node;

/**
 * Entry point for using the node builder API.
 * Also see the {@link info.magnolia.nodebuilder.task.NodeBuilderTask} and {@link info.magnolia.nodebuilder.task.ModuleNodeBuilderTask}
 * classes for usage of the node builder API in {@link info.magnolia.module.ModuleVersionHandler}s.
 *
 * @version $Id$
 */
public class NodeBuilder {
    private final ErrorHandler errorHandler;
    private final Node root;
    private final NodeOperation[] childrenOps;

    public NodeBuilder(Node root, NodeOperation... childrenOps) {
        this(new StrictErrorHandler(), root, childrenOps);
    }

    public NodeBuilder(ErrorHandler errorHandler, Node root, NodeOperation... childrenOps) {
        this.errorHandler = errorHandler;
        this.root = root;
        this.childrenOps = childrenOps;
    }

    /**
     * Execute the operations.
     *
     * @throws NodeOperationException if the given ErrorHandler decided to do so !
     */
    public void exec() throws NodeOperationException {
        for (NodeOperation childrenOp : childrenOps) {
            childrenOp.exec(root, errorHandler);
        }
    }

    // TODO some context passed around, configuration at beginning
    // (what to do with exceptions, what to do with warnings

}
