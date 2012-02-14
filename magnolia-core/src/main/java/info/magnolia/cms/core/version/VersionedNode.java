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
package info.magnolia.cms.core.version;

import info.magnolia.cms.core.ItemType;
import info.magnolia.jcr.wrapper.DelegateNodeWrapper;
import info.magnolia.jcr.wrapper.DelegatePropertyWrapper;

import java.util.Calendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

/**
 * Wrapper for version of the node exposing frozen node content as its own as used to happen in old Content API.
 *
 * @version $Id$
 */
public class VersionedNode extends DelegateNodeWrapper implements Version {


    private final Version version;
    private final Node baseNode;

    private class VersionedNodeChild extends DelegateNodeWrapper implements Node {

        private final DelegateNodeWrapper versionedParent;

        public VersionedNodeChild(VersionedNode versionedNode, Node node) {
            super(node);
            this.versionedParent = versionedNode;
        }

        public VersionedNodeChild(VersionedNodeChild versionedNode, Node node) {
            super(node);
            this.versionedParent = versionedNode;
        }

        @Override
        public int getDepth() throws RepositoryException {
            return this.versionedParent.getDepth() + 1;
        }

        @Override
        public Node getParent() throws ItemNotFoundException,
        AccessDeniedException, RepositoryException {
            return this.versionedParent;
        }

        @Override
        public String getPath() throws RepositoryException {
            return this.versionedParent.getPath() + "/" + getName();
        }

        @Override
        protected Property wrap(Property property) {
            return new DelegatePropertyWrapper(property) {
                @Override
                public String getPath() throws RepositoryException {
                    return VersionedNodeChild.this.getPath() + "/" + getName();
                }
            };
        }

        @Override
        public Node wrap(Node node) {
            return new VersionedNodeChild(VersionedNodeChild.this, node);
        }
    }

    public VersionedNode(Version versionedNode, Node baseNode) throws PathNotFoundException, RepositoryException {
        super(versionedNode.getNode(ItemType.JCR_FROZENNODE));
        this.version = versionedNode;
        this.baseNode = baseNode;
    }

    @Override
    public int getDepth() throws RepositoryException {
        return this.baseNode.getDepth();
    }

    public Version unwrap() {
        return version;
    }

    @Override
    public VersionHistory getContainingHistory() throws RepositoryException {
        return version.getContainingHistory();
    }

    @Override
    public Calendar getCreated() throws RepositoryException {
        return version.getCreated();
    }

    @Override
    public Node getFrozenNode() throws RepositoryException {
        return deepUnwrap(getClass());
    }

    @Override
    public Version getLinearPredecessor() throws RepositoryException {
        return version.getLinearPredecessor();
    }

    @Override
    public Version getLinearSuccessor() throws RepositoryException {
        return version.getLinearSuccessor();
    }

    @Override
    public Version[] getPredecessors() throws RepositoryException {
        return null;
    }

    @Override
    public Version[] getSuccessors() throws RepositoryException {
        return version.getSuccessors();
    }

    @Override
    public Node wrap(Node node) {
        return new VersionedNodeChild(this, node);
    }

    @Override
    public String getPath() throws RepositoryException {
        return baseNode.getPath();
    }

    @Override
    public Node getParent() throws ItemNotFoundException,
    AccessDeniedException, RepositoryException {
        return baseNode.getParent();
    }

    @Override
    public boolean isNodeType(String nodeTypeName) throws RepositoryException {
        return baseNode.isNodeType(nodeTypeName);
    }

    public Node getBaseNode() {
        return baseNode;
    }

    @Override
    public Session getSession() throws RepositoryException {
        return baseNode.getSession();
    }
}
