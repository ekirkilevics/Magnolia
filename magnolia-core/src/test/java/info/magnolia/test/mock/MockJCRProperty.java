/**
 * This file Copyright (c) 2010 Magnolia International
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

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;

/**
 * @author pbaerfuss
 * @version $Id$
 *
 */
public class MockJCRProperty implements Property {

    /**
     * 
     */
    private final MockNodeData mockNodeData;

    /**
     * @param mockNodeData
     */
    MockJCRProperty(MockNodeData mockNodeData) {
        this.mockNodeData = mockNodeData;
    }

    public void save() throws javax.jcr.AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException,
        ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        this.mockNodeData.save();
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        this.mockNodeData.delete();
    }

    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {
        this.mockNodeData.refresh(keepChanges);
    }

    public boolean isSame(Item otherItem) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isNode() {
        return false;
    }

    public boolean isNew() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isModified() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Session getSession() throws RepositoryException {
        return this.mockNodeData.getParent().getHierarchyManager().getWorkspace().getSession();
    }

    public String getPath() throws RepositoryException {
        return this.mockNodeData.getHandle();
    }

    public Node getParent() throws ItemNotFoundException, javax.jcr.AccessDeniedException, RepositoryException {
        return this.mockNodeData.getParent().getJCRNode();
    }

    public String getName() throws RepositoryException {
        return this.mockNodeData.getName();
    }

    public int getDepth() throws RepositoryException {
        return this.mockNodeData.getParent().getLevel() + 1;
    }

    public Item getAncestor(int depth) throws ItemNotFoundException, javax.jcr.AccessDeniedException, RepositoryException {
        return this.mockNodeData.getParent().getParent().getAncestor(depth -1).getJCRNode();
    }

    public void accept(ItemVisitor visitor) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setValue(Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setValue(boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
        RepositoryException {
        this.mockNodeData.setValue(value);
    }

    public void setValue(Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
        RepositoryException {
        this.mockNodeData.setValue(value);
    }

    public void setValue(double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
        RepositoryException {
        this.mockNodeData.setValue(value);
    }

    public void setValue(long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        this.mockNodeData.setValue(value);
    }

    public void setValue(InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
        RepositoryException {
        this.mockNodeData.setValue(value);
    }

    public void setValue(String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
        RepositoryException {
    }

    public void setValue(String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
        RepositoryException {
        this.mockNodeData.setValue(value);
    }

    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
        RepositoryException {
        this.mockNodeData.setValue(values);
    }

    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        this.mockNodeData.setValue(value);
    }

    public Value[] getValues() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getValues();
    }

    public Value getValue() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getValue();
    }

    public int getType() throws RepositoryException {
        return this.mockNodeData.getType();
    }

    public String getString() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getString();
    }

    public InputStream getStream() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getStream();
    }

    public Node getNode() throws ValueFormatException, RepositoryException {
        if (getType() != PropertyType.REFERENCE) {
            throw new ValueFormatException("Not a reference");
        }
        throw new UnsupportedOperationException();
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getLong();
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getDouble();
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getDate();
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getBoolean();
    }

    public long getLength() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getContentLength();
    }

    public long[] getLengths() throws ValueFormatException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public PropertyDefinition getDefinition() throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }
}