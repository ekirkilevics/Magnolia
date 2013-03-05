/**
 * This file Copyright (c) 2008-2012 Magnolia International
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
package info.magnolia.cms.core.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockContext;

import java.io.ByteArrayInputStream;
import java.util.Collections;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;

import org.apache.jackrabbit.JcrConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class BaseVersionManagerTest extends RepositoryTestCase {

    private static String mgnlMixDeleted = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<nodeTypes" + " xmlns:rep=\"internal\""
    + " xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\"" + " xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\""
    + " xmlns:mgnl=\"http://www.magnolia.info/jcr/mgnl\"" + " xmlns:jcr=\"http://www.jcp.org/jcr/1.0\">" + "<nodeType name=\"" + NodeTypes.Deleted.NAME
    + "\" isMixin=\"true\" hasOrderableChildNodes=\"true\" primaryItemName=\"\">" + "<supertypes>" + "<supertype>nt:base</supertype>"
    + "</supertypes>" + "</nodeType>" + "</nodeTypes>";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // context is then cleared automatically on teardown by RepoTestCase(MgnlTestCase)
        MockContext ctx = (MockContext) MgnlContext.getInstance();
        ctx.setUser(new MgnlUser("toto","admin",Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_MAP, null, null));
        MgnlContext.setInstance(ctx);
    }

    @Test
    public void testCreateAndRestoreVersion() throws RepositoryException{
        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        VersionManager versionMan = VersionManager.getInstance();
        Node node = session.getRootNode().addNode( "page", NodeTypes.Content.NAME);
        node.addNode("paragraph", NodeTypes.ContentNode.NAME);
        session.save();
        Version version = versionMan.addVersion(node);
        assertFalse("Original node should not have mixin", node.isNodeType(JcrConstants.MIX_VERSIONABLE));

        Node nodeInVersionWS =versionMan.getVersionedNode(node);
        assertTrue("Node in mgnlVersion workspace must have mixin", nodeInVersionWS.isNodeType(JcrConstants.MIX_VERSIONABLE));

        // assert that the the paragraph was versioned
        Node versionedNode = versionMan.getVersion(node, version.getName());
        assertTrue("Versioned content must include the paragraph", versionedNode.hasNode("paragraph"));

        // now delete the paragraph
        node.getNode("paragraph").remove();
        node.save();
        assertFalse("Paragraph should be deleted", node.hasNode("paragraph"));

        // restore
        //FIXME: wrap all nodes returned by the session (except for mgnlVersion) in the wrapper that delegates restore call to the version manager ...
        node.restore(version.getName(), true);
        assertTrue("Paragraph should be restored", node.hasNode("paragraph"));
    }

    @Test
    public void testCreateAndRestoreDeletedVersion() throws RepositoryException {
        Provider repoProvider = ContentRepository.getRepositoryProvider(RepositoryConstants.WEBSITE);

        repoProvider.registerNodeTypes(new ByteArrayInputStream(mgnlMixDeleted.getBytes()));

        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        VersionManager versionMan = VersionManager.getInstance();
        Node node = session.getRootNode().addNode( "page", NodeTypes.Content.NAME);

        // add deleted mixin
        node.addMixin(NodeTypes.Deleted.NAME);

        session.save();
        versionMan.addVersion(node);

        Node nodeInVersionWS = versionMan.getVersionedNode(node);
        assertTrue("Node in mgnlVersion workspace must have mixin", nodeInVersionWS.isNodeType(NodeTypes.Deleted.NAME));

        node.removeMixin(NodeTypes.Deleted.NAME);
        session.save();

        assertFalse("Node in website workspace should not have mixin", node.isNodeType(NodeTypes.Deleted.NAME));

        // add version w/o mixin
        versionMan.addVersion(node);
        nodeInVersionWS = versionMan.getVersionedNode(node);

        assertFalse("Node in mgnlVersion workspace should not have mixin", nodeInVersionWS.isNodeType(NodeTypes.Deleted.NAME));
    }

    @Test
    public void testUseSystemSessionToRetrieveVersions() throws RepositoryException {
        Session session = MgnlContext.getSystemContext().getJCRSession(RepositoryConstants.VERSION_STORE);
        VersionManager versionMan = VersionManager.getInstance();
        assertSame(session, versionMan.getSession());
    }

    @Test
    public void testNumberOfCreatedVersions() throws LoginException, RepositoryException{
        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        Node root = session.getRootNode();

        Node firstPage = root.addNode("firstPage", NodeTypes.Page.NAME);


        session.save();
        VersionManager versionManager = VersionManager.getInstance();

        final Rule rule = new Rule();
        rule.addAllowType(NodeTypes.Page.NAME);
        rule.addAllowType(NodeTypes.ContentNode.NAME);
        rule.addAllowType(NodeTypes.Resource.NAME);

        firstPage.setProperty("title", "v1title");
        firstPage.addNode("v1child", "mgnl:area");
        firstPage.save();
        Version v1 = versionManager.addVersion(firstPage, rule);
        assertEquals(versionManager.getAllVersions(firstPage).getSize(), 2);
        Node versionedNode = versionManager.getVersion(firstPage, v1.getName());
        assertEquals("v1title", versionedNode.getProperty("title").getString());
        assertTrue(versionedNode.hasNode("v1child"));
        assertEquals("toto", versionManager.getSystemNode(versionedNode).getProperty(ContentVersion.VERSION_USER).getString());

        firstPage.setProperty("title", "v2title");
        firstPage.addNode("v2child", "mgnl:area");
        firstPage.save();
        Version v2 = versionManager.addVersion(firstPage, rule);
        versionedNode = versionManager.getVersion(firstPage, v2.getName());
        assertEquals(versionManager.getAllVersions(firstPage).getSize(), 3);
        assertEquals("v2title", versionedNode.getProperty("title").getString());
        assertTrue(versionedNode.hasNode("v2child"));
        assertEquals("toto", versionManager.getSystemNode(versionedNode).getProperty(ContentVersion.VERSION_USER).getString());

        //test when user wasn't set into MgnlContext
        MockContext ctx = (MockContext) MgnlContext.getInstance();
        ctx.setUser(null);
        MgnlContext.setInstance(ctx);

        firstPage.setProperty("title", "v3title");
        firstPage.addNode("v3child", "mgnl:area");
        firstPage.save();
        Version v3 = versionManager.addVersion(firstPage, rule);
        versionedNode = versionManager.getVersion(firstPage, v3.getName());
        assertEquals(versionManager.getAllVersions(firstPage).getSize(), 4);
        assertEquals("v3title", versionedNode.getProperty("title").getString());
        assertTrue(versionedNode.hasNode("v3child"));
        assertEquals("", versionManager.getSystemNode(versionedNode).getProperty(ContentVersion.VERSION_USER).getString());
    }
    
    @Test
    public void testCreateAndRestoreVersionAndCheckOrder() throws RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        VersionManager versionMan = VersionManager.getInstance();
        Node node = session.getRootNode().addNode("page", MgnlNodeType.NT_PAGE);
        node.addNode("component0", MgnlNodeType.NT_COMPONENT);
        node.addNode("component1", MgnlNodeType.NT_COMPONENT);
        node.addNode("component2", MgnlNodeType.NT_COMPONENT);
        node.addNode("component3", MgnlNodeType.NT_COMPONENT);
        Node area = node.addNode("area", MgnlNodeType.NT_AREA);
        area.addNode("sub0", MgnlNodeType.NT_COMPONENT);
        area.addNode("sub1", MgnlNodeType.NT_COMPONENT);
        session.save();
        Version version = versionMan.addVersion(node);

        Node nodeInVersionWS = versionMan.getVersionedNode(node);
        // sanity check
        NodeIterator iterator = nodeInVersionWS.getNodes("compo*");
        assertEquals("component0", iterator.nextNode().getName());
        assertEquals("component1", iterator.nextNode().getName());
        assertEquals("component2", iterator.nextNode().getName());
        assertEquals("component3", iterator.nextNode().getName());
        iterator = area.getNodes("sub*");
        assertEquals("sub0", iterator.nextNode().getName());
        assertEquals("sub1", iterator.nextNode().getName());

        // WHEN
        // swap subnodes - put first at the end
        node.orderBefore("component0", null);
        // and second before the fourth
        node.orderBefore("component1", "component3");
        area.orderBefore("sub0", null);
        session.save();
        // sanity check again
        iterator = node.getNodes("compo*");
        assertEquals("component2", iterator.nextNode().getName());
        assertEquals("component1", iterator.nextNode().getName());
        assertEquals("component3", iterator.nextNode().getName());
        assertEquals("component0", iterator.nextNode().getName());
        iterator = area.getNodes("sub*");
        assertEquals("sub1", iterator.nextNode().getName());
        assertEquals("sub0", iterator.nextNode().getName());

        // restore
        node.restore(version.getName(), true);

        // THEN
        // http://www.day.com/specs/jcr/2.0/23_Orderable_Child_Nodes.html
        // If a node has orderable child nodes then at any time its child node set has a current order, reflected in the iterator returned by Node.getNodes()(see §5.2.2 Iterating Over Child Items). If a node does not have orderable child nodes then the order of nodes returned by Node.getNodes is not guaranteed and may change at any time.
        iterator = node.getNodes("compo*");
        assertEquals("component0", iterator.nextNode().getName());
        assertEquals("component1", iterator.nextNode().getName());
        iterator = node.getNode("area").getNodes("sub*");
        assertEquals("sub0", iterator.nextNode().getName());
        assertEquals("sub1", iterator.nextNode().getName());

    }

    @Test
    public void testCreateAndRestoreVersionAndCheckOrderBetweenTwoVersions() throws RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        VersionManager versionMan = VersionManager.getInstance();
        Node node = session.getRootNode().addNode("page", MgnlNodeType.NT_PAGE);
        node.addNode("component0", MgnlNodeType.NT_COMPONENT);
        node.addNode("component1", MgnlNodeType.NT_COMPONENT);
        session.save();
        Version version1 = versionMan.addVersion(node);

        // add 3rd and order on top
        node.addNode("component2", MgnlNodeType.NT_COMPONENT);
        node.orderBefore("component2", "component0");
        session.save();
        // version again
        Version version2 = versionMan.addVersion(node);

        // WHEN
        // restore 1st
        node.restore(version1.getName(), true);

        // THEN
        NodeIterator iterator = node.getNodes("compo*");
        assertEquals("component0", iterator.nextNode().getName());
        assertEquals("component1", iterator.nextNode().getName());
        // comp2 is gone
        assertFalse(iterator.hasNext());

        // WHEN
        // restore 2ndt
        node.restore(version2.getName(), true);

        // THEN
        iterator = node.getNodes("compo*");
        assertEquals("component2", iterator.nextNode().getName());
        assertEquals("component0", iterator.nextNode().getName());
        assertEquals("component1", iterator.nextNode().getName());

    }

    @Override
    @After
    public void tearDown(){
        MgnlContext.setInstance(null);
        SystemProperty.clear();
    }

}
