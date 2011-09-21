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
package info.magnolia.cms.util;

import static info.magnolia.nodebuilder.Ops.addNode;
import static java.util.Arrays.asList;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultContent;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.nodebuilder.NodeBuilder;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ContentUtil} which do rely on an actual repository, i.e not using {@link info.magnolia.test.RepositoryTestCase}.
 *
 * @version $Id$
 */
public class ContentUtilTest extends MgnlTestCase {

    private MockContent rootABC;

    @Override
    @Before
    public void setUp() {
        rootABC = new MockContent("root", ItemType.CONTENT);
        new NodeBuilder(rootABC,
                addNode("a", ItemType.CONTENT).then(
                        addNode("aa", ItemType.CONTENT),
                        addNode("ab", ItemType.CONTENTNODE).then(
                                addNode("abb", ItemType.CONTENTNODE)
                        )
                ),
                addNode("b", ItemType.CONTENT),
                addNode("c", ItemType.CONTENTNODE)
        ).exec();
    }

    @Test
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

    @Test
    public void testDeleteAndRemoveParentsIfAnOtherChild() throws IOException, RepositoryException{
        String content = "/node1/child1\n" +
            "/node1/child2\n";

        HierarchyManager hm = MockUtil.createHierarchyManager(content);

        Content child1 = hm.getContent("/node1/child1");
        ContentUtil.deleteAndRemoveEmptyParents(child1);
        assertTrue("node1 should not be deleted because it has children", hm.isExist("/node1"));

    }

    @Test
    public void testDeleteAndRemoveParentsIfNoOtherChild() throws IOException, RepositoryException{
        String content = "/node1/child1";
        HierarchyManager hm = MockUtil.createHierarchyManager(content);

        Content child1 = hm.getContent("/node1/child1");
        ContentUtil.deleteAndRemoveEmptyParents(child1);
        assertTrue("node1 must be deleted because it has no children", !hm.isExist("/node1"));
    }

    @Test
    public void testDeleteAndRemoveParentsWithLevel() throws IOException, RepositoryException{
        String content = "/node1/child1/subchild1";

        HierarchyManager hm = MockUtil.createHierarchyManager(content);

        Content subchild1 = hm.getContent("/node1/child1/subchild1");
        ContentUtil.deleteAndRemoveEmptyParents(subchild1,1);

        assertTrue("child1 must be deleted because it has no children", !hm.isExist("/node1/child1"));
        assertTrue("node1 must existe because the level was defined", hm.isExist("/node1"));

    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(Arrays.asList(new String[]{"b", "a","c"}), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("b", "c","a"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","d","e","f","c"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a", "c","b"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","f","b","c","d","e"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","e","b","c","d","f"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","c","f","d","e"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","e","c","d","f"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("c","a","b","d","e","f"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("b","a","c","d","e","f"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","c","d","e","f"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("f","a","b","c","d","e"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("b","c","d","e","a","f"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","d","e","c","f"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","c","d","e","f"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","c","d","b","e","f"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("b","c","d","a","e","f"), result);
    }

    @Test
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
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(asList("a","b","c","d","f","e"), result);
    }

    @Test
    public void testGetAncestorOfTypeBasicCase() throws RepositoryException {
        final Content content = rootABC.getContent("a/ab/abb");
        assertEquals(rootABC.getContent("a"), ContentUtil.getAncestorOfType(content, ItemType.CONTENT.getSystemName()));
    }

    @Test
    public void testGetAncestorOfTypeReturnsSelfIfMatch() throws RepositoryException {
        final Content abb = rootABC.getContent("a/ab/abb");
        final Content ab = rootABC.getContent("a/ab");
        final Content aa = rootABC.getContent("a/aa");
        final Content c = rootABC.getContent("c");
        assertEquals(abb, ContentUtil.getAncestorOfType(abb, ItemType.CONTENTNODE.getSystemName()));
        assertEquals(ab, ContentUtil.getAncestorOfType(ab, ItemType.CONTENTNODE.getSystemName()));
        assertEquals(aa, ContentUtil.getAncestorOfType(aa, ItemType.CONTENT.getSystemName()));
        assertEquals(c, ContentUtil.getAncestorOfType(c, ItemType.CONTENTNODE.getSystemName()));
    }

    @Test
    public void testGetAncestorOfTypeThrowsExceptionIfNotFound() throws RepositoryException {
        final Content content = rootABC.getContent("a/aa");
        try {
            ContentUtil.getAncestorOfType(content, ItemType.CONTENTNODE.getSystemName());
            fail("should have failed");
        } catch (Throwable t) {
            assertTrue(t instanceof RepositoryException);
            assertEquals("No ancestor of type mgnl:contentNode found for null:/root/a/aa[mgnl:content]", t.getMessage());
        }
    }

    @Test
    public void testAsContent() throws Exception{
        // GIVEN
        MockSession session = SessionTestUtil.createSession("testWorkspace","/foo/bar/sub1.@uuid=1");

        Node node = session.getNode("/foo/bar/sub1");

        // WHEN
        Content content = ContentUtil.asContent(node);

        // THEN
        assertEquals(node, content.getJCRNode());
        assertTrue(content instanceof DefaultContent);
    }

    private final static class ContentTypeRejector implements Content.ContentFilter {
        private final List<String> rejectedTypes;

        public ContentTypeRejector(String... rejectedTypes) {
            this.rejectedTypes = Arrays.asList(rejectedTypes);
        }

        @Override
        public boolean accept(Content content) {
            try {
                return !rejectedTypes.contains(content.getNodeTypeName());
            } catch (RepositoryException e) {
                throw new RuntimeException(e); // TODO
            }
        }
    }
}
