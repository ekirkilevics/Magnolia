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
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class OpsTest extends RepositoryTestCase {
    public void testAddPropertyFailsIfPropertyExists() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        final Content node = hm.getRoot().createContent("hello");
        node.createNodeData("foo", "bar");
        hm.save();
        try {
            final NodeOperation op = Ops.addProperty("foo", "zazoo");
            op.exec(node);
            fail("should have failed");
        } catch (ItemExistsException e) {
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
            op.exec(node);
            fail("should have failed");
        } catch (RepositoryException e) {
            assertEquals("Expected baz and found bar instead.", e.getMessage());
        }
    }
    public void testSetPropertyFailsIfPropertyDoesNotHaveExpectedValue() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        final Content node = hm.getRoot().createContent("hello");
        hm.save();
        try {
            final NodeOperation op = Ops.setProperty("foo", "zazoo");
            op.exec(node);
            fail("should have failed");
        } catch (ItemNotFoundException e) {
            assertEquals("foo", e.getMessage());
        }
    }

    public void testRemoveFailsOnUnexistingPropertyOrNode() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        hm.getRoot().createContent("hello").createNodeData("foo", "bar");
        hm.save();

        try {
            Ops.remove("non-existing").exec(hm.getContent("/hello"));
            fail("should have failed");
        } catch (PathNotFoundException e) {
            assertEquals("non-existing", e.getMessage());
        }
    }
}
