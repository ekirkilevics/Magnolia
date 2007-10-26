/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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

        public void execute(InstallContext installContext) throws TaskExecutionException {
        }
    }
}
