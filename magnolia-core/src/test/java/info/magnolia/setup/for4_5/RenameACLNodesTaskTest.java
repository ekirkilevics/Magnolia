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
package info.magnolia.setup.for4_5;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Test;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test case for RenameACLNodesTask.
 */
public class RenameACLNodesTaskTest extends RepositoryTestCase {

    @Test
    public void testRenameACLNodesTask() throws Exception {

        // Given
        Session session = MgnlContext.getSystemContext().getJCRSession(RepositoryConstants.USER_ROLES);
        Node roleNode = session.getRootNode().addNode("superuser", NodeTypes.Role.NAME);
        roleNode.addNode("acl_dms_dms", NodeTypes.ContentNode.NAME);
        roleNode.addNode("acl_website", NodeTypes.ContentNode.NAME);
        roleNode.addNode("something_else_completely", NodeTypes.ContentNode.NAME);

        InstallContext installContext = mock(InstallContext.class);
        when(installContext.getJCRSession(RepositoryConstants.USER_ROLES)).thenReturn(session);

        // When
        RenameACLNodesTask task = new RenameACLNodesTask();
        task.execute(installContext);

        // Then
        session.getNode("/superuser/acl_dms");
        session.getNode("/superuser/acl_website");
        session.getNode("/superuser/something_else_completely");
    }
}
