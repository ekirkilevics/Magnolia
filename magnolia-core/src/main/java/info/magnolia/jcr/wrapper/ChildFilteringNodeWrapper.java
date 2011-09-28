/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.jcr.wrapper;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import info.magnolia.jcr.iterator.FilteringNodeIterator;
import info.magnolia.jcr.predicate.Predicate;

/**
 * NodeWrapper that hides children based on a predicate.
 *
 * @version $Id$
 */
public class ChildFilteringNodeWrapper extends ChildWrappingNodeWrapper {

    private Predicate<Node> predicate;

    public ChildFilteringNodeWrapper(Node wrapped, Predicate<Node> predicate) {
        super(wrapped);
        this.predicate = predicate;
    }

    @Override
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        Node node = super.getNode(relPath);
        if (!predicate.evaluate(node)) {
            throw new PathNotFoundException("Path not found [" + relPath + "]");
        }
        return node;
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return new FilteringNodeIterator(super.getNodes(), predicate);
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return new FilteringNodeIterator(super.getNodes(namePattern), predicate);
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return new FilteringNodeIterator(super.getNodes(nameGlobs), predicate);
    }

    @Override
    public boolean hasNode(String relPath) throws RepositoryException {
        if (!super.hasNode(relPath)) {
            return false;
        }
        Node node = super.getNode(relPath);
        return predicate.evaluate(node);
    }

    @Override
    public boolean hasNodes() throws RepositoryException {
        return getNodes().hasNext();
    }

    @Override
    public Node wrap(Node node) {
        return new ChildFilteringNodeWrapper(node, predicate);
    }
}
