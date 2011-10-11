/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module.mail.util;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContent;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/**
 * Test for mail utils.
 *
 * @author had
 *
 */
public class MailUtilTest {

    @Test
    public void testGetGroupMembersMails() throws Exception {

        UserManager manager = createMock(UserManager.class);
        StringBuffer ret = new StringBuffer();
        MockContent peteNode = new MockContent("pete");
        peteNode.createContent("groups").setNodeData("0", "no-mail-group-uuid");
        peteNode.setNodeData("email", "test@pete.com");
        SystemContext ctx = createMock(SystemContext.class);
        MgnlContext.setInstance(ctx);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        HierarchyManager usersHM = createMock(HierarchyManager.class);
        Content admin = createMock(Content.class);
        Content system = createMock(Content.class);
        User pete = createMock(User.class);
        expect(pete.getAllGroups()).andReturn(Arrays.asList(new String[] { "noMailGroup", "test" }));
        HierarchyManager groupsHM = createMock(HierarchyManager.class);
        MockContent noMailContent = new MockContent("noMailGroup");
        noMailContent.createContent("groups").setNodeData("0", "test-group-uuid");
        MockContent testContent = new MockContent("test");
        GroupManager groupMan = createMock(GroupManager.class);
        ComponentsTestUtil.setInstance(GroupManager.class, groupMan);
        Group noMailGroup = createMock(Group.class);
        Group testGroup = createMock(Group.class);

        ComponentsTestUtil.setImplementation(SecuritySupport.class, SecuritySupportImpl.class);
        ((SecuritySupportImpl) SecuritySupport.Factory.getInstance()).setGroupManager(groupMan);

        expect(ctx.getHierarchyManager("users")).andReturn(usersHM);
        expect(usersHM.getContent("admin")).andReturn(admin);
        expect(admin.getChildren(ItemType.USER)).andReturn(Arrays.asList(new Content[] { peteNode }));
        expect(usersHM.getContent("system")).andReturn(system);
        expect(system.getChildren(ItemType.USER)).andReturn(Collections.EMPTY_LIST);

        expect(manager.getUser("pete")).andReturn(pete);

        expect(pete.getProperty("email")).andReturn("test@pete.com");
        expect(pete.getName()).andReturn("pete");

        Object[] mocks = new Object[] { manager, ctx, usersHM, admin, system, pete, groupsHM, groupMan, noMailGroup, testGroup };
        replay(mocks);
        MailUtil.getGroupMembersMails(manager, ret, "test");
        assertEquals("test@pete.com\n", ret.toString());
        verify(mocks);
    }

}
