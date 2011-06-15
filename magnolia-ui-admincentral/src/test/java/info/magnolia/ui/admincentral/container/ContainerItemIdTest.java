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
package info.magnolia.ui.admincentral.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import info.magnolia.test.mock.jcr.MockNode;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for uuid conversion in ContainerItemId.
 * 
 * @author had
 * 
 */
public class ContainerItemIdTest {

    private ContainerItemId cii;

    @Before
    public void setup() throws Exception {
        cii = new ContainerItemId(new MockNode("blah"));

    }
    @Test
    public void testUUIDConversion() throws Exception {

        int[] array = (int[]) cii.uuid2int("159bc523-ffff-40e6-965d-4ab91a4bdd9a");
        assertEquals(8, array.length);
        String uuid = cii.int2uuid(array);
        assertEquals("159bc523-ffff-40e6-965d-4ab91a4bdd9a", uuid);
    }

    @Test
    public void testUUIDConversionZeroPadding() throws Exception {
        // check zero padding as well
        int[] array = (int[]) cii.uuid2int("4b909379-ac41-1cad-697f-0028592b4af8");
        assertEquals(8, array.length);
        String uuid = cii.int2uuid(array);
        assertEquals("4b909379-ac41-1cad-697f-0028592b4af8", uuid);
    }

    @Test
    public void testEquals() throws Exception {
        MockNode mock1 = new MockNode("blah");
        mock1.setIdentifier("4b909379-ac41-1cad-697f-0028592b4af8");

        MockNode mock2 = new MockNode("blah");
        mock2.setIdentifier("4b909379-ac41-1cad-697f-0028592b4af8");

        ContainerItemId cii1 = new ContainerItemId(mock1);
        ContainerItemId cii2 = new ContainerItemId(mock2);

        assertEquals(cii1.hashCode(), cii2.hashCode());
        assertTrue(cii1.equals(cii2));
    }
}
