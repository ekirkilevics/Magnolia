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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.Binary;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.junit.Test;

/**
 * @version $Id$
 */
public class MockValueTest {


    @Test
    public void testConstructionWithProvidedType() throws RepositoryException {
        final String reference = "identifier";
        final MockValue value = new MockValue(reference, PropertyType.REFERENCE);
        assertEquals(PropertyType.REFERENCE, value.getType());
        assertEquals(reference, value.getString());
    }

    protected void doTestGetType(Object objectValue, int expectedType) {
        final MockValue jcrValue = new MockValue(objectValue);
        assertEquals(expectedType, jcrValue.getType());
    }

    @Test
    public void testGetBinary() throws Exception {
        Object objectValue = new Binary() {
            @Override
            public void dispose() {
            }

            @Override
            public long getSize() throws RepositoryException {
                return 0;
            }

            @Override
            public InputStream getStream() throws RepositoryException {
                return null;
            }

            @Override
            public int read(byte[] b, long position) throws IOException, RepositoryException {
                return 0;
            }
        };
        MockValue jcrValue = new MockValue(objectValue);
        assertEquals(objectValue, jcrValue.getBinary());

    }

    @Test(expected = ValueFormatException.class)
    public void testGetBinaryWithWrongValueType() throws Exception {
        new MockValue(Boolean.FALSE).getBinary();
    }

    @Test
    public void testGetBoolean() throws Exception {
        Object objectValue = Boolean.TRUE;
        MockValue jcrValue = new MockValue(objectValue);
        assertEquals(objectValue, jcrValue.getBoolean());
    }

    @Test(expected = ValueFormatException.class)
    public void testGetBooleanmWithWrongValueType() throws Exception {
        new MockValue(BigDecimal.ONE).getBoolean();
    }

    @Test
    public void testGetDate() throws Exception {
        Object objectValue = Calendar.getInstance();
        MockValue jcrValue = new MockValue(objectValue);
        assertEquals(objectValue, jcrValue.getDate());

    }

    @Test(expected = ValueFormatException.class)
    public void testGetDateWithWrongValueType() throws Exception {
        new MockValue(Boolean.FALSE).getDate();
    }

    @Test
    public void testGetDecimal() throws Exception {
        Object objectValue = BigDecimal.valueOf(123l);
        MockValue jcrValue = new MockValue(objectValue);
        assertEquals(objectValue, jcrValue.getDecimal());
    }

    @Test(expected = ValueFormatException.class)
    public void testGetDecimalWithWrongValueType() throws Exception {
        new MockValue(Boolean.FALSE).getDecimal();
    }

    @Test
    public void testGetDouble() throws Exception {
        Object objectValue = Double.valueOf(123);
        MockValue jcrValue = new MockValue(objectValue);
        assertEquals(objectValue, jcrValue.getDouble());
    }

    @Test(expected = ValueFormatException.class)
    public void testGetDoubleWithWrongValueType() throws Exception {
        new MockValue(Boolean.FALSE).getDouble();
    }

    @Test
    public void testGetLength() throws Exception {
        assertEquals(6, new MockValue("string").getLength());
        assertEquals(5, new MockValue(Boolean.FALSE).getLength());
        assertEquals(2, new MockValue(BigDecimal.valueOf(12)).getLength());
    }

    @Test
    public void testGetLongWithLong() throws Exception {
        Object objectValue = 123l;
        MockValue jcrValue = new MockValue(objectValue);
        assertEquals(objectValue, jcrValue.getLong());
    }

    @Test(expected = ValueFormatException.class)
    public void testGetLongWithWrongValueType() throws Exception {
        new MockValue(Boolean.FALSE).getLong();
    }

    @Test
    public void testGetStream() throws Exception {
        Object objectValue = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        MockValue jcrValue = new MockValue(objectValue);
        assertEquals(objectValue, jcrValue.getStream());
    }

    @Test(expected = ValueFormatException.class)
    public void testGetStreamWithWrongValueType() throws Exception {
        new MockValue(Boolean.FALSE).getStream();
    }

    @Test
    public void testGetStringWithString() throws Exception {
        Object objectValue = "String";
        MockValue jcrValue = new MockValue(objectValue);
        assertEquals(objectValue, jcrValue.getString());
    }

    @Test(expected = ValueFormatException.class)
    public void testGetStringWithWrongValueType() throws Exception {
        new MockValue(Boolean.FALSE).getString();
    }

    @Test
    public void testGetType() throws Exception {
        assertEquals(PropertyType.STRING, new MockValue("string").getType());
        assertEquals(PropertyType.BOOLEAN, new MockValue(Boolean.FALSE).getType());
        assertEquals(PropertyType.DOUBLE, new MockValue(Double.valueOf(12)).getType());
    }
}
