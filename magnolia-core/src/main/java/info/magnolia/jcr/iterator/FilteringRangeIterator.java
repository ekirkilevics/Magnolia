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

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.jcr.RangeIterator;

import org.apache.jackrabbit.commons.predicate.Predicate;


/**
 * Base class for implementing filtering JCR iterators. Does not support getSize() since that would require iterating
 * through the entire target iterator to count the number of nodes that match the predicate.
 *
 * @param <T>
 * @version $Id$
 */
public class FilteringRangeIterator<T> implements RangeIterator {

    private final Iterator<T> iterator;
    private final Predicate predicate;
    private long position;
    private T next;

    public FilteringRangeIterator(Iterator<T> iterator, Predicate predicate) {
        this.iterator = iterator;
        this.predicate = predicate;
    }

    @Override
    public boolean hasNext() {
        if (next == null) {
            seekNext();
        }
        return next != null;
    }

    @Override
    public T next() {
        if (next == null) {
            seekNext();
        }
        if (next != null) {
            T t = next;
            next = null;
            return t;
        }
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        iterator.remove();
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
        // getSize() is optional and we don't support it since that would require walking through the entire iterator
        return -1;
    }

    protected void seekNext() {
        while (iterator.hasNext()) {
            T n = iterator.next();
            if (predicate.evaluate(n)) {
                next = n;
                position++;
                return;
            }
        }
    }
}
