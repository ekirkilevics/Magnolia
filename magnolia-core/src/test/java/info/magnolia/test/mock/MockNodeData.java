/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.core.AbstractNodeData;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.NodeDataUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;


/**
 * @author philipp
 * @version $Id$
 */
public class MockNodeData extends AbstractNodeData {

    private final String name;
    private final int type;
    private Object value;

    public MockNodeData(String name, Object value) {
        super(null, name);
        this.name = name;
        this.value = value;
        this.type = NodeDataUtil.getJCRPropertyType(value);
    }

    public MockNodeData(String name, int type) {
        super(null, name);
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getString() {
        return value !=null ? value.toString() : "";
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public boolean getBoolean() {
        try {
            return ((Boolean) value).booleanValue();
        } catch (Exception e) {
            // TODO : this is mimicing the (very wrong) behaviour of DefaultNodeData - see MAGNOLIA-2237
            return false;
        }
    }

    @Override
    public Calendar getDate() {
        try {
            return (Calendar) value;
        } catch (Exception e) {
            // TODO : this is mimicing the (very wrong) behaviour of DefaultNodeData - see MAGNOLIA-2237
            return null;
        }
    }

    @Override
    public double getDouble() {
        try {
            return ((Double) value).doubleValue();
        } catch (Exception e) {
            // TODO : this is mimicing the (very wrong) behaviour of DefaultNodeData - see MAGNOLIA-2237
            return 0;
        }
    }

    @Override
    public long getLong() {
        try {
            if (value instanceof Integer) {
                return ((Integer)value).longValue();
            }
            return ((Long) value).longValue();
        } catch (Exception e) {
            // TODO : this is mimicing the (very wrong) behaviour of DefaultNodeData - see MAGNOLIA-2237
            return 0;
        }
    }

    @Override
    public InputStream getStream() {
        if (value instanceof InputStream) {
            return (InputStream) value;
        }
        final String s = getString();
        if (s == null) {
            // TODO : this is mimicing the (very wrong) behaviour of DefaultNodeData - see MAGNOLIA-2237
            return null;
        }
        try {
            return new ByteArrayInputStream(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getHandle() {
        try {
            if(getParent() != null){
                return getParent().getHandle() + "/" + this.getName();
            }
            else{
                return this.getName();
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException("Can't build handle", e);
        }
    }

    @Override
    protected Content getContentFromJCRReference() throws RepositoryException {
        if (value instanceof Content) {
            return (Content) value;
        }
        throw new ValueFormatException("Not a reference");
    }

    @Override
    public boolean isExist() {
        return value != null;
    }

    @Override
    public void setValue(boolean value) throws RepositoryException, AccessDeniedException {
        this.value = Boolean.valueOf(value);
    }

    @Override
    public void setValue(Calendar value) throws RepositoryException, AccessDeniedException {
        this.value = value;
    }

    @Override
    public void setValue(double value) throws RepositoryException, AccessDeniedException {
        this.value = Double.valueOf(value);
    }

    @Override
    public void setValue(InputStream value) throws RepositoryException, AccessDeniedException {
        this.value = value;
    }

    @Override
    public void setValue(int value) throws RepositoryException, AccessDeniedException {
        this.value = Integer.valueOf(value);
    }

    @Override
    public void setValue(long value) throws RepositoryException, AccessDeniedException {
        this.value = Long.valueOf(value);
    }

    @Override
    public void setValue(String value) throws RepositoryException, AccessDeniedException {
        this.value = value;
    }

    @Override
    public void setValue(Content value) throws RepositoryException, AccessDeniedException {
        this.value = value;
    }

    @Override
    public void save() throws RepositoryException {
        // nothing to do
    }

    @Override
    public void delete() throws RepositoryException {
        getParent().deleteNodeData(this.name);
    }

    @Override
    public long getContentLength() {
        return getString().length();
    }

    @Override
    public Property getJCRProperty() throws PathNotFoundException {
        if(isExist()){
            return new MockJCRProperty(this);
        }
        throw new PathNotFoundException(getHandle());
    }

    @Override
    public Value getValue() {
        return new MockJCRValue(this);
    }

    @Override
    public Value[] getValues() {
        Value[] values = new Value[1];
        values[0] = getValue();
        return values;
    }

    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        // nothing to do
    }

    @Override
    public void setValue(Value value) throws RepositoryException, AccessDeniedException {
        switch (value.getType()) {
        case PropertyType.STRING:
            setValue(value.getString());
            break;
        case PropertyType.LONG:
            setValue(value.getLong());
            break;
        case PropertyType.DATE:
            setValue(value.getDate());
            break;
        case PropertyType.BOOLEAN:
            setValue(value.getBoolean());
        // TODO complete when required...
            break;

        default:
            throw new UnsupportedOperationException("Not implemented");

        }
    }

    @Override
    public void setValue(Value[] value) throws RepositoryException, AccessDeniedException {
        throw new UnsupportedOperationException("Not implemented");
    }

}
