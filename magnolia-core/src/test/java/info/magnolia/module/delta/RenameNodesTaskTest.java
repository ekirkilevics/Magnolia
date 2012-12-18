/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.module.delta;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;

/**
 * Tests.
 */
public class RenameNodesTaskTest extends RepositoryTestCase {

    @Test
    public void testRenameACLNodesTask() throws Exception {

        // Given
        final Session session = MgnlContext.getSystemContext().getJCRSession(RepositoryConstants.USER_ROLES);
        final String relPath = "taskWillTreatAllNodesUnderThisOne";

        session.getRootNode().addNode(relPath);
        final String oldName = "old";
        final String newName = "new";
        final String testNodeType = NodeTypes.Content.NAME;
        final Node rootOfOperation = session.getRootNode().getNode(relPath);
        // a first one
        rootOfOperation.addNode(oldName, testNodeType);

        // now one deeper in hierarchy
        final String anotherName = "willNotBeRenamed";
        rootOfOperation.addNode(anotherName).addNode(oldName, testNodeType);

        // one with matching name but wrong NodeType
        final String pathForSubNodeWithWrongNodeType = "subnodeHasWrongNodeType";
        rootOfOperation.addNode(pathForSubNodeWithWrongNodeType).addNode(oldName, NodeTypes.ContentNode.NAME);

        InstallContext installContext = mock(InstallContext.class);
        when(installContext.getJCRSession(RepositoryConstants.CONFIG)).thenReturn(session);

        // When
        RenameNodesTask task = new RenameNodesTask("name", "description", RepositoryConstants.CONFIG, "/"+relPath, oldName, newName, testNodeType);
        task.execute(installContext);

        // Then
        session.getRootNode().getNode(relPath).getNode(newName);
        session.getRootNode().getNode(relPath).getNode(anotherName).getNode(newName);

        final Node parentForNodeWithWrongNodeType = session.getRootNode().getNode(relPath).getNode(pathForSubNodeWithWrongNodeType);
        try {
            parentForNodeWithWrongNodeType.getNode(newName);
            fail("Expected PathNotFoundException!");
        } catch (RepositoryException e) {
            assertTrue(e instanceof PathNotFoundException);
        }
    }
}
