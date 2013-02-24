/**
 * This file Copyright (c) 2011-2012 Magnolia International
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

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.AbstractProperty;

/**
 * Mock-impl. for javax.jcr.Property - basically wrapping a MockValue. It's currently overriding all setValue methods
 * from AbstractProperty. This has the advantage, we don't need a parent (Node) set. Not sure how we could benefit from
 * delegating the set to the Node.
 *
 * @version $Id$
 */
public class MockProperty extends AbstractProperty {

    private final String name;
    private MockNode parent;
    private Session session;
    private Value value;

    public MockProperty(String name, Value value, MockNode parent) {
        this.name = name;
        this.parent = parent;
        this.value = value;
        setSessionFrom(parent);
    }

    public MockProperty(String name, Object objectValue, MockNode parent) {
        this(name, new MockValue(objectValue), parent);
    }

    @Override
    public void accept(ItemVisitor visitor) throws RepositoryException {
        visitor.visit(this);
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
    public long[] getLengths() throws ValueFormatException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public long getLong() throws ValueFormatException, RepositoryException {
        return getValue().getLong();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Node getNode() throws RepositoryException {
        return getSession().getNodeByIdentifier(getValue().getString());
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        String path = getValue().getString();
        if (StringUtils.isEmpty(path)) {
            return this;
        }
        if (!path.startsWith("/")) {
            throw new UnsupportedOperationException("Only absolute path references supported yet. This is a fake class.");
        }
        return (Property) getSession().getItem(path);
    }

    @Override
    public Session getSession() {
        if (session == null && parent != null) {
            // fallback - avoid session has to be set on every level
            return parent.getSession();
        }
        return session;
    }

    @Override
    public InputStream getStream() throws ValueFormatException, RepositoryException {
        return getValue().getStream();
    }

    /**
     * Return the value as String or null if the value is null.
     */
    @Override
    public String getString() throws ValueFormatException, RepositoryException {
        return getValue() != null ? getValue().getString() : null;
    }

    @Override
    public int getType() throws RepositoryException {
        return getValue().getType();
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public Value[] getValues() throws ValueFormatException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean isModified() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isMultiple() throws RepositoryException {
        // Multiple not supported (yet)
        return false;
    }

    @Override
    public boolean isNew() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSame(Item otherItem) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {
    }

    @Override
    public void remove() {
        ((MockNode) getParent()).removeProperty(getName());
        setParent(null);
    }

    @Override
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
    }

    public void setParent(MockNode parent) {
        this.parent = parent;
        setSessionFrom(parent);
    }

    public void setSession(Session session) {
        this.session = session;
    }

    private void setSessionFrom(MockNode parent) {
        setSession(parent == null ? null : parent.getSession());
    }

    @Override
    public void setValue(BigDecimal value) throws RepositoryException {
        setValueFromObject(value);
    }

    @Override
    public void setValue(Binary value) throws RepositoryException {
        setValueFromObject(value);
    }

    @Override
    public void setValue(boolean value) throws RepositoryException {
        setValueFromObject(value);
    }

    @Override
    public void setValue(Calendar value) throws RepositoryException {
        setValueFromObject(value);
    }

    @Override
    public void setValue(double value) throws RepositoryException {
        setValueFromObject(value);
    }

    @Override
    public void setValue(InputStream value) throws RepositoryException {
        setValueFromObject(value);
    }

    @Override
    public void setValue(long value) throws RepositoryException {
        setValueFromObject(value);
    }

    @Override
    public void setValue(String value) throws RepositoryException {
        setValueFromObject(value);
    }

    private void setValueFromObject(Object value) {
        setValue(new MockValue(value));
    }

    @Override
    public void setValue(Value value) {
        this.value = value;
    }

    public Object getObjectValue() {
        return ((MockValue) getValue()).getValue();
    }
}
