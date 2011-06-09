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

import info.magnolia.cms.util.NodeDataUtil;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.Binary;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

/**
 * @version $Id$
 */
public class MockValue implements Value {

    private final int type;
    private Object value;

    public MockValue(Object value) {
        this(value, NodeDataUtil.getJCRPropertyType(value));
    }

    public MockValue(Object value, int type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public Binary getBinary() throws RepositoryException {
        if (value instanceof Binary) {
            return (Binary) value;
        }
        throw new ValueFormatException("Value can't be converted to Binary: " + value);
    }

    @Override
    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new ValueFormatException("Value can't be converted to Boolean: " + value);
    }

    @Override
    public Calendar getDate() throws ValueFormatException, RepositoryException {
        if (value instanceof Calendar) {
            return (Calendar) value;
        }
        throw new ValueFormatException("Value can't be converted to Calendar: " + value);
    }

    @Override
    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        throw new ValueFormatException("Value can't be converted to BigDecimal: " + value);
    }

    @Override
    public double getDouble() throws ValueFormatException, RepositoryException {
        if (value instanceof Double) {
            return (Double) value;
        }
        throw new ValueFormatException("Value can't be converted to Double: " + value);
    }

    /**
     * Convenience method.
     */
    protected long getLength() throws RepositoryException {
        if (PropertyType.BINARY == type) {
            return ((Binary) value).getSize();
        }
        return value.toString().length();
    }

    @Override
    public long getLong() throws ValueFormatException, RepositoryException {
        if (value instanceof Long) {
            return (Long) value;
        }
        throw new ValueFormatException("Value can't be converted to Long: " + value);
    }

    @Override
    public InputStream getStream() throws RepositoryException {
        if (value instanceof InputStream) {
            return (InputStream) value;
        }
        throw new ValueFormatException("Value can't be converted to InputStream: " + value);
    }

    @Override
    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
        // TODO dlipp: check whether this is the desired behavior...
        if (value instanceof String) {
            return (String) value;
        }
        throw new ValueFormatException("Value can't be converted to String: " + value);
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "MockValue [type=" + type + ", value=" + value + "]";
    }

}
