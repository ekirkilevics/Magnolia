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
package info.magnolia.jcr.nodebuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.ItemType;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.MockValue;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class OpsTest {

    private static final String CHILD_NAME = "child";
    private static final String PROPERTY_NAME = "property1";
    private static final String PROPERTY_VALUE = "propertValue";
    private final ErrorHandler eh = new RuntimeExceptionThrowingErrorHandler();
    private MockNode rootNode;
    private MockSession session;

    @Before
    public void setUp() {
        rootNode = new MockNode("root");
        session = new MockSession("testSession");
        rootNode.setSession(session);
    }

    @Test
    public void testAddNodeWithString() throws RepositoryException {
        final NodeOperation op = Ops.addNode(CHILD_NAME);
        op.exec(rootNode, eh);
        assertTrue(rootNode.hasNode(CHILD_NAME));
    }

    @Test
    public void testAddNodeWithStringAndItemType() throws RepositoryException {
        final ItemType itemType = ItemType.USER;

        final NodeOperation op = Ops.addNode(CHILD_NAME, itemType);
        op.exec(rootNode, eh);
        assertTrue(rootNode.hasNode(CHILD_NAME));
        assertEquals(itemType.getSystemName(), rootNode.getNode(CHILD_NAME).getPrimaryNodeType().getName());
    }

    @Test
    public void testAddNodeWithTwoStrings() throws RepositoryException {
        final String itemTypeName = ItemType.FOLDER.getSystemName();

        final NodeOperation op = Ops.addNode(CHILD_NAME, itemTypeName);
        op.exec(rootNode, eh);
        assertTrue(rootNode.hasNode(CHILD_NAME));
        assertEquals(itemTypeName, rootNode.getNode(CHILD_NAME).getPrimaryNodeType().getName());
    }

    @Test
    public void testAddProperty() throws Exception {
        final ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.createValue(PROPERTY_VALUE)).thenReturn(new MockValue(PROPERTY_VALUE));
        session.setValueFactory(valueFactory);

        final NodeOperation op = Ops.addProperty(PROPERTY_NAME, PROPERTY_VALUE);
        op.exec(rootNode, eh);

        assertEquals(PROPERTY_VALUE, rootNode.getProperty(PROPERTY_NAME).getString());
    }

    @Test(expected = RuntimeException.class)
    public void testAddPropertyFailsIfPropertyExists() throws Exception {
        rootNode.setProperty(PROPERTY_NAME, PROPERTY_VALUE);
        final NodeOperation op = Ops.addProperty(PROPERTY_NAME, "otherValue");
        op.exec(rootNode, eh);
        fail("should have failed");
    }

    @Test
    public void testSetProperty() throws Exception {
        rootNode.setProperty(PROPERTY_NAME, PROPERTY_VALUE);

        final String newPropertyValue = "zazoo";

        final ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.createValue(newPropertyValue)).thenReturn(new MockValue(newPropertyValue));
        session.setValueFactory(valueFactory);

        final NodeOperation op = Ops.setProperty(PROPERTY_NAME, newPropertyValue);
        op.exec(rootNode, eh);

        assertEquals(newPropertyValue, rootNode.getProperty(PROPERTY_NAME).getString());
    }

    @Test(expected = RuntimeException.class)
    public void testSetPropertyFailsIfItsNotExistingAlready() throws Exception {
        final NodeOperation op = Ops.setProperty(PROPERTY_NAME, PROPERTY_VALUE);
        op.exec(rootNode, eh);
        fail("should have failed");
    }
}
