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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.jcr.nodebuilder.ErrorHandler;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.nodebuilder.Ops;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.MockValue;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;

import org.junit.Test;

/**
 * @version $Id$
 */
public class OpsTest {
    private final ErrorHandler eh = new ErrorHandler() {
        @Override
        public void report(String message) {
            throw new RuntimeException(message);
        }

        @Override
        public void handle(RepositoryException e, Node context) {
            throw new RuntimeException(e.getMessage());
        }
    };

    @Test
    public void testAddProperty() throws Exception {
        final MockNode node = new MockNode("hello");
        String propertyName = "foo";
        String propertyValue = "zazoo";

        final MockSession session = new MockSession("testSession");
        node.setSession(session);

        final ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.createValue(propertyValue)).thenReturn(new MockValue(propertyValue));
        session.setValueFactory(valueFactory);

        final NodeOperation op = Ops.addProperty(propertyName, propertyValue);
        op.exec(node, eh);

        assertEquals(propertyValue, node.getProperty(propertyName).getString());
    }

    @Test(expected = RuntimeException.class)
    public void testAddPropertyFailsIfPropertyExists() throws Exception {
        final Node node = new MockNode("hello");
        node.setProperty("foo", "bar");
        final NodeOperation op = Ops.addProperty("foo", "zazoo");
        op.exec(node, eh);
        fail("should have failed");
    }

    @Test
    public void testSetProperty() throws Exception {
        final MockNode node = new MockNode("hello");
        final String propertyName = "foo";
        final String oldPropertyValue = "old";
        node.setProperty(propertyName, oldPropertyValue);

        final String propertyValue = "zazoo";

        final MockSession session = new MockSession("testSession");
        node.setSession(session);

        final ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.createValue(propertyValue)).thenReturn(new MockValue(propertyValue));
        session.setValueFactory(valueFactory);

        final NodeOperation op = Ops.setProperty(propertyName, propertyValue);
        op.exec(node, eh);

        assertEquals(propertyValue, node.getProperty(propertyName).getString());
    }

    @Test(expected = RuntimeException.class)
    public void testSetPropertyFailsIfItsNotExistingAlready() throws Exception {
        final MockNode root = new MockNode("root");

        final NodeOperation op = Ops.setProperty("foo", "bar");
        op.exec(root, eh);
        fail("should have failed");
    }

}
