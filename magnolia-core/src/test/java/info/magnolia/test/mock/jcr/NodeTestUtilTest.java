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
package info.magnolia.test.mock.jcr;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import javax.jcr.Node;

import org.junit.Test;

/**
 * @version $Id$
 */
public class NodeTestUtilTest {
    @Test
    public void testNodeFromStringAndStrings() throws Exception{
        // GIVEN
        String string1 = "/root/sub1.prop1=one";
        String string2 = "/root/sub1.prop2=two";
        // WHEN
        Node result = NodeTestUtil.createNode("root", "testWorkspace", string1, string2);

        // THEN
        assertEquals("root", result.getName());
        assertEquals("one", result.getNode("/sub1").getProperty("prop1").getString());
        assertEquals("two", result.getNode("/sub1").getProperty("prop2").getString());
    }

    @Test
    public void testNodeFromStringAndObjectArray() throws Exception{
        // GIVEN
        Object[][] data = new String[][]{{"a", "a-value"}};

        // WHEN
        Node result = NodeTestUtil.createNode("root", data);

        // THEN
        assertEquals("root", result.getName());
        assertEquals("a-value", result.getProperty("a").getString());
    }

    @Test
    public void testCreateValues() throws Exception {
        // GIVEN
        Object[][] data = new String[][]{{"a", "a-value"}, {"b", "b-value"}};

        // WHEN
        Map<String, MockValue> result = NodeTestUtil.createValues(data);

        // THEN
        assertEquals("a-value", result.get("a").getString());
        assertEquals("b-value", result.get("b").getString());
    }
}
