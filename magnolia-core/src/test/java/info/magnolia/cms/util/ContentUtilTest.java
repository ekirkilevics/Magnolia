/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockUtil;
import static org.easymock.EasyMock.*;

import javax.jcr.RepositoryException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ContentUtilTest extends RepositoryTestCase {

    public void testVisitShouldPassFilterAlong() throws Exception {
        final ItemType foo = new ItemType("foo");
        final ItemType bar = new ItemType("bar");
        final MockContent root = new MockContent("root", foo);
        final MockContent child1 = new MockContent("child1", foo);
        root.addContent(child1);
        final MockContent child2 = new MockContent("child2", bar);
        root.addContent(child2);
        final MockContent child2child = new MockContent("child2-child", foo);
        child2.addContent(child2child);
        final MockContent child3 = new MockContent("child3", foo);
        root.addContent(child3);
        final MockContent child3childFoo = new MockContent("child3-childFoo", foo);
        child3.addContent(child3childFoo);
        final MockContent child3childFoo2 = new MockContent("child3-childFoo2", foo);
        child3.addContent(child3childFoo2);
        final MockContent child3childFooChild = new MockContent("child3-childFoo-child", foo);
        child3childFoo.addContent(child3childFooChild);
        final MockContent child3childBar = new MockContent("child3-childBar", bar);
        child3.addContent(child3childBar);
        final MockContent child4 = new MockContent("child4", bar);
        root.addContent(child4);
        final MockContent child5 = new MockContent("child5", foo);
        root.addContent(child5);
        final ContentTypeRejector filter = new ContentTypeRejector("bar");
        final ContentUtil.Visitor visitor = createStrictMock(ContentUtil.Visitor.class);

        visitor.visit(root);
        visitor.visit(child1);
        visitor.visit(child3);
        visitor.visit(child3childFoo);
        visitor.visit(child3childFooChild);
        visitor.visit(child3childFoo2);
        visitor.visit(child5);

        replay(visitor);
        ContentUtil.visit(root, visitor, filter);
        verify(visitor);
    }

    public void testDeleteAndRemoveParentsIfAnOtherChild() throws IOException, RepositoryException{
        String content = "/node1/child1\n" +
            "/node1/child2\n";

        HierarchyManager hm = MockUtil.createHierarchyManager(content);

        Content child1 = hm.getContent("/node1/child1");
        ContentUtil.deleteAndRemoveEmptyParents(child1);
        assertTrue("node1 should not be deleted because it has children", hm.isExist("/node1"));

    }

    public void testDeleteAndRemoveParentsIfNoOtherChild() throws IOException, RepositoryException{
        String content = "/node1/child1";
        HierarchyManager hm = MockUtil.createHierarchyManager(content);

        Content child1 = hm.getContent("/node1/child1");
        ContentUtil.deleteAndRemoveEmptyParents(child1);
        assertTrue("node1 must be deleted because it has no children", !hm.isExist("/node1"));
    }

    public void testDeleteAndRemoveParentsWithLevel() throws IOException, RepositoryException{
        String content = "/node1/child1/subchild1";

        HierarchyManager hm = MockUtil.createHierarchyManager(content);

        Content subchild1 = hm.getContent("/node1/child1/subchild1");
        ContentUtil.deleteAndRemoveEmptyParents(subchild1,1);

        assertTrue("child1 must be deleted because it has no children", !hm.isExist("/node1/child1"));
        assertTrue("node1 must existe because the level was defined", hm.isExist("/node1"));

    }

    public void testSessionBasedCopy() throws RepositoryException{
        HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        Content src = hm.getRoot().createContent("test");
        src.createContent("subnode");
        ContentUtil.copyInSession(src, "/gugu");
        assertTrue(hm.isExist("/gugu"));
        assertTrue(hm.isExist("/gugu/subnode"));
    }

    private final static class ContentTypeRejector implements Content.ContentFilter {
        private final List<String> rejectedTypes;

        public ContentTypeRejector(String... rejectedTypes) {
            this.rejectedTypes = Arrays.asList(rejectedTypes);
        }

        public boolean accept(Content content) {
            try {
                return !rejectedTypes.contains(content.getNodeTypeName());
            } catch (RepositoryException e) {
                throw new RuntimeException(e); // TODO
            }
        }
    }





}
