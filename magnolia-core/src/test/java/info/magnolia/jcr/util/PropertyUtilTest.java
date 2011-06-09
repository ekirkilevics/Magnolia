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
package info.magnolia.jcr.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockValue;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class PropertyUtilTest {
    private MockNode root;
    private static final String PROPERTY_NAME = "test";

    @Before
    public void setUp() {
        root = new MockNode("root");
    }

    @Test
    public void testOrderLast() throws RepositoryException {
        final String newPropertyName = "newPropertyName";
        final Property property = root.setProperty(PROPERTY_NAME, "value");

        PropertyUtil.renameProperty(property, newPropertyName);

        assertTrue(!root.hasProperty(PROPERTY_NAME));
        assertEquals("value", root.getProperty(newPropertyName).getString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPropertyThrowsExceptionOnNullNode() throws RepositoryException {
        PropertyUtil.setProperty(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPropertyThrowsExceptionOnNullName() throws RepositoryException {
        PropertyUtil.setProperty(root, null, null);
    }

    @Test
    public void testSetPropertyToString() throws RepositoryException {
        final Object value = "value";
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);
        assertEquals(value, root.getProperty(PROPERTY_NAME).getString());
    }

    @Test
    public void testSetPropertyToBigDecimal() throws RepositoryException {
        final Object value = BigDecimal.ONE;
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);
        assertEquals(value, root.getProperty(PROPERTY_NAME).getDecimal());
    }

    @Test
    public void testSetPropertyToCalendar() throws RepositoryException {
        final Object value = Calendar.getInstance();
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);
        assertEquals(value, root.getProperty(PROPERTY_NAME).getDate());
    }

    @Test
    public void testSetPropertyToValue() throws RepositoryException {
        final Object value = new MockValue("x");
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);
        assertEquals(value, root.getProperty(PROPERTY_NAME).getValue());
    }

    @Test
    public void testSetPropertyToLong() throws RepositoryException {
        final Object value = Long.valueOf(123l);
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);
        assertEquals(value, root.getProperty(PROPERTY_NAME).getLong());
    }

    @Test
    public void testSetPropertyToDouble() throws RepositoryException {
        final Object value = Double.valueOf("42.195");
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);
        assertEquals(value, root.getProperty(PROPERTY_NAME).getDouble());
    }

    @Test
    public void testSetPropertyToBoolean() throws RepositoryException {
        final Object value = Boolean.TRUE;
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);
        assertEquals(value, root.getProperty(PROPERTY_NAME).getBoolean());
    }

    @Test
    public void testSetPropertyToNode() throws RepositoryException {
        final String identifier = "identifier";
        final MockNode referenced = new MockNode("referenced");
        referenced.setIdentifier(identifier);
        PropertyUtil.setProperty(root, PROPERTY_NAME, referenced);
        assertEquals(identifier, root.getProperty(PROPERTY_NAME).getString());
    }

}
