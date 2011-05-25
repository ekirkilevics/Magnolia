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


import static info.magnolia.jcr.util.Ops.addNode;
import static info.magnolia.jcr.util.Ops.addProperty;
import static info.magnolia.jcr.util.Ops.getNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.MockValue;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.ValueFactory;

import org.junit.Test;

/**
 * @version $Id$
 */
public class NodeBuilderTest {

    @Test
    public void testHasMixin() throws Exception {
        final String rootNodeName = "root";
        final String firstSubName = "firstSub";
        final String secondSubName = "newSub";
        final String propertyName = "newProp";
        final String propertyValue= "newValue";
        final MockNode node = new MockNode(rootNodeName);
        final MockSession session = new MockSession("testSession");
        node.setSession(session);
        final MockNode firstSub = (MockNode) node.addNode(firstSubName);

        final ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.createValue(propertyValue)).thenReturn(new MockValue(propertyValue));
        session.setValueFactory(valueFactory);

        NodeBuilder builder =
                new NodeBuilder(node, getNode(firstSubName).then(
                        addNode(secondSubName).then(addProperty(propertyName, propertyValue))));

        builder.exec();

        assertTrue(firstSub.hasNode(secondSubName));
        Node subNode = firstSub.getNode(secondSubName);
        Property prop = subNode.getProperty(propertyName);
        assertEquals(propertyValue, prop.getString());

    }
}
