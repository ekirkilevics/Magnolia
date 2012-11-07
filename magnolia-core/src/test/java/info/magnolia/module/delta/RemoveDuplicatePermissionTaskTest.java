/**
 * This file Copyright (c) 2012-2012 Magnolia International
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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import info.magnolia.jcr.iterator.SameChildNodeTypeIterator;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.cms.security.MgnlRoleManager;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.context.MgnlContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class RemoveDuplicatePermissionTaskTest {
    private InstallContextImpl installContext;

    @Before
    public void setUp() throws Exception {
        final SecuritySupportImpl sec = new SecuritySupportImpl();
        sec.setRoleManager(new MgnlRoleManager());
        ComponentsTestUtil.setInstance(SecuritySupport.class, sec);
        MockUtil.initMockContext();
        MockUtil.createAndSetHierarchyManager(RepositoryConstants.USER_ROLES, getClass().getResourceAsStream("sample-userroles.properties"));
        final ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        installContext = new InstallContextImpl(moduleRegistry);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testRemoveDuplicatePermission() throws Exception {
        Session session = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES);
        Node roleNode = session.getNodeByIdentifier(Security.getRoleManager().getRole("roleA").getId());
        
        Node aclNodeBefore = roleNode.getNode("acl_config");
        NodeIterator childrenIterBefore = new SameChildNodeTypeIterator(aclNodeBefore);
        assertEquals(3,childrenIterBefore.getSize());
        
        final RemoveDuplicatePermissionTask rdpt = new RemoveDuplicatePermissionTask(null, null, "roleA", "acl_config");
        rdpt.execute(installContext);

        Node aclNodeAfter = roleNode.getNode("acl_config");
        NodeIterator childrenIterAfter = new SameChildNodeTypeIterator(aclNodeAfter);
        assertEquals(1,childrenIterAfter.getSize());
        assertEquals(1, (new SameChildNodeTypeIterator(roleNode.getNode("acl_website"))).getSize());
        
        Node child = childrenIterAfter.nextNode();
        assertEquals("/*",child.getProperty("path").getString());
        assertEquals(8,child.getProperty("permissions").getLong());
        assertFalse(childrenIterAfter.hasNext());
    }
    
    @Test
    public void testRemoveDuplicatePermission2() throws Exception {
        Session session = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES);
        Node roleNode = session.getNodeByIdentifier(Security.getRoleManager().getRole("roleB").getId());
        
        Node aclNodeBefore = roleNode.getNode("acl_Expression");
        NodeIterator childrenIter = new SameChildNodeTypeIterator(aclNodeBefore);
        assertEquals(11,childrenIter.getSize());
        
        final RemoveDuplicatePermissionTask rdpt = new RemoveDuplicatePermissionTask(null, null, "roleB", "acl_Expression");
        rdpt.execute(installContext);

        Node aclNodeAfter = roleNode.getNode("acl_Expression");
        NodeIterator childrenIter1 = new SameChildNodeTypeIterator(aclNodeAfter);
        assertEquals(7,childrenIter1.getSize());
    }
}
