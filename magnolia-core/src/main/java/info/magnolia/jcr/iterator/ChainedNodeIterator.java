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
package info.magnolia.jcr.iterator;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.apache.commons.collections.iterators.IteratorChain;
/**
 * An implementation of {@link NodeIterator} which will chain several iterators making them behave like one.
 * @version $Id$
 *
 */
public class ChainedNodeIterator implements NodeIterator {

    private IteratorChain iterators = new IteratorChain();
    private long position = 0;
    private long size = 0;

    public ChainedNodeIterator(List<NodeIterator> iterators) {
        for(NodeIterator it : iterators) {
            this.iterators.addIterator(it);
            size += it.getSize();
        }
    }

    public ChainedNodeIterator(NodeIterator... iterators) {
        this(Arrays.asList(iterators));
    }

    @Override
    public void skip(long skipNum) {
        while (skipNum > 0) {
            iterators.next();
            skipNum--;
        }
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public boolean hasNext() {
        return iterators.hasNext();
    }

    @Override
    public Object next() {
        return iterators.next();
    }

    @Override
    public void remove() {
        iterators.remove();
    }

    @Override
    public Node nextNode() {
        Node node = (Node)iterators.next();
        position++;
        return node;
    }
}
