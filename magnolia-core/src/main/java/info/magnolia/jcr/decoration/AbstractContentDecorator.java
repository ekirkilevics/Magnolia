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
package info.magnolia.jcr.decoration;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;

/**
 * Abstract implementation of {@link ContentDecorator} that filters out nothing and wraps everything to carry the
 * decoration to the entire graph.
 *
 * @version $Id$
 */
public abstract class AbstractContentDecorator implements ContentDecorator {

    @Override
    public Session wrapSession(Session session) {
        return new ContentDecoratorSessionWrapper(session, this);
    }

    @Override
    public Node wrapNode(Node node) {
        return new ContentDecoratorNodeWrapper(node, this);
    }

    @Override
    public NodeIterator wrapNodeIterator(NodeIterator nodeIterator) {
        return new ContentDecoratorNodeIterator(nodeIterator, this);
    }

    @Override
    public boolean evaluateNode(Node node) {
        return true;
    }

    @Override
    public Property wrapProperty(Property property) {
        return new ContentDecoratorPropertyWrapper(property, this);
    }

    @Override
    public PropertyIterator wrapPropertyIterator(PropertyIterator propertyIterator) {
        return new ContentDecoratorPropertyIterator(propertyIterator, this);
    }

    @Override
    public boolean evaluateProperty(Property property) {
        return true;
    }
}
