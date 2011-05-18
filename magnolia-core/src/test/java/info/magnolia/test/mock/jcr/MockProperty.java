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

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;

/**
 * Mock-impl. for javax.jcr.Property - basically wrapping a MockValue.
 *
 * @version $Id$
 */
public class MockProperty extends MockItem implements Property {

    private Value value;

    public MockProperty(String name, Object objectValue) {
        this(name, new MockValue(objectValue));
    }

    public MockProperty(String name, Value value) {
        super(name);
        this.value = value;
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
    public Binary getBinary() throws ValueFormatException, RepositoryException {
        return getValue().getBinary();
    }

    @Override
    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return getValue().getBoolean();
    }

    @Override
    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return getValue().getDate();
    }

    @Override
    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return getValue().getDecimal();
    }

    @Override
    public PropertyDefinition getDefinition() throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public double getDouble() throws ValueFormatException, RepositoryException {
        return getValue().getDouble();
    }

    @Override
    public long getLength() throws ValueFormatException, RepositoryException {
        return ((MockValue) getValue()).getLength();
    }

    @Override
    public long[] getLengths() throws ValueFormatException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public long getLong() throws ValueFormatException, RepositoryException {
        return getValue().getLong();
    }

    @Override
    public Node getNode() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        // References not implemented
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        // References not implemented
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public InputStream getStream() throws ValueFormatException, RepositoryException {
        return getValue().getStream();
    }

    @Override
    public String getString() throws ValueFormatException, RepositoryException {
        return getValue().getString();
    }

    @Override
    public int getType() throws RepositoryException {
        return getValue().getType();
    }

    @Override
    public Value getValue() throws ValueFormatException, RepositoryException {
        return value;
    }

    @Override
    public Value[] getValues() throws ValueFormatException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean isMultiple() throws RepositoryException {
        // Multiple not supported (yet)
        return false;
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean isSame(Item otherItem) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void setValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        setValueFromObject(value);
    }

    @Override
    public void setValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void setValue(boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        setValueFromObject(value);
    }

    @Override
    public void setValue(Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        setValueFromObject(value);
    }

    @Override
    public void setValue(double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        setValueFromObject(value);
    }

    @Override
    public void setValue(InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void setValue(long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        setValueFromObject(value);
    }

    @Override
    public void setValue(Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        // References not implemented
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void setValue(String value) {
        setValueFromObject(value);
    }

    @Override
    public void setValue(String[] values) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public void setValue(Value[] values) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    protected void setValueFromObject(Object object) {
        value = new MockValue(object);
    }
}
