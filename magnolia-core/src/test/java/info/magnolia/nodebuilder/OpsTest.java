/**
 * This file Copyright (c) 2009-2010 Magnolia International
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
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class OpsTest extends RepositoryTestCase {
    private final ErrorHandler eh = new ErrorHandler() {
        public void report(String message) {
            throw new RuntimeException(message); // TODO
        }

        public void handle(RepositoryException e, Content context) {
            throw new RuntimeException(e.getMessage());
        }
    };

    public void testAddPropertyFailsIfPropertyExists() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        final Content node = hm.getRoot().createContent("hello");
        node.createNodeData("foo", "bar");
        hm.save();
        try {
            final NodeOperation op = Ops.addProperty("foo", "zazoo");
            op.exec(node, eh);
            fail("should have failed");
        } catch (Throwable e) { // (ItemExistsException e)
            assertEquals("foo", e.getMessage());
        }
    }

    public void testSetPropertyFailsIfPropertyDoesNotExist() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        final Content node = hm.getRoot().createContent("hello");
        node.createNodeData("foo", "bar");
        hm.save();
        try {
            final NodeOperation op = Ops.setProperty("foo", "baz", "zazoo");
            op.exec(node, eh);
            fail("should have failed");
        } catch (Throwable e) { // (RepositoryException e)
            assertEquals("Expected baz at /hello/foo but found bar instead; can't set value to zazoo.", e.getMessage());
        }
    }

    public void testSetPropertyFailsIfPropertyDoesNotHaveExpectedValue() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        final Content node = hm.getRoot().createContent("hello");
        hm.save();
        try {
            final NodeOperation op = Ops.setProperty("foo", "zazoo");
            op.exec(node, eh);
            fail("should have failed");
        } catch (Throwable e) { // (ItemNotFoundException e)
            assertEquals("foo", e.getMessage());
        }
    }

    public void testRemoveFailsOnUnexistingPropertyOrNode() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        hm.getRoot().createContent("hello").createNodeData("foo", "bar");
        hm.save();

        final Content root = hm.getContent("/hello");
        try {
            final NodeOperation op = Ops.remove("non-existing");
            op.exec(root, eh);
            fail("should have failed");
        } catch (Throwable e) { // (PathNotFoundException e)
            assertEquals("non-existing", e.getMessage());
        }
    }

    public void testRenamePropertyAndCheckValueForString() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        hm.getRoot().createContent("hello").setNodeData("fooOld", "bar");
        hm.save();

        final Content root = hm.getContent("/hello");
        final NodeOperation op = Ops.renameProperty("fooOld", "fooNew");
        op.exec(root, eh);
        assertFalse(root.hasNodeData("fooOld"));
        assertNotNull(root.getNodeData("fooNew"));
        assertEquals("bar", root.getNodeData("fooNew").getString());
    }

}
