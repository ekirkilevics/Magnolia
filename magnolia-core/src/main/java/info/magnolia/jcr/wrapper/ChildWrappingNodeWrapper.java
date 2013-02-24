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
package info.magnolia.jcr.wrapper;

import info.magnolia.jcr.iterator.WrappingNodeIterator;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;


/**
 * Wrapper providing support for wrapping all child nodes of the wrapped node incl those returned by the NodeIterators.
 * 
 * @version $Id$
 * @deprecated since magnolia 4.5.8 use {@link info.magnolia.jcr.decoration.ContentDecoratorNodeWrapper} instead
 */
@Deprecated
public abstract class ChildWrappingNodeWrapper extends DelegateNodeWrapper implements NodeWrapperFactory {

    public ChildWrappingNodeWrapper(Node wrapped) {
        super(wrapped);
    }

    @Override
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        return wrapNode(super.getNode(relPath));
    }

    @Override
    public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        return wrapNode(super.addNode(relPath));
    }

    @Override
    public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        return wrapNode(super.addNode(relPath, primaryNodeTypeName));
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return wrapNodeIterator(super.getNodes());
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return wrapNodeIterator(super.getNodes(namePattern));
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return wrapNodeIterator(super.getNodes(nameGlobs));
    }

    @Override
    public Node wrapNode(Node node) {
        try {
            ChildWrappingNodeWrapper clone = (ChildWrappingNodeWrapper) super.clone();
            clone.wrapped = node;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Unable to clone node wrapper [" + getClass() + "]", e);
        }
    }

    protected NodeIterator wrapNodeIterator(NodeIterator nodeIterator) {
        return new WrappingNodeIterator(nodeIterator, this);
    }
}
