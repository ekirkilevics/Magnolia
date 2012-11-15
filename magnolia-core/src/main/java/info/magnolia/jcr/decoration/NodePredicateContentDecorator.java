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
package info.magnolia.jcr.decoration;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.predicate.AbstractPredicate;

/**
 * {@link ContentDecorator} that applies a predicate to all nodes in a JCR object graph. The predicate is checked
 * against all parent nodes and for properties its tested on their node and all its parents. This effectively hides the
 * entire sub-tree below a node that doesn't match the predicate.
 *
 * @version $Id$
 */
public class NodePredicateContentDecorator extends AbstractContentDecorator {

    private final AbstractPredicate<Node> nodePredicate;

    public NodePredicateContentDecorator(AbstractPredicate<Node> nodePredicate) {
        this.nodePredicate = nodePredicate;
    }

    @Override
    public boolean evaluateNode(Node node) {
        try {
            do {
                if (!nodePredicate.evaluate(node)) {
                    return false;
                }
                node = node.getParent();
            } while (node.getDepth() != 0);
            return true;
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public boolean evaluateProperty(Property property) {
        try {
            return evaluateNode(property.getParent());
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
}
