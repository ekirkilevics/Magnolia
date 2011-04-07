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
package info.magnolia.test.mock;

import info.magnolia.cms.core.Content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.jcr.Node;

/**
 * Wrapper for collections of content where collections of nodes are expected.
 * 
 * @author had
 * @version $Id: $
 */
public class Content2NodeCollectionWrapper implements Collection<Node> {

    private final Collection<Content> col;

    public Content2NodeCollectionWrapper(Collection<Content> c) {
        this.col = c;
    }

    public boolean add(Node arg0) {
        throw new UnsupportedOperationException("This collection is read only");
    }

    public boolean addAll(Collection<? extends Node> arg0) {
        throw new UnsupportedOperationException("This collection is read only");
    }

    public void clear() {
        col.clear();
    }

    public boolean contains(Object arg0) {
        for (Content c : col) {
            if (c.getJCRNode().equals(arg0)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(Collection<?> arg0) {
        if (arg0 == null) {
            return false;
        }
        Collection<Object> test = new ArrayList<Object>(arg0);
        for (Content c : col) {
            test.remove(c.getJCRNode());
        }
        return test.isEmpty();
    }

    public boolean isEmpty() {
        return col.isEmpty();
    }

    public Iterator<Node> iterator() {
        Collection<Node> test = new ArrayList<Node>();
        for (Content c : col) {
            test.add(c.getJCRNode());
        }
        return test.iterator();
    }

    public boolean remove(Object arg0) {
        for (Content c : col) {
            if (c.getJCRNode().equals(arg0)) {
                col.remove(c);
                return true;
            }
        }
        return false;
    }

    public boolean removeAll(Collection<?> nodesToRemove) {
        Collection<Content> contentToRemove = createCollectionOfContainedContent(nodesToRemove);
        return col.removeAll(contentToRemove);
    }

    private Collection<Content> createCollectionOfContainedContent(Collection<?> nodesToRemove) {
        Collection<Content> test = new ArrayList<Content>();
        for (Object o : nodesToRemove) {
            for (Content c : col) {
                if (c.getJCRNode().equals(o)) {
                    test.add(c);
                }
            }
        }
        return test;
    }

    public boolean retainAll(Collection<?> nodesToRetain) {
        Collection<Content> contentToRetain = createCollectionOfContainedContent(nodesToRetain);
        return col.retainAll(contentToRetain);
    }

    public int size() {
        return col.size();
    }

    public Object[] toArray() {
        Collection<Node> test = new ArrayList<Node>();
        for (Content c : col) {
            test.add(c.getJCRNode());
        }
        return test.toArray();
    }

    public <T> T[] toArray(T[] arg0) {
        Collection<Node> test = new ArrayList<Node>();
        for (Content c : col) {
            test.add(c.getJCRNode());
        }
        return test.toArray(arg0);
    }
}
