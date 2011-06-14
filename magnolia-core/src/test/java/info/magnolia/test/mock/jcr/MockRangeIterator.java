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
package info.magnolia.test.mock.jcr;

import java.util.Collection;
import java.util.Iterator;
import javax.jcr.RangeIterator;

/**
 * @version $Id$
 */
public class MockRangeIterator<T> implements RangeIterator {

    private Iterator<T> iterator;
    private int size;
    private int position = 0;

    public MockRangeIterator(Collection<T> collection) {
        this.iterator = collection.iterator();
        this.size = collection.size();
    }

    public MockRangeIterator(Iterator<T> iterator, int size) {
        this.iterator = iterator;
        this.size = size;
    }

    @Override
    public void skip(long skipNum) {
        while (skipNum > 0) {
            next();
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
        return iterator.hasNext();
    }

    @Override
    public Object next() {
        return nextElement();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    protected T nextElement() {
        T element = iterator.next();
        position++;
        return element;
    }
}