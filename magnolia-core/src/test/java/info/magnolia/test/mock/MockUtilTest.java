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
package info.magnolia.test.mock;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.jcr.RepositoryException;

import junit.framework.TestCase;

/**
 * @author philipp
 * @version $Id$
 *
 */
public class MockUtilTest extends TestCase {

    @Override
    protected void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    public void testGettingHierarchyManagerFromContext() throws IOException, RepositoryException{
        MockContext ctx = MockUtil.initMockContext();
        HierarchyManager hm = initTestData();
        ctx.addHierarchyManager(ContentRepository.CONFIG, hm);
        assertEquals(MgnlContext.getHierarchyManager(ContentRepository.CONFIG), hm);
    }

    public void testReadingNodes() throws IOException, RepositoryException{
        HierarchyManager hm = initTestData();

        Content parent = hm.getContent("/parent");
        Content sub1 = hm.getContent("/parent/sub1");
        Content sub2 = hm.getContent("/parent/sub2");

        assertNotNull(parent);
        assertNotNull(sub1);
        assertNotNull(sub2);

        assertEquals(sub1, parent.getContent("sub1"));
        assertEquals(sub2, parent.getContent("sub2"));

        assertEquals("mgnl:test", parent.getItemType().getSystemName());
        assertEquals("mgnl:sub", sub1.getItemType().getSystemName());
        assertEquals("mgnl:sub", sub2.getItemType().getSystemName());

        assertEquals("value1", parent.getNodeData("prop1").getString());
        assertEquals("value2", parent.getNodeData("prop2").getString());
        assertEquals("sub1value1", sub1.getNodeData("prop1").getString());
        assertEquals("sub1value2", sub1.getNodeData("prop2").getString());
        assertEquals("sub2value1", sub2.getNodeData("prop1").getString());
        assertEquals("sub2value2", sub2.getNodeData("prop2").getString());
    }

    public void testReadingBoolean() throws IOException, RepositoryException{
        HierarchyManager hm = initTestData();

        Content node = hm.getContent("/parent");
        assertEquals(true, node.getNodeData("prop3").getBoolean());
        assertEquals(Boolean.TRUE, NodeDataUtil.getValueObject(node.getNodeData("prop3")));
    }

    public void testGettingByUUID() throws IOException, RepositoryException{
        HierarchyManager hm = initTestData();

        Content node = hm.getContentByUUID("3");
        assertNotNull(node);
        assertEquals(node.getHandle(), "/parent/sub2");
    }

    public void testMetaData() throws IOException, RepositoryException {
        final HierarchyManager hm = initTestData();
        Content sub1 = hm.getContent("/parent/sub1");
        final MetaData metaData = sub1.getMetaData();
        assertEquals("greg", metaData.getAuthorId());
        assertEquals("bozo", metaData.getActivatorId());
        assertEquals("someParagraphName", metaData.getTemplate());
        assertEquals("MetaData", metaData.getLabel());
        assertEquals("myTitle", metaData.getTitle());
        assertEquals("/parent/sub1/MetaData", metaData.getHandle());
    }

    public void testSyntax() throws IOException, RepositoryException{
        String content =
            "/parent1/sub1.prop1=one\n"+
            "parent2/sub2.prop1=two\n"+
            "parent3.sub3.prop1=three"; // TODO : this syntax is deprecated
        // TODO : "/parent3/sub2.prop2" should be supported, should create a property in node parent3/sub2, with no value

        HierarchyManager hm = MockUtil.createHierarchyManager(content);
        assertEquals("one", hm.getContent("/parent1/sub1").getNodeData("prop1").getString());
        assertEquals("two", hm.getContent("/parent2/sub2").getNodeData("prop1").getString());
        assertEquals("three", hm.getContent("/parent3/sub3").getNodeData("prop1").getString());

        content =
            "/parent1/sub1@uuid=1\n"+
            "parent2/sub2.@uuid=2\n"+ // TODO : this syntax is deprecated
            "parent3.sub3@uuid=3\n"+  // TODO : this syntax is deprecated
            "parent4.sub4.@uuid=4"; // TODO : this syntax is deprecated

        hm = MockUtil.createHierarchyManager(content);
        assertEquals("1", hm.getContent("/parent1/sub1").getUUID());
        assertEquals("2", hm.getContent("/parent2/sub2").getUUID());
        assertEquals("3", hm.getContent("/parent3/sub3").getUUID());
        assertEquals("4", hm.getContent("/parent4/sub4").getUUID());
    }

    public void testNodesCanBeCreatedWithoutProperties() throws IOException, RepositoryException{
        String content =
            "/node1/sub1.prop1=one\n"+
            "/node2/sub2/\n"+
            "/node3/sub3\n"+
            "/node4\n";

        HierarchyManager hm = MockUtil.createHierarchyManager(content);
        assertEquals("one", hm.getContent("/node1/sub1").getNodeData("prop1").getString());
        assertEquals(0, hm.getContent("/node2/sub2").getNodeDataCollection().size());
        assertEquals(0, hm.getContent("/node3/sub3").getNodeDataCollection().size());
        assertEquals(0, hm.getContent("/node4").getNodeDataCollection().size());
    }

    protected HierarchyManager initTestData() throws IOException, RepositoryException {
        return MockUtil.createHierarchyManager(ContentRepository.CONFIG, this.getClass().getResourceAsStream("testcontent.properties"));
    }


}
