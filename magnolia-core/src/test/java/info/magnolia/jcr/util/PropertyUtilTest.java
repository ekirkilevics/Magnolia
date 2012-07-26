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

import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockValue;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


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
        // GIVEN
        final String newPropertyName = "newPropertyName";
        final Property property = root.setProperty(PROPERTY_NAME, "value");

        // WHEN
        PropertyUtil.renameProperty(property, newPropertyName);

        // THEN
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
        // GIVEN
        final Object value = "value";
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        String res = root.getProperty(PROPERTY_NAME).getString();

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testSetPropertyToBigDecimal() throws RepositoryException {
        // GIVEN
        final Object value = BigDecimal.ONE;
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        BigDecimal res = root.getProperty(PROPERTY_NAME).getDecimal();

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testSetPropertyToCalendar() throws RepositoryException {
        // GIVEN
        final Object value = Calendar.getInstance();
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        Calendar res = root.getProperty(PROPERTY_NAME).getDate();

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testSetPropertyToDate() throws RepositoryException {
        // GIVEN
        final Object value = new Date();
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        Calendar res = root.getProperty(PROPERTY_NAME).getDate();

        // THEN
        assertEquals(value, res.getTime());
    }

    @Test
    public void testSetPropertyToValue() throws RepositoryException {
        // GIVEN
        final Object value = new MockValue("x");
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        Value res = root.getProperty(PROPERTY_NAME).getValue();

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testSetPropertyToLong() throws RepositoryException {
        // GIVEN
        final Object value = Long.valueOf(123l);
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        long resr = root.getProperty(PROPERTY_NAME).getLong();

        // THEN
        assertEquals(value, resr);
    }


    @Test
    public void testSetPropertyToDouble() throws RepositoryException {
        // GIVEN
        final Object value = Double.valueOf("42.195");
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        double res = root.getProperty(PROPERTY_NAME).getDouble();

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testSetPropertyToBoolean() throws RepositoryException {
        // GIVEN
        final Object value = Boolean.TRUE;
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        boolean res = root.getProperty(PROPERTY_NAME).getBoolean();

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testSetPropertyToNode() throws RepositoryException {
        // GIVEN
        final String identifier = "identifier";
        final MockNode referenced = new MockNode("referenced");
        referenced.setIdentifier(identifier);
        PropertyUtil.setProperty(root, PROPERTY_NAME, referenced);

        // WHEN
        String res = root.getProperty(PROPERTY_NAME).getString();

        // THEN
        assertEquals(identifier, res);
    }

    @Test
    public void testSetPropertyToNul() throws RepositoryException {
        // GIVEN
        final Object value = null;

        // WHEN
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // THEN
        assertFalse(root.hasProperty(PROPERTY_NAME));
    }

    @Test
    public void testGetStringTwoArgs() throws RepositoryException {
        // GIVEN
        String value = "value";
        root.setProperty(PROPERTY_NAME, value);

        // WHEN
        String res = PropertyUtil.getString(root, PROPERTY_NAME);

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testGetStringThreeArgs() throws RepositoryException {
        // GIVEN
        String defaultValue = "defaultValue";
        String value = "value";
        root.setProperty(PROPERTY_NAME, value);

        // WHEN
        String res = PropertyUtil.getString(root, PROPERTY_NAME, defaultValue);

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testGetStringThreeArgsBadNodeName() throws RepositoryException {
        // GIVEN
        String defaultValue = "defaultValue";
        String value = "value";
        root.setProperty(PROPERTY_NAME + "xx", value);

        // WHEN
        String res = PropertyUtil.getString(root, PROPERTY_NAME, defaultValue);

        // THEN
        assertEquals(defaultValue, res);
    }

    @Test
    public void testGetStringThreeArgsBadNodeType() throws RepositoryException {
        // GIVEN
        String defaultValue = "defaultValue";
        BigDecimal value = BigDecimal.ONE;
        root.setProperty(PROPERTY_NAME, value);

        // WHEN
        String res = PropertyUtil.getString(root, PROPERTY_NAME, defaultValue);

        // THEN
        assertEquals(value.toString(), res);
    }

    @Test
    public void testGetDateTwoArgs() throws RepositoryException {
        // GIVEN
        Calendar value = Calendar.getInstance();
        value.set(Calendar.YEAR, 2011);
        value.set(Calendar.MONTH, 9 - 1);
        value.set(Calendar.DAY_OF_MONTH, 5);
        value.set(Calendar.HOUR_OF_DAY, 0);
        value.set(Calendar.MINUTE, 0);
        value.set(Calendar.SECOND, 0);
        value.set(Calendar.MILLISECOND, 0);

        root.setProperty(PROPERTY_NAME, value);

        // WHEN
        Calendar res = PropertyUtil.getDate(root, PROPERTY_NAME);

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testGetDateThreeArgs() throws RepositoryException {
        // GIVEN
        Calendar defaultValue = Calendar.getInstance();
        Calendar value = Calendar.getInstance();
        value.set(Calendar.YEAR, 2011);
        value.set(Calendar.MONTH, 9 - 1);
        value.set(Calendar.DAY_OF_MONTH, 5);
        value.set(Calendar.HOUR_OF_DAY, 0);
        value.set(Calendar.MINUTE, 0);
        value.set(Calendar.SECOND, 0);
        value.set(Calendar.MILLISECOND, 0);

        root.setProperty(PROPERTY_NAME, value);

        // WHEN
        Calendar res = PropertyUtil.getDate(root, PROPERTY_NAME, defaultValue);

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testGetDetThreeArgsBadNodeName() throws RepositoryException {
        // GIVEN
        Calendar defaultValue = Calendar.getInstance();
        Calendar value = Calendar.getInstance();
        value.set(Calendar.YEAR, 2011);
        value.set(Calendar.MONTH, 9 - 1);
        value.set(Calendar.DAY_OF_MONTH, 5);
        value.set(Calendar.HOUR_OF_DAY, 0);
        value.set(Calendar.MINUTE, 0);
        value.set(Calendar.SECOND, 0);
        value.set(Calendar.MILLISECOND, 0);

        root.setProperty(PROPERTY_NAME + "xx", value);

        // WHEN
        Calendar res = PropertyUtil.getDate(root, PROPERTY_NAME, defaultValue);

        // THEN
        assertEquals(defaultValue, res);
    }

    @Test
    public void testGetDateThreeArgsBadNodeType() throws RepositoryException {
        // GIVEN
        Calendar defaultValue = Calendar.getInstance();
        BigDecimal value = BigDecimal.ONE;
        root.setProperty(PROPERTY_NAME, value);

        // WHEN
        Calendar res = PropertyUtil.getDate(root, PROPERTY_NAME, defaultValue);

        // THEN
        assertEquals(defaultValue, res);
    }

    @Test
    public void testGetBoolean() throws RepositoryException {
        boolean defaultValue = false;
        boolean value = true;
        root.setProperty(PROPERTY_NAME, value);

        // WHEN
        boolean res = PropertyUtil.getBoolean(root, PROPERTY_NAME, defaultValue);

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testGetBooleanBadNodeName() throws RepositoryException {
        // GIVEN
        boolean defaultValue = false;
        boolean value = true;
        root.setProperty(PROPERTY_NAME + "xx", value);

        // WHEN
        boolean res = PropertyUtil.getBoolean(root, PROPERTY_NAME, defaultValue);

        // THEN
        assertEquals(defaultValue, res);
    }

    @Test
    public void testGetBooleanBadNodeType() throws RepositoryException {
        // GIVEN
        boolean defaultValue = false;
        BigDecimal value = BigDecimal.ONE;
        root.setProperty(PROPERTY_NAME, value);

        // WHEN
        boolean res = PropertyUtil.getBoolean(root, PROPERTY_NAME, defaultValue);

        // THEN
        assertEquals(defaultValue, res);
    }
    @Test
    public void testGetProperty() throws RepositoryException {
        // GIVEN
        Property res = null;
        String propertyValue = "value";
        String propertyName = "myProperty";
        root.setProperty(propertyName, propertyValue);

        // WHEN
        res = PropertyUtil.getProperty(root, "myProperty");

        // THEN
        assertEquals("Props Name should be "+propertyName,propertyName, res.getName());
        assertEquals("Props Value should be "+propertyValue,propertyValue, res.getString());
    }
    @Test
    public void testGetPropertyPathNotFoundException() throws RepositoryException {
        // GIVEN
        Property res = null;
        String propertyValue = "value";
        String propertyName = "myProperty";
        root.setProperty(propertyName, propertyValue);

        // WHEN
        res = PropertyUtil.getProperty(root, "myProperty"+2);

        // THEN
        assertEquals("Should be Null  ",null,res);
    }

    @Test
    public void testGetPropertyValueObjectDouble() throws RepositoryException {
        // GIVEN
        final Object value = Double.valueOf("42.195");
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        double res = (Double)PropertyUtil.getPropertyValueObject(root,PROPERTY_NAME);

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testGetPropertyValueObjectString() throws RepositoryException {
        // GIVEN
        final Object value = "value";
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        String res = (String)PropertyUtil.getPropertyValueObject(root,PROPERTY_NAME);

        // THEN
        assertEquals(value, res);
    }


    @Test
    public void testGetPropertyValueObjectCalendar() throws RepositoryException {
        // GIVEN
        final Object value = Calendar.getInstance();
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        Calendar res = Calendar.getInstance();
        res.setTime((Date)PropertyUtil.getPropertyValueObject(root,PROPERTY_NAME));

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testGetPropertyValueObjectDate() throws RepositoryException {
        // GIVEN
        final Object value = new Date();
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        Date res = (Date)PropertyUtil.getPropertyValueObject(root,PROPERTY_NAME);

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testGetPropertyValueObjectBoolean() throws RepositoryException {
        // GIVEN
        final Object value = Boolean.TRUE;
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        boolean res = (Boolean)PropertyUtil.getPropertyValueObject(root,PROPERTY_NAME);

        // THEN
        assertEquals(value, res);
    }

    @Test
    public void testGetPropertyValueObjectLong() throws RepositoryException {
        // GIVEN
        final Object value = Long.valueOf(123l);
        PropertyUtil.setProperty(root, PROPERTY_NAME, value);

        // WHEN
        long resr = (Long)PropertyUtil.getPropertyValueObject(root,PROPERTY_NAME);

        // THEN
        assertEquals(value, resr);
    }


}
