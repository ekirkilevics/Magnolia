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

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

/**
 * @version $Id$
 */
public abstract class MockItem implements Item {

    final private String name;
    private MockNode parent;
    private MockSession session;

    public MockItem(String name) {
        this(name, null);
    }

    public MockItem(String name, MockSession session) {
        this.name = name;
        this.session = session;
    }

    @Override
    public void accept(ItemVisitor visitor) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public int getDepth() throws RepositoryException {
        if (this.getParent() == null) {
            return 0;
        }
        return getParent().getDepth() + 1;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public String getPath() throws RepositoryException {
        if (this.getParent() != null && !this.getParent().getName().equals("jcr:root")) {
            return getParent().getPath() + "/" + this.getName();
        }
        return "/" + this.getName();
    }

    @Override
    public Session getSession() {
        return session;
    }

    protected void setSession(MockSession session) {
        this.session = session;
    }

    @Override
    public boolean isModified() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean isNew() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public abstract boolean isNode();

    @Override
    public boolean isSame(Item otherItem) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void remove() throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    protected void setParent(MockNode parent) throws RepositoryException {
        this.parent = parent;
        setSession((MockSession) parent.getSession());
    }

    @Override
    public String toString() {
        return "MockItem [name=" + name + ", parent=" + (parent == null ? "null" :parent.getName()) + ", session=" + (session == null ? "null" : session.getWorkspace().getName()) + "]";
    }
}
