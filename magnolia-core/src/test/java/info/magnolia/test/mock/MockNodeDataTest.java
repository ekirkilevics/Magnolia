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
package info.magnolia.test.mock;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;

import java.util.Calendar;

import javax.jcr.PropertyType;

import junit.framework.TestCase;

/**
 * @version $Id$
 */
public class MockNodeDataTest extends TestCase {

    public void testSetValueWithParamValue() throws Exception {
        doTestSetValueWithParamValue("String", "Hello World");
        doTestSetValueWithParamValue("Long", Long.valueOf(123l));
        doTestSetValueWithParamValue("Date", Calendar.getInstance());
        doTestSetValueWithParamValue("Boolean", Boolean.TRUE);
    }

    private void doTestSetValueWithParamValue(String nodeDataName, Object nodeDataValue) throws Exception{
        final MockNodeData nodeData = new MockNodeData("toTest", PropertyType.BINARY);
        final MockNodeData jcrValueNodeData = new MockNodeData(nodeDataName, nodeDataValue);
        MockJCRValue jcrValue = new MockJCRValue(jcrValueNodeData);
        nodeData.setValue(jcrValue);

        assertEquals(jcrValueNodeData, jcrValueNodeData);
    }

    public void testCanGetHandle() throws Exception {
        MockHierarchyManager hm = MockUtil.createHierarchyManager("/node/sub/sub2.a=lol\n");
        final Content node = hm.getContent("/node/sub/sub2");
        final NodeData p = node.getNodeData("a");
        assertEquals("/node/sub/sub2/a", p.getHandle());
    }

    public void testCanDeleteSelf() throws Exception {
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node.a=lol\n" +
            "/node.b=yop\n" +
            "/node.c=boum\n");
        final NodeData p = hm.getNodeData("/node/b");
        assertEquals("yop", p.getString());
        p.delete();

        final Content node = hm.getContent("/node");
        assertEquals(2, node.getNodeDataCollection().size());
        assertEquals(true, node.getNodeData("a").isExist());
        assertEquals(true, node.getNodeData("c").isExist());
        assertEquals(false, node.getNodeData("b").isExist());
    }

}
