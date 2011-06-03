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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import javax.jcr.ItemVisitor;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.junit.Test;

/**
 * @version $Id$
 */
public class MockPropertyTest {

    @Test
    public void testIsMultiple() throws RepositoryException {
        Property property = new MockProperty("test", "test");
        assertTrue(!property.isMultiple());
    }

    @Test
    public void testIsNode() {
        Property property = new MockProperty("test", "test");
        assertTrue(!property.isNode());
    }

    @Test
    public void testGetSetValueWithBigDecimal() throws Exception{
        Property property = new MockProperty("test", "test");
        property.setValue(BigDecimal.ONE);
        assertEquals(BigDecimal.ONE, property.getDecimal());
    }

    @Test
    public void testGetString() throws ValueFormatException, RepositoryException {
        String stringValue = "string";
        Property property = new MockProperty("test", new MockValue(stringValue));
        assertEquals(stringValue, property.getString());
    }

    @Test
    public void testGetType() throws RepositoryException {
        String stringValue = "string";
        Property property = new MockProperty("test", new MockValue(stringValue));
        assertEquals(PropertyType.STRING, property.getType());
    }

    @Test
    public void testGetValue() throws ValueFormatException, RepositoryException {
        String stringValue = "string";
        MockValue mockValue = new MockValue(stringValue);
        Property property = new MockProperty("test", mockValue);
        assertEquals(mockValue, property.getValue());
    }

    @Test
    public void testAccept() throws RepositoryException{
        Property property = new MockProperty("test", "test");
        ItemVisitor visitor = mock(ItemVisitor.class);
        property.accept(visitor);

        verify(visitor).visit(property);
    }

}
