/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;


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

    public String getName() {
        return name;
    }

    public String getString() {
        return value !=null ? value.toString() : "";
    }

    public int getType() {
        return type;
    }

    public boolean getBoolean() {
        try {
            return ((Boolean) value).booleanValue();
        } catch (Exception e) {
            // TODO : this is mimicing the (very wrong) behaviour of DefaultNodeData - see MAGNOLIA-2237
            return false;
        }
    }

    public Calendar getDate() {
        try {
            return (Calendar) value;
        } catch (Exception e) {
            // TODO : this is mimicing the (very wrong) behaviour of DefaultNodeData - see MAGNOLIA-2237
            return null;
        }
    }

    public double getDouble() {
        try {
            return ((Double) value).doubleValue();
        } catch (Exception e) {
            // TODO : this is mimicing the (very wrong) behaviour of DefaultNodeData - see MAGNOLIA-2237
            return 0;
        }
    }

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

    public InputStream getStream() {
        try {
            return (InputStream) value;
        } catch (Exception e) {
            // TODO : this is mimicing the (very wrong) behaviour of DefaultNodeData - see MAGNOLIA-2237
            return null;
        }
    }

    public String getHandle() {
        try {
            return getParent().getHandle() + "/" + this.getName();
        }
        catch (RepositoryException e) {
            throw new RuntimeException("Can't build handle", e);
        }
    }

    public boolean isExist() {
        return value != null;
    }

    public void setValue(boolean value) throws RepositoryException, AccessDeniedException {
        this.value = Boolean.valueOf(value);
    }

    public void setValue(Calendar value) throws RepositoryException, AccessDeniedException {
        this.value = value;
    }

    public void setValue(double value) throws RepositoryException, AccessDeniedException {
        this.value = new Double(value);
    }

    public void setValue(InputStream value) throws RepositoryException, AccessDeniedException {
        this.value = value;
    }

    public void setValue(int value) throws RepositoryException, AccessDeniedException {
        this.value = new Integer(value);
    }

    public void setValue(long value) throws RepositoryException, AccessDeniedException {
        this.value = new Long(value);
    }

    public void setValue(String value) throws RepositoryException, AccessDeniedException {
        this.value = value;
    }
    
    public void setValue(Content value) throws RepositoryException, AccessDeniedException {
        this.value = value;
    }

    public void save() throws RepositoryException {
        // nothing to do
    }

    public void delete() throws RepositoryException {
    }

    public long getContentLength() {
        return getString().length();
    }

    public Property getJCRProperty() {
        return new MockJCRProperty(this);
    }

    public Value getValue() {
        return new MockJCRValue(this);
    }

    public Value[] getValues() {
        Value[] values = new Value[1];
        values[0] = getValue();
        return values;
    }

    public void refresh(boolean keepChanges) throws RepositoryException {
        // nothing to do
    }

    public void setValue(Value value) throws RepositoryException, AccessDeniedException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setValue(Value[] value) throws RepositoryException, AccessDeniedException {
        throw new UnsupportedOperationException("Not implemented");
    }

}
