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
import info.magnolia.test.RepositoryTestCase;

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

        final NodeBuilder nodeBuilder = new NodeBuilder(hm.getContent("MyRoot"),
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
        nodeBuilder.exec();
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
