/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.util;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.assertEquals;
import info.magnolia.cms.core.NodeData;
import info.magnolia.test.mock.MockNodeData;

import javax.jcr.PropertyType;
import javax.jcr.ValueFactory;

import org.junit.Test;

/**
 * Not converted to Mockito as it will be replaced anyway (when giving up Content-API).
 *
 * @version $Id$
 */
public class NodeDataUtilTest {

    @Test
    public void testCreateValueWithDouble() throws Exception{
        // GIVEN
        ValueFactory valueFactory = createNiceMock(ValueFactory.class);
        replay(valueFactory);
        double three = 3;
        Object obj = Double.valueOf(three);

        // Easymocks' expectation
        valueFactory.createValue(three);

        // WHEN
        NodeDataUtil.createValue(obj, valueFactory);

        // THEN
        verify(valueFactory);
    }

    @Test
    public void testCreateValueWithDoubleFromString() throws Exception{
        // GIVEN
        ValueFactory valueFactory = createNiceMock(ValueFactory.class);
        replay(valueFactory);
        double three = 3;
        String obj = "" + three;

        // Easymocks' expectation
        valueFactory.createValue(three);

        // WHEN
        NodeDataUtil.createValue(obj, PropertyType.DOUBLE, valueFactory);

        // THEN
        verify(valueFactory);
    }

    @Test
    public void testCreateValueWithFloat() throws Exception{
        // GIVEN
        ValueFactory valueFactory = createNiceMock(ValueFactory.class);
        replay(valueFactory);
        float three = 3f;
        double threeAsDouble = three;
        Object obj = Float.valueOf(three);

        // Easymocks' expectation
        valueFactory.createValue(threeAsDouble);

        // WHEN
        NodeDataUtil.createValue(obj, valueFactory);

        // THEN
        verify(valueFactory);
    }

    @Test
    public void testCreateValueWithInteger() throws Exception{
        // GIVEN
        ValueFactory valueFactory = createNiceMock(ValueFactory.class);
        replay(valueFactory);
        int three = 3;
        long threeAsLong = three;
        Object obj = Integer.valueOf(three);

        // Easymocks' expectation
        valueFactory.createValue(threeAsLong);

        // WHEN
        NodeDataUtil.createValue(obj, valueFactory);

        // THEN
        verify(valueFactory);
    }

    @Test
    public void testCreateValueWithLong() throws Exception{
        // GIVEN
        ValueFactory valueFactory = createNiceMock(ValueFactory.class);
        replay(valueFactory);
        long three = 3;
        Object obj = Long.valueOf(three);

        // Easymocks' expectation
        valueFactory.createValue(three);

        // WHEN
        NodeDataUtil.createValue(obj, valueFactory);

        // THEN
        verify(valueFactory);
    }

    @Test
    public void testCreateValueWithLongFromString() throws Exception{
        // GIVEN
        ValueFactory valueFactory = createNiceMock(ValueFactory.class);
        replay(valueFactory);
        long three = 3;
        String obj = "" + three;

        // Easymocks' expectation
        valueFactory.createValue(three);

        // WHEN
        NodeDataUtil.createValue(obj, PropertyType.LONG, valueFactory);

        // THEN
        verify(valueFactory);
    }

    @Test
    public void testSetNodeDataWithDouble() throws Exception {
        // GIVEN
        NodeData data = new MockNodeData("test", 0);

        // WHEN
        NodeDataUtil.setValue(data, Double.valueOf(3));

        // THEN
        assertEquals(3.0, data.getDouble(), 0.0);
    }

    @Test
    public void testSetNodeDataWithFloat() throws Exception {
        // GIVEN
        NodeData data = new MockNodeData("test", 0);

        // WHEN
        NodeDataUtil.setValue(data, Float.valueOf(3));

        // THEN
        assertEquals(3.0, data.getDouble(), 0.0);
    }

    @Test
    public void testSetNodeDataWithInteger() throws Exception {
        // GIVEN
        NodeData data = new MockNodeData("test", 0);

        // WHEN
        NodeDataUtil.setValue(data, Integer.valueOf(3));

        // THEN - JCR doesn't support storage of integers - instead they get converted to longs
        assertEquals(3, data.getLong());
    }

    @Test
    public void testSetNodeDataWithLong() throws Exception {
        // GIVEN
        NodeData data = new MockNodeData("test", 0);

        // WHEN
        NodeDataUtil.setValue(data, Long.valueOf(3));

        // THEN
        assertEquals(3, data.getLong());
    }
}
