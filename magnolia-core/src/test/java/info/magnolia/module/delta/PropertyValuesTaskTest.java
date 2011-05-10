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
package info.magnolia.module.delta;

import info.magnolia.cms.core.NodeData;
import info.magnolia.module.InstallContext;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockNodeData;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PropertyValuesTaskTest extends TestCase {
    private InstallContext ctx;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ctx = createStrictMock(InstallContext.class);
    }

    public void testExistingPropertyIsReplaced() throws RepositoryException {
        final MockContent node = new MockContent("foo");
        node.addNodeData(new MockNodeData("bar", "old-value"));

        final PropertyValuesTask pvd = new DummyPropertyValuesDelta();

        replay(ctx);
        pvd.checkAndModifyPropertyValue(ctx, node, "bar", "old-value", "newValue");
        verify(ctx);

        final NodeData nodeData = node.getNodeData("bar");
        assertEquals("newValue", nodeData.getString());
    }

    public void testNonExistingPropertyIsNotReplacedButLogged() throws RepositoryException {
        ctx.warn("Property \"bar\" was expected to exist at /foo with value \"old-value\" but does not exist.");

        final MockContent node = new MockContent("foo");

        final PropertyValuesTask pvd = new DummyPropertyValuesDelta();

        replay(ctx);
        pvd.checkAndModifyPropertyValue(ctx, node, "bar", "old-value", "newValue");
        verify(ctx);

        final NodeData nodeData = node.getNodeData("bar");
        assertEquals(false, nodeData.isExist());
    }

    public void testPropertywithUnexpectedValueIsNotReplacedButLogged() throws RepositoryException {
        ctx.warn("Property \"bar\" was expected to exist at /foo with value \"old-value\" but has the value \"wrong-value\" instead.");

        final MockContent node = new MockContent("foo");
        node.addNodeData(new MockNodeData("bar", "wrong-value"));

        final PropertyValuesTask pvd = new DummyPropertyValuesDelta();

        replay(ctx);
        pvd.checkAndModifyPropertyValue(ctx, node, "bar", "old-value", "newValue");
        verify(ctx);

        final NodeData nodeData = node.getNodeData("bar");
        assertEquals("wrong-value", nodeData.getString());
    }

    public void testNonExistingPropertyAndExpectedAsSuchIsCreated() throws RepositoryException {
        final MockContent node = new MockContent("foo");

        final PropertyValuesTask pvd = new DummyPropertyValuesDelta();

        replay(ctx);
        pvd.newProperty(ctx, node, "bar", "newValue");
        verify(ctx);

        final NodeData nodeData = node.getNodeData("bar");
        assertEquals("newValue", nodeData.getString());
    }

    public void testUnexpectedlyExistingPropertyIsNotReplacedAndLogged() throws RepositoryException {
        ctx.warn("Property \"bar\" was expected not to exist at /foo, but exists with value \"old-value\" and was going to be created with value \"newValue\".");

        final MockContent node = new MockContent("foo");
        node.addNodeData(new MockNodeData("bar", "old-value"));

        final PropertyValuesTask pvd = new DummyPropertyValuesDelta();

        replay(ctx);
        pvd.newProperty(ctx, node, "bar", "newValue");
        verify(ctx);
    }

    private static class DummyPropertyValuesDelta extends PropertyValuesTask {
        public DummyPropertyValuesDelta() {
            super(null, null);
        }

        @Override
        public void execute(InstallContext installContext) throws TaskExecutionException {
        }
    }
}
