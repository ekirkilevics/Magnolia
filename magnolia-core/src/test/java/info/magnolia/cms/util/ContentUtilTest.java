/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import static org.easymock.EasyMock.*;
import static java.util.Arrays.asList;

import javax.jcr.RepositoryException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
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

    public void testOrderAfter() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n");
        Content node = hm.getContent("/node");
        Content a = node.getContent("a");
        ContentUtil.orderAfter(a, "b");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(Arrays.asList(new String[]{"b", "a","c"}), result);
    }

    public void testOrderAfterLastNode() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n");
        Content node = hm.getContent("/node");
        Content a = node.getContent("a");
        ContentUtil.orderAfter(a, "c");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("b", "c","a"), result);
    }
    
    public void testOrderAfterLastNodeVariation1() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content a = node.getContent("c");
        ContentUtil.orderAfter(a, "f");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","d","e","f","c"), result);
    }

    public void testOrderAfterFirstNodeOnlyThree() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("c");
        ContentUtil.orderAfter(c, "a");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a", "c","b"), result);
    }

    public void testOrderAfterFirstNodeMoreThanThreeVariation1() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("f");
        ContentUtil.orderAfter(c, "a");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","f","b","c","d","e"), result);
    }

    public void testOrderAfterFirstNodeMoreThanThreeVariation2() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("e");
        ContentUtil.orderAfter(c, "a");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","e","b","c","d","f"), result);
    }

    public void testOrderAfterMidNodeMoreThanThreeVariation1() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("f");
        ContentUtil.orderAfter(c, "c");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","c","f","d","e"), result);
    }

    public void testOrderAfterMidNodeMoreThanThreeVariation2() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("e");
        ContentUtil.orderAfter(c, "b");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","e","c","d","f"), result);
    }
    
    public void testOrderBeforeFirstNodeVariation1() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("c");
        ContentUtil.orderBefore(c, "a");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("c","a","b","d","e","f"), result);
    }

    public void testOrderBeforeFirstNodeVariation2() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("b");
        ContentUtil.orderBefore(c, "a");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("b","a","c","d","e","f"), result);
    }

    public void testOrderBeforeFirstNodeVariation3() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("a");
        ContentUtil.orderBefore(c, "b");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","c","d","e","f"), result);
    }

    public void testOrderBeforeFirstNodeVariation4() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("f");
        ContentUtil.orderBefore(c, "a");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("f","a","b","c","d","e"), result);
    }

    public void testOrderBeforeLastNodeVariation1() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("a");
        ContentUtil.orderBefore(c, "f");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("b","c","d","e","a","f"), result);
    }

    public void testOrderBeforeLastNodeVariation2() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("c");
        ContentUtil.orderBefore(c, "f");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","d","e","c","f"), result);
    }

    public void testOrderBeforeLastNodeVariation3() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("e");
        ContentUtil.orderBefore(c, "f");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","c","d","e","f"), result);
    }

    public void testOrderBeforeMidNodeVariation1() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("b");
        ContentUtil.orderBefore(c, "e");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","c","d","b","e","f"), result);
    }

    public void testOrderBeforeMidNodeVariation2() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("a");
        ContentUtil.orderBefore(c, "e");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("b","c","d","a","e","f"), result);
    }

    public void testOrderBeforeMidNodeVariation3() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
            "/node/b\n" +
            "/node/c\n" +
            "/node/d\n" +
            "/node/e\n" +
            "/node/f\n");
        Content node = hm.getContent("/node");
        Content c = node.getContent("f");
        ContentUtil.orderBefore(c, "e");
        Collection<Content> result = node.getChildren();
        CollectionUtils.transform(result, new Transformer() {
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","c","d","f","e"), result);
    }

    public void testChangeNodeTypeReplaceFirstOccurrenceOnly() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        final Content src = hm.getRoot().createContent("test");
        src.createContent("foo");
        src.createContent("bar");
        final String oldUUID = src.getUUID();

        assertEquals("wrong initial type", ItemType.CONTENT.getSystemName() , src.getNodeTypeName());

        ContentUtil.changeNodeType(src, ItemType.CONTENTNODE, false);

        assertTrue(hm.isExist("/test"));
        assertEquals(oldUUID, hm.getContent("/test").getUUID());
        assertEquals(ItemType.CONTENTNODE.getSystemName(), hm.getContent("/test").getNodeTypeName());
        assertEquals(ItemType.CONTENT.getSystemName(), hm.getContent("/test/bar").getNodeTypeName());
        assertEquals(ItemType.CONTENT.getSystemName(), hm.getContent("/test/foo").getNodeTypeName());
    }

    public void testChangeNodeTypeReplaceAllOccurrences() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        final Content src = hm.getRoot().createContent("test");
        src.createContent("foo");
        src.createContent("bar");
        final String oldUUID = src.getUUID();
        assertEquals("wrong initial type", ItemType.CONTENT.getSystemName(), src.getNodeTypeName());
        assertEquals("wrong initial type", ItemType.CONTENT.getSystemName(), hm.getContent("/test/bar").getNodeTypeName());
        assertEquals("wrong initial type", ItemType.CONTENT.getSystemName(), hm.getContent("/test/foo").getNodeTypeName());

        ContentUtil.changeNodeType(src, ItemType.CONTENTNODE, true);

        assertTrue(hm.isExist("/test"));
        assertEquals(oldUUID, hm.getContent("/test").getUUID());
        assertEquals(ItemType.CONTENTNODE.getSystemName(), hm.getContent("/test").getNodeTypeName());
        assertEquals(ItemType.CONTENTNODE.getSystemName(), hm.getContent("/test/foo").getNodeTypeName());
        assertEquals(ItemType.CONTENTNODE.getSystemName(), hm.getContent("/test/bar").getNodeTypeName());
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
