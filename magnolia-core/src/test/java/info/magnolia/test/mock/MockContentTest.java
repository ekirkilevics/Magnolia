/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.test.mock;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import junit.framework.TestCase;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MockContentTest extends TestCase {

    /**
     * This is the mock-equivalent test of {@link info.magnolia.cms.core.DefaultContentTest#testNameFilteringWorksForBothBinaryAndNonBinaryProperties()}.
     */
    public void testNameFilteringWorksForBothBinaryAndNonBinaryProperties() throws Exception {
        String contentProperties = StringUtils.join(Arrays.asList(
                "/somepage/mypage@type=mgnl:content",
                "/somepage/mypage/paragraphs@type=mgnl:contentNode",
                "/somepage/mypage/paragraphs/0@type=mgnl:contentNode",
                "/somepage/mypage/paragraphs/0@type=mgnl:contentNode",

                // 2 regular props
                "/somepage/mypage/paragraphs/0/attention=booyah",
                "/somepage/mypage/paragraphs/0/imaginary=date:2009-10-14T08:59:01.227-04:00",

                // 3 binaries
                "/somepage/mypage/paragraphs/0/attachment1@type=mgnl:resource",
                "/somepage/mypage/paragraphs/0/attachment1.fileName=hello",
                "/somepage/mypage/paragraphs/0/attachment1.extension=gif",
                "/somepage/mypage/paragraphs/0/attachment1.jcr\\:data=X",
                "/somepage/mypage/paragraphs/0/attachment1.jcr\\:mimeType=image/gif",
                "/somepage/mypage/paragraphs/0/attachment1.jcr\\:lastModified=",

                "/somepage/mypage/paragraphs/0/attachment2@type=mgnl:resource",
                "/somepage/mypage/paragraphs/0/attachment2.fileName=test",
                "/somepage/mypage/paragraphs/0/attachment2.extension=jpeg",
                "/somepage/mypage/paragraphs/0/attachment2.jcr\\:data=X",
                "/somepage/mypage/paragraphs/0/attachment2.jcr\\:mimeType=image/jpeg",
                "/somepage/mypage/paragraphs/0/attachment2.jcr\\:lastModified=",

                "/somepage/mypage/paragraphs/0/image3@type=mgnl:resource",
                "/somepage/mypage/paragraphs/0/image3.fileName=third",
                "/somepage/mypage/paragraphs/0/image3.extension=png",
                "/somepage/mypage/paragraphs/0/image3.jcr\\:data=X",
                "/somepage/mypage/paragraphs/0/image3.jcr\\:mimeType=image/png",
                "/somepage/mypage/paragraphs/0/image3.jcr\\:lastModified=",

                // and more which should not match
                "/somepage/mypage/paragraphs/0/foo=bar",
                "/somepage/mypage/paragraphs/0/mybool=boolean:true",
                "/somepage/mypage/paragraphs/0/rand@type=mgnl:resource",
                "/somepage/mypage/paragraphs/0/rand.fileName=randdddd",
                "/somepage/mypage/paragraphs/0/rand.extension=png",
                "/somepage/mypage/paragraphs/0/rand.jcr\\:data=X",
                "/somepage/mypage/paragraphs/0/rand.jcr\\:mimeType=image/png",
                "/somepage/mypage/paragraphs/0/rand.jcr\\:lastModified="
        ), "\n");
        // --- only this line differs from the DefaultContentTest equivalent
        final MockHierarchyManager hm = MockUtil.createHierarchyManager(contentProperties);
        // ---

        final Content content = hm.getContent("/somepage/mypage/paragraphs/0");
        final Collection<NodeData> props = content.getNodeDataCollection("att*|ima*");
        assertEquals(5, props.size());

        // sort by name
        final TreeSet<NodeData> sorted = new TreeSet<NodeData>(new Comparator<NodeData>() {
            @Override
            public int compare(NodeData o1, NodeData o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        sorted.addAll(props);
        // sanity check - just recheck we still have 5 elements
        assertEquals(5, sorted.size());
        final Iterator<NodeData> it = sorted.iterator();
        final NodeData a = it.next();
        final NodeData b = it.next();
        final NodeData c = it.next();
        final NodeData d = it.next();
        final NodeData e = it.next();
        assertEquals("attachment1", a.getName());
        assertEquals(PropertyType.BINARY, a.getType());
        assertEquals("attachment2", b.getName());
        assertEquals(PropertyType.BINARY, b.getType());
        assertEquals("image3", d.getName());
        assertEquals(PropertyType.BINARY, d.getType());
        assertEquals("image3", d.getName());
        assertEquals(PropertyType.BINARY, d.getType());

        assertEquals("attention", c.getName());
        assertEquals(PropertyType.STRING, c.getType());
        assertEquals("booyah", c.getString());
        assertEquals("imaginary", e.getName());
        assertEquals(PropertyType.DATE, e.getType());
        assertEquals(true, e.getDate().before(Calendar.getInstance()));
    }

    /**
     * This is the mock-equivalent test of {@link info.magnolia.cms.core.DefaultContentTest#testStringPropertiesCanBeRetrievedByStreamAndViceVersa()}.
     */
    public void testStringPropertiesCanBeRetrievedByStreamAndViceVersa() throws Exception {
        String contentProperties = StringUtils.join(Arrays.asList(
                "/hello/foo=bar",
                // a binary
                "/hello/bin@type=mgnl:resource",
                "/hello/bin.fileName=hello",
                "/hello/bin.extension=gif",
                "/hello/bin.jcr\\:data=some-data",
                "/hello/bin.jcr\\:mimeType=image/gif",
                "/hello/bin.jcr\\:lastModified="), "\n");
        // --- only this line differs from the DefaultContentTest equivalent
        final MockHierarchyManager hm = MockUtil.createHierarchyManager(contentProperties);
        // ---

        final Content content = hm.getContent("/hello");
        final NodeData st = content.getNodeData("foo");
        assertTrue(st instanceof MockNodeData);
        assertEquals(PropertyType.STRING, st.getType());
        assertEquals("bar", st.getString());
        assertEquals("bar", IOUtils.toString(st.getStream()));

        final NodeData bin = content.getNodeData("bin");
        assertTrue(bin instanceof BinaryMockNodeData);
        assertEquals(PropertyType.BINARY, bin.getType());
        assertEquals("some-data", IOUtils.toString(bin.getStream()));
        assertEquals("some-data", bin.getString());
    }

    public void testOrderBefore() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
 "/node/b\n" +
            "/node/c\n");
        Content node = hm.getContent("/node");
        node.orderBefore("c", "b");
        Collection<Content> result = node.getChildren();
        // transform to collection of names
        CollectionUtils.transform(result, new Transformer() {
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(Arrays.asList(new String[]{"a", "c","b"}), result);
    }

    public void testOrderBefore2() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
 "/node/b\n" +
            "/node/c\n");
        Content node = hm.getContent("/node");
        node.orderBefore("a", "c");
        Collection<Content> result = node.getChildren();
        // transform to collection of names
        CollectionUtils.transform(result, new Transformer() {
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(Arrays.asList(new String[]{"b", "a","c"}), result);
    }

    public void testOrderBeforeFirstNode() throws RepositoryException, IOException{
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node/a\n" +
 "/node/b\n" +
            "/node/c\n");
        Content node = hm.getContent("/node");
        node.orderBefore("c", "a");
        Collection<Content> result = node.getChildren();
        // transform to collection of names
        CollectionUtils.transform(result, new Transformer() {
            @Override
            public Object transform(Object childObj) {
                return ((Content)childObj).getName();
            }
        });
        assertEquals(Arrays.asList(new String[]{"c", "a","b"}), result);
    }

    public void testGetAncestor() throws RepositoryException, IOException {
        MockHierarchyManager hm =
                MockUtil.createHierarchyManager("/level0\n" + "/level0/level1\n" + "/level0/level1/level2\n");
        Content node = hm.getContent("/level0/level1/level2");
        Collection<Content> ancestors = node.getAncestors();
        assertEquals(3, ancestors.size());
    }

    public void testCanDeleteAProperty() throws Exception {
        MockHierarchyManager hm = MockUtil.createHierarchyManager(
            "/node.a=lol\n" +
            "/node.b=yop\n" +
            "/node.c=boum\n");
        final Content node = hm.getContent("/node");
        assertEquals(3, node.getNodeDataCollection().size());
        node.deleteNodeData("b");

        assertEquals(2, node.getNodeDataCollection().size());
        assertEquals(true, node.getNodeData("a").isExist());
        assertEquals(true, node.getNodeData("c").isExist());
        assertEquals(false, node.getNodeData("b").isExist());
    }
}
