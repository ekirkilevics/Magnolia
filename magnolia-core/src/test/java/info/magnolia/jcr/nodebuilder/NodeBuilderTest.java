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

import static info.magnolia.jcr.nodebuilder.Ops.addNode;
import static info.magnolia.jcr.nodebuilder.Ops.addProperty;
import static info.magnolia.jcr.nodebuilder.Ops.getNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
public class NodeBuilderTest {

    private static final String CHILD_NAME = "child";
    private static final String PROPERTY_NAME = "property1";
    private static final String PROPERTY_VALUE = "propertValue";
    private MockNode rootNode;
    private MockSession session;
    private MockValue propertyValue;

    @Before
    public void setUp() {
        rootNode = new MockNode("root");
        propertyValue = new MockValue(PROPERTY_VALUE);
        session = new MockSession("testSession");
        rootNode.setSession(session);


        final ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.createValue(PROPERTY_VALUE)).thenReturn(propertyValue);
        session.setValueFactory(valueFactory);
    }

    @Test
    public void testExecWithSeveralChildOps() throws Exception {
        final NodeOperation addNodeOp = Ops.addNode(CHILD_NAME);
        final NodeOperation addPropertyOp = Ops.addProperty(PROPERTY_NAME, PROPERTY_VALUE);

        NodeBuilder builder = new NodeBuilder(rootNode, addNodeOp, addPropertyOp);
        builder.exec();

        assertTrue("AddNode Operation failed!", rootNode.hasNode(CHILD_NAME));
        assertEquals("AddProperty Operation failed!", propertyValue, rootNode.getProperty(PROPERTY_NAME).getValue());
    }

    /**
     * This test is rather a sample how to properly use NodeBuilder-API than a simple unit-test.
     */
    @Test
    public void testRealisticUsageScenario() throws RepositoryException {
        final MockNode childNode = (MockNode) rootNode.addNode(CHILD_NAME);
        final String childOfChildName = "childOfChild";

        NodeBuilder builder =
                new NodeBuilder(rootNode, getNode(CHILD_NAME).then(
                        addNode(childOfChildName).then(addProperty(PROPERTY_NAME, PROPERTY_VALUE))));
        builder.exec();

        assertTrue(childNode.hasNode(childOfChildName));
        assertEquals("AddProperty Operation failed!", propertyValue, childNode.getNode(childOfChildName).getProperty(PROPERTY_NAME).getValue());
    }
}
