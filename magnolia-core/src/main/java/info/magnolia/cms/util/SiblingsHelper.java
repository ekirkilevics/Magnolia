/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Content.ContentFilter;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A utility class to navigate amongst the siblings of a given node.
 * This is not synchronized.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SiblingsHelper {
    /**
     * Instantiates a SiblingsHelper representing the siblings of the given node and of the same type.
     */
    public static SiblingsHelper of(Content node) throws RepositoryException {
        return new SiblingsHelper(node, filterForTypeOf(node));
    }

    /**
     * Instantiates a SiblingsHelper representing the children of the given node,
     * where the current node is the first child of the same type as its parent.
     */
    public static SiblingsHelper childrenOf(Content parent) throws RepositoryException {
        return childrenOf(parent, parent.getItemType());
    }

    /**
     * Instantiates a SiblingsHelper representing the children of the given node,
     * where the current node is the first child of given type.
     */
    public static SiblingsHelper childrenOf(Content parent, ItemType childType) throws RepositoryException {
        final NodeTypeFilter filter = new NodeTypeFilter(childType);
        return childrenOf(parent, filter);
    }

    /**
     * Instantiates a SiblingsHelper representing the children of the given node,
     * where the current node is the first child from all that pass the filter.
     */
    public static SiblingsHelper childrenOf(Content parent, ContentFilter filter) throws RepositoryException {
        final Collection<Content> children = parent.getChildren(filter);
        if (children.size() < 1) {
            throw new IllegalStateException(parent + " has no children for " + filter);
        }
        final Content firstChild = children.iterator().next();
        return new SiblingsHelper(firstChild, filter);
    }

    private static NodeTypeFilter filterForTypeOf(Content node) throws RepositoryException {
        final ItemType type = node.getItemType();
        return new NodeTypeFilter(type);
    }

    private final List<Content> siblings;
    private final int lastIndex;
    private Content current = null;
    private int currentIndex = -1;

    private SiblingsHelper(Content node, Content.ContentFilter filter) throws RepositoryException {
        this.siblings = new ArrayList<Content>(node.getParent().getChildren(filter));
        this.lastIndex = siblings.size() - 1;
        this.current = node;
        // can't use indexOf to determine current index, as getParent().getChildren() returns a different instance of the node
        for (int i = 0; i <= lastIndex; i++) {
            final Content c = siblings.get(i);
            if (c.getUUID().equals(current.getUUID())) {
                this.currentIndex = i;
                break;
            }
        }
        if (currentIndex < 0) {
            throw new IllegalStateException("Given node not found in its own siblings.");
        }
    }

    // next() and prev() are not called getNext() and getPrevious() because they change the state.
    public Content next() {
        // TODO : check if iterator hasNext();
        this.currentIndex = currentIndex + 1;
        this.current = siblings.get(currentIndex);
        return current;
    }

    public Content prev() {
        // TODO : check if hasPrevious();
        this.currentIndex = currentIndex - 1;
        this.current = siblings.get(currentIndex);
        return current;
    }

    public Content goTofirst() {
        this.currentIndex = 0;
        this.current = siblings.get(currentIndex);
        return current;
    }

    public Content goTolast() {
        this.currentIndex = siblings.size() - 1;
        this.current = siblings.get(currentIndex);
        return current;
    }

    public Content getCurrent() {
        return current;
    }

    /**
     * Returns the zero-based index of the current node.
     */
    public int getIndex() {
        return currentIndex;
    }

    public boolean isFirst() {
        return currentIndex == 0;
    }

    public boolean isLast() {
        return currentIndex == lastIndex;
    }

}
