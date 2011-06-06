/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.Binary;
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
 * @version $Id$
 * @deprecated since 5.0 - use {@link info.magnolia.test.mock.jcr.MockProperty} instead.
 */

@Deprecated
public class MockJCRProperty implements Property {

    private final MockNodeData mockNodeData;
    private boolean multiple;

    /**
     * @param mockNodeData
     */
    MockJCRProperty(MockNodeData mockNodeData) {
        this.mockNodeData = mockNodeData;
    }

    @Override
    public void save() throws javax.jcr.AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException,
    ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        this.mockNodeData.save();
    }

    @Override
    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        this.mockNodeData.delete();
    }

    @Override
    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {
        this.mockNodeData.refresh(keepChanges);
    }

    @Override
    public boolean isSame(Item otherItem) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean isNew() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isModified() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Session getSession() throws RepositoryException {
        return this.mockNodeData.getParent().getHierarchyManager().getWorkspace().getSession();
    }

    @Override
    public String getPath() throws RepositoryException {
        return this.mockNodeData.getHandle();
    }

    @Override
    public Node getParent() throws ItemNotFoundException, javax.jcr.AccessDeniedException, RepositoryException {
        return this.mockNodeData.getParent().getJCRNode();
    }

    @Override
    public String getName() throws RepositoryException {
        return this.mockNodeData.getName();
    }

    @Override
    public int getDepth() throws RepositoryException {
        return this.mockNodeData.getParent().getLevel() + 1;
    }

    @Override
    public Item getAncestor(int depth) throws ItemNotFoundException, javax.jcr.AccessDeniedException, RepositoryException {
        return this.mockNodeData.getParent().getParent().getAncestor(depth -1).getJCRNode();
    }

    @Override
    public void accept(ItemVisitor visitor) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setValue(Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setValue(boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        this.mockNodeData.setValue(value);
    }

    @Override
    public void setValue(Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        this.mockNodeData.setValue(value);
    }

    @Override
    public void setValue(double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        this.mockNodeData.setValue(value);
    }

    @Override
    public void setValue(long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        this.mockNodeData.setValue(value);
    }

    @Override
    public void setValue(InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        this.mockNodeData.setValue(value);
    }

    @Override
    public void setValue(String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
    }

    @Override
    public void setValue(String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        this.mockNodeData.setValue(value);
    }

    @Override
    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        this.mockNodeData.setValue(values);
    }

    @Override
    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        this.mockNodeData.setValue(value);
    }

    @Override
    public Value[] getValues() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getValues();
    }

    @Override
    public Value getValue() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getValue();
    }

    @Override
    public int getType() throws RepositoryException {
        return this.mockNodeData.getType();
    }

    @Override
    public String getString() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getString();
    }

    @Override
    public InputStream getStream() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getStream();
    }

    @Override
    public Node getNode() throws ValueFormatException, RepositoryException {
        if (getType() != PropertyType.REFERENCE) {
            throw new ValueFormatException("Not a reference");
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getLong();
    }

    @Override
    public double getDouble() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getDouble();
    }

    @Override
    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getDate();
    }

    @Override
    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getBoolean();
    }

    @Override
    public long getLength() throws ValueFormatException, RepositoryException {
        return this.mockNodeData.getContentLength();
    }

    @Override
    public long[] getLengths() throws ValueFormatException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PropertyDefinition getDefinition() throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    @Override
    public void setValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    @Override
    public Binary getBinary() throws ValueFormatException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isMultiple() throws RepositoryException {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mockNodeData == null) ? 0 : mockNodeData.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MockJCRProperty other = (MockJCRProperty) obj;
        if (mockNodeData == null) {
            if (other.mockNodeData != null) {
                return false;
            }
        } else if (!mockNodeData.equals(other.mockNodeData)) {
            return false;
        }
        return true;
    }
}
