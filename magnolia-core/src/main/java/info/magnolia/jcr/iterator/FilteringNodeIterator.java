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
package info.magnolia.jcr.iterator;

import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.wrapper.NodeWrapperFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

/**
 * NodeIterator hiding all nodes that do not pass the predicate, returned nodes can also be wrapped effectively
 * extending the condition to the whole hierarchy.
 *
 * @version $Id$
 */
public class FilteringNodeIterator extends FilteringRangeIterator<Node> implements NodeIterator {

    private NodeWrapperFactory wrapperFactory;
    private AbstractPredicate<Node> predicate;

    public FilteringNodeIterator(NodeIterator nodeIterator, AbstractPredicate<Node> predicate) {
        super(nodeIterator);
        this.predicate = predicate;
    }

    public FilteringNodeIterator(NodeIterator nodeIterator, AbstractPredicate<Node> predicate, NodeWrapperFactory wrapperFactory) {
        super(nodeIterator);
        this.wrapperFactory = wrapperFactory;
        this.predicate = predicate;
    }

    @Override
    public Node next() {
        return wrapNode(super.next());
    }

    @Override
    public Node nextNode() {
        return wrapNode(super.next());
    }

    protected Node wrapNode(Node node) {
        return wrapperFactory != null ? wrapperFactory.wrapNode(node) : node;
    }

    @Override
    protected boolean evaluate(Node node) {
        return predicate == null || predicate.evaluate(node);
    }
}
