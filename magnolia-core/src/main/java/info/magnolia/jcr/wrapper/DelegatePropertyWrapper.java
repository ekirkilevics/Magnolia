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
package info.magnolia.jcr.wrapper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
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
 * Wrapper for JCR property.
 *
 * @version $Id$
 */
public abstract class DelegatePropertyWrapper implements Property {

    private Property wrapped;

    public DelegatePropertyWrapper(Property wrapped) {
        this.wrapped = wrapped;
    }

    protected Property getWrappedProperty() {
        return wrapped;
    }

    protected void setWrappedProperty(Property property) {
        this.wrapped = property;
    }

    @Override
    public String toString() {
        return wrapped != null ? wrapped.toString() : "";
    }

    /////////////
    //
    //  Delegating method stubs
    //
    /////////////

    @Override
    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedProperty().setValue(value);
    }

    @Override
    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedProperty().setValue(values);
    }

    @Override
    public void setValue(String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedProperty().setValue(value);
    }

    @Override
    public void setValue(String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedProperty().setValue(values);
    }

    @Override
    public void setValue(InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedProperty().setValue(value);
    }

    @Override
    public void setValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedProperty().setValue(value);
    }

    @Override
    public void setValue(long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedProperty().setValue(value);
    }

    @Override
    public void setValue(double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedProperty().setValue(value);
    }

    @Override
    public void setValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedProperty().setValue(value);
    }

    @Override
    public void setValue(Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedProperty().setValue(value);
    }

    @Override
    public void setValue(boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedProperty().setValue(value);
    }

    @Override
    public void setValue(Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedProperty().setValue(value);
    }

    @Override
    public Value getValue() throws ValueFormatException, RepositoryException {
        return getWrappedProperty().getValue();
    }

    @Override
    public Value[] getValues() throws ValueFormatException, RepositoryException {
        return getWrappedProperty().getValues();
    }

    @Override
    public String getString() throws ValueFormatException, RepositoryException {
        return getWrappedProperty().getString();
    }

    @Override
    public InputStream getStream() throws ValueFormatException, RepositoryException {
        return getWrappedProperty().getStream();
    }

    @Override
    public Binary getBinary() throws ValueFormatException, RepositoryException {
        return getWrappedProperty().getBinary();
    }

    @Override
    public long getLong() throws ValueFormatException, RepositoryException {
        return getWrappedProperty().getLong();
    }

    @Override
    public double getDouble() throws ValueFormatException, RepositoryException {
        return getWrappedProperty().getDouble();
    }

    @Override
    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return getWrappedProperty().getDecimal();
    }

    @Override
    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return getWrappedProperty().getDate();
    }

    @Override
    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return getWrappedProperty().getBoolean();
    }

    @Override
    public Node getNode() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        return getWrappedProperty().getNode();
    }

    @Override
    public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        return getWrappedProperty().getProperty();
    }

    @Override
    public long getLength() throws ValueFormatException, RepositoryException {
        return getWrappedProperty().getLength();
    }

    @Override
    public long[] getLengths() throws ValueFormatException, RepositoryException {
        return getWrappedProperty().getLengths();
    }

    @Override
    public PropertyDefinition getDefinition() throws RepositoryException {
        return getWrappedProperty().getDefinition();
    }

    @Override
    public int getType() throws RepositoryException {
        return getWrappedProperty().getType();
    }

    @Override
    public boolean isMultiple() throws RepositoryException {
        return getWrappedProperty().isMultiple();
    }

    @Override
    public String getPath() throws RepositoryException {
        return getWrappedProperty().getPath();
    }

    @Override
    public String getName() throws RepositoryException {
        return getWrappedProperty().getName();
    }

    @Override
    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return getWrappedProperty().getAncestor(depth);
    }

    @Override
    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return getWrappedProperty().getParent();
    }

    @Override
    public int getDepth() throws RepositoryException {
        return getWrappedProperty().getDepth();
    }

    @Override
    public Session getSession() throws RepositoryException {
        return getWrappedProperty().getSession();
    }

    @Override
    public boolean isNode() {
        return getWrappedProperty().isNode();
    }

    @Override
    public boolean isNew() {
        return getWrappedProperty().isNew();
    }

    @Override
    public boolean isModified() {
        return getWrappedProperty().isModified();
    }

    @Override
    public boolean isSame(Item otherItem) throws RepositoryException {
        return getWrappedProperty().isSame(otherItem);
    }

    @Override
    public void accept(ItemVisitor visitor) throws RepositoryException {
        getWrappedProperty().accept(visitor);
    }

    @Override
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        getWrappedProperty().save();
    }

    @Override
    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {
        getWrappedProperty().refresh(keepChanges);
    }

    @Override
    public void remove() throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
        getWrappedProperty().remove();
    }
}
