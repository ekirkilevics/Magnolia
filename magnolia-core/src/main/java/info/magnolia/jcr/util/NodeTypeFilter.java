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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * A {@link NodeFilter} filter that filters out all nodes except those of the specified node type. The node
 * type can be the primary node type or a mixin.
 *
 * @version $Id$
 */
public class NodeTypeFilter implements NodeFilter {

    private String nodeTypeName;

    public NodeTypeFilter(String nodeTypeName) {
        this.nodeTypeName = nodeTypeName;
    }

    @Override
    public boolean accept(Node node) throws RepositoryException {
        return NodeUtil.isNodeType(node, nodeTypeName);
    }

    @Override
    public String toString() {
        return "NodeTypeFilter for type " + nodeTypeName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || !(obj instanceof NodeTypeFilter)) {
            return false;
        }

        NodeTypeFilter that = (NodeTypeFilter) obj;
        return this.nodeTypeName == null ? that.nodeTypeName == null : this.nodeTypeName.equals(that.nodeTypeName);
    }

    @Override
    public int hashCode() {
        // any prime is good default
        return this.nodeTypeName == null ? 23 : this.nodeTypeName.hashCode();
    }
}
