/**
 * This file Copyright (c) 2009 Magnolia International
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
package info.magnolia.nodebuilder;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;
import static info.magnolia.nodebuilder.Ops.*;
import info.magnolia.test.RepositoryTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class NodeBuilderTest extends RepositoryTestCase {
    public void testContextNodeIsProperlyPropagated() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        {
            final Content hello = hm.getRoot().createContent("MyRoot").createContent("hello");
            final Content w = hello.createContent("world");
            w.createNodeData("foo", "bar");
            hello.createContent("zing").createNodeData("this", "will be removed");
        }

        final MessageTracker messageTracker = new MessageTracker();
        final NodeBuilder nodeBuilder = new NodeBuilder(messageTracker, hm.getContent("MyRoot"),
                getNode("hello").then(
                        addNode("newsub").then(
                                addProperty("newProp", "New Value")
                        ),
                        remove("zing"),
                        getNode("world").then(
                                remove("foo")
                        )
                ),
                addNode("other").then(addProperty("X", "Y")),
                addProperty("lala", "lolo")

        );
        nodeBuilder.exec();
        hm.save();

        final NodeData prop = hm.getNodeData("/MyRoot/hello/newsub/newProp");
        assertTrue("Property should have been created", prop.isExist());
        assertEquals("New Value", prop.getString());
        assertEquals("Y", hm.getNodeData("/MyRoot/other/X").getString());
        assertEquals("lolo", hm.getNodeData("/MyRoot/lala").getString());
        assertFalse("Node should have been removed", hm.isExist("/MyRoot/hello/zing"));
        assertFalse("Property should have been removed", hm.isExist("/MyRoot/hello/world/foo"));
        assertEquals(0, messageTracker.getMessages().size());
    }

    public void testErrorMessages() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        {
            final Content hello = hm.getRoot().createContent("MyRoot").createContent("hello");
            final Content world = hello.createContent("world");
            world.createNodeData("foo", "bar");
            world.createNodeData("baz", "baz-current");
        }

        final MessageTracker messageTracker = new MessageTracker();
        final NodeBuilder nodeBuilder = new NodeBuilder(messageTracker, hm.getContent("MyRoot"),
                getNode("hello").then(
                        setProperty("unexisting1", "won't be set"),
                        setProperty("unexisting2", "does not exist and", "won't be set"),
                        remove("unexisting3"),
                        getNode("world").then(
                                addProperty("baz", "baz"),
                                setProperty("baz", "expected", "new"),
                                getNode("subsub"),
                                getNode("chalala/zeuzeu")
                        )
                ));
        nodeBuilder.exec();
        hm.save();

        assertEquals(7, messageTracker.getMessages().size());
        assertEquals("unexisting1 can't be found at /MyRoot/hello.", messageTracker.getMessages().get(0));
        assertEquals("unexisting2 can't be found at /MyRoot/hello.", messageTracker.getMessages().get(1));
        assertEquals("unexisting3 can't be found at /MyRoot/hello.", messageTracker.getMessages().get(2));
        assertEquals("baz already exists at /MyRoot/hello/world.", messageTracker.getMessages().get(3));
        assertEquals("Expected expected at /MyRoot/hello/world/baz but found baz-current instead; can't set value to new.", messageTracker.getMessages().get(4));
        assertEquals("subsub can't be found at /MyRoot/hello/world.", messageTracker.getMessages().get(5));
        assertEquals("chalala/zeuzeu can't be found at /MyRoot/hello/world.", messageTracker.getMessages().get(6));
    }

    public void testPropertyNotReplaceIfCurrentValueDoesNotMatchExpectations() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        {
            final Content hello = hm.getRoot().createContent("MyRoot").createContent("hello");
            hello.createNodeData("foo", "foo-current");
            hello.createNodeData("baz", "baz-current");
        }

        final MessageTracker messageTracker = new MessageTracker();
        final NodeBuilder nodeBuilder = new NodeBuilder(messageTracker, hm.getContent("MyRoot"),
                getNode("hello").then(
                        setProperty("foo", "foo-current", "foo-replaced"),
                        setProperty("baz", "baz-unexpected", "baz-not-replaced")
                ));
        nodeBuilder.exec();
        hm.save();

        assertEquals("foo-replaced", hm.getNodeData("/MyRoot/hello/foo").getString());
        assertEquals(1, messageTracker.getMessages().size());
        assertEquals("Expected baz-unexpected at /MyRoot/hello/baz but found baz-current instead; can't set value to baz-not-replaced.", messageTracker.getMessages().get(0));
        assertEquals("baz-current", hm.getNodeData("/MyRoot/hello/baz").getString());
    }

    /**
     * An error handler which keeps the error messages in a List but otherwise ignores
     * them, except unhandled RepositoryExceptions, which are rethrown.
     */
    private static class MessageTracker extends AbstractErrorHandler {
        private final List<String> messages = new ArrayList<String>();

        public List<String> getMessages() {
            return messages;
        }

        public void report(String message) {
            messages.add(message);
        }
    }
}
