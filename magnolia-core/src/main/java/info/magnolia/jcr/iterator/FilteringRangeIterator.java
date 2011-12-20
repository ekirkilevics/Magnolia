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
package info.magnolia.jcr.iterator;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import javax.jcr.RangeIterator;


/**
 * Base class for implementing filtering JCR iterators. Does not support the remove method because doing so would make
 * it impossible to implement the getSize method. Should not be a problem since Jackrabbit anyway does not support the
 * remove method on its iterators.
 *
 * @param <T>
 * @version $Id$
 */
public abstract class FilteringRangeIterator<T> implements RangeIterator {

    private final Iterator<T> iterator;
    private long position;
    private long size = 0;
    private final Queue<T> queue = new ArrayDeque<T>();

    public FilteringRangeIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        while (queue.isEmpty() && iterator.hasNext()) {
            queueNext();
        }
        return !queue.isEmpty();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        T t = queue.poll();
        position++;
        return t;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public void skip(long skipNum) {
        while (skipNum-- > 0)
            next();
    }

    @Override
    public long getSize() {
        while (iterator.hasNext()) {
            queueNext();
        }
        return size;
    }

    private void queueNext() {
        T n = iterator.next();
        if (evaluate(n)) {
            queue.add(n);
            size++;
        }
    }

    protected abstract boolean evaluate(T t);
}
