/**
 * This file Copyright (c) 2008-2011 Magnolia International
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

import java.io.ByteArrayInputStream;
import java.util.Collections;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.Provider;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockContext;

/**
 * @author philipp
 * @version $Id$
 */
public class BaseVersionManagerTest extends RepositoryTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockContext ctx = (MockContext) MgnlContext.getSystemContext();
        ctx.setUser(new MgnlUser("toto","admin",Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_MAP));
        //MockUtil.createAndSetHierarchyManager(ContentRepository.USERS, getClass().getResourceAsStream("superuser.properties"));
        //DataTransporter.importXmlStream(getClass().getResourceAsStream("/mgnl-bootstrap/core/users.system.superuser.xml"), ContentRepository.USERS, "/system", "superuser", false, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW, true, true);

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateAndRestoreVersion() throws RepositoryException{
        Session session = MgnlContext.getJCRSession(ContentRepository.WEBSITE);
        VersionManager versionMan = VersionManager.getInstance();
        Node node = session.getRootNode().addNode( "page", ItemType.CONTENT.getSystemName());
        node.addNode("paragraph", ItemType.CONTENTNODE.getSystemName());
        session.save();
        Version version = versionMan.addVersion(node);
        assertFalse("Original node should not have mixin", node.isNodeType(ItemType.MIX_VERSIONABLE));

        Node nodeInVersionWS =versionMan.getVersionedNode(node);
        assertTrue("Node in mgnlVersion workspace must have mixin", nodeInVersionWS.isNodeType(ItemType.MIX_VERSIONABLE));

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

    public void testCreateAndRestoreDeletedVersion() throws RepositoryException {
        Provider repoProvider = ContentRepository.getRepositoryProvider(ContentRepository.WEBSITE);
        String mgnlMixDeleted = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<nodeTypes" + " xmlns:rep=\"internal\""
        + " xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\"" + " xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\""
        + " xmlns:mgnl=\"http://www.magnolia.info/jcr/mgnl\"" + " xmlns:jcr=\"http://www.jcp.org/jcr/1.0\">" + "<nodeType name=\"" + ItemType.DELETED_NODE_MIXIN
        + "\" isMixin=\"true\" hasOrderableChildNodes=\"true\" primaryItemName=\"\">" + "<supertypes>" + "<supertype>nt:base</supertype>"
        + "</supertypes>" + "</nodeType>" + "</nodeTypes>";

        repoProvider.registerNodeTypes(new ByteArrayInputStream(mgnlMixDeleted.getBytes()));

        Session session = MgnlContext.getJCRSession(ContentRepository.WEBSITE);
        VersionManager versionMan = VersionManager.getInstance();
        Node node = session.getRootNode().addNode( "page", ItemType.CONTENT.getSystemName());

        // add deleted mixin
        node.addMixin(ItemType.DELETED_NODE_MIXIN);

        session.save();
        Version version = versionMan.addVersion(node);

        Node nodeInVersionWS = versionMan.getVersionedNode(node);
        assertTrue("Node in mgnlVersion workspace must have mixin", nodeInVersionWS.isNodeType(ItemType.DELETED_NODE_MIXIN));

        node.removeMixin(ItemType.DELETED_NODE_MIXIN);
        session.save();

        assertFalse("Node in website workspace should not have mixin", node.isNodeType(ItemType.DELETED_NODE_MIXIN));

        // add version w/o mixin
        versionMan.addVersion(node);
        nodeInVersionWS = versionMan.getVersionedNode(node);

        assertFalse("Node in mgnlVersion workspace should not have mixin", nodeInVersionWS.isNodeType(ItemType.DELETED_NODE_MIXIN));
    }

}
