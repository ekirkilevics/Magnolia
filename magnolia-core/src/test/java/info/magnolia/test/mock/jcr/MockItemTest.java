/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import org.junit.Test;

/**
 * @version $Id$
 */
public class MockItemTest {

    @Test
    public void testGetDepth() throws Exception {
        // GIVEN
        final MockNode root = new MockNode("root");
        final MockNode child = new MockNode("child");
        final MockNode childOfChild = new MockNode("childOfChild");
        final String propertyName = "prop";

        root.addNode(child);
        child.addNode(childOfChild);
        child.setProperty(propertyName, "test");

        // WHEN - in that case we don't have a real WHEN...

        // THEN - expected values set as stated in javadoc of Item#getDetph
        assertEquals(0, root.getDepth());
        assertEquals(1, child.getDepth());
        assertEquals(2, childOfChild.getDepth());
        assertEquals(2, child.getProperty(propertyName).getDepth());
    }

    @Test
    public void testGetPath() throws Exception {
        // GIVEN
        final MockNode root = new MockNode("root");
        final MockNode child = new MockNode("child");
        final MockNode childOfChild = new MockNode("childOfChild");
        root.addNode(child);
        child.addNode(childOfChild);

        // WHEN - in that case we don't have a real WHEN...

        // THEN
        assertEquals("/root/child", child.getPath());
        assertEquals("/root/child/childOfChild", childOfChild.getPath());
    }
}
