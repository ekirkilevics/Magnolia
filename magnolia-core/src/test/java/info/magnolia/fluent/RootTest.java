/**
 * This file Copyright (c) 2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.fluent;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RootTest extends RepositoryTestCase {
    public void testContextNodeIsProperlyPropagated() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        {
            final Content hello = hm.getRoot().createContent("MyRoot").createContent("hello");
            final Content w = hello.createContent("world");
            w.createNodeData("foo", "bar");
            hello.createContent("zing").createNodeData("this", "will be removed");
        }

        final Root root = new Root(hm.getContent("MyRoot"),
                Ops.getNode("hello").then(
                        Ops.add("newsub").then(
                                Ops.addProperty("newProp", "New Value")
                        ),
                        Ops.remove("zing"),
                        Ops.getNode("world").then(
                                Ops.remove("foo")
                        )
                ),
                Ops.add("other").then(Ops.addProperty("X", "Y")),
                Ops.addProperty("lala", "lolo")

        );
        root.exec();
        hm.save();

        final NodeData prop = hm.getNodeData("/MyRoot/hello/newsub/newProp");
        assertTrue("Property should have been created", prop.isExist());
        assertEquals("New Value", prop.getString());
        assertEquals("Y", hm.getNodeData("/MyRoot/other/X").getString());
        assertEquals("lolo", hm.getNodeData("/MyRoot/lala").getString());
        assertFalse("Node should have been removed", hm.isExist("/MyRoot/hello/zing"));
        assertFalse("Property should have been removed", hm.isExist("/MyRoot/hello/world/foo"));
    }
}
