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

import static org.junit.Assert.assertTrue;
import info.magnolia.cms.security.MgnlGroupManager;
import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class MailUtilTest {

    private MgnlUserManager userManager;
    private StringBuffer mailList;

    @Before
    protected void setUp() throws Exception {
        final SecuritySupportImpl sec = new SecuritySupportImpl();
        sec.setGroupManager(new MgnlGroupManager());
        ComponentsTestUtil.setInstance(SecuritySupport.class, sec);
        mailList = new StringBuffer();
        MockUtil.initMockContext();
        MockUtil.createAndSetHierarchyManager(RepositoryConstants.USERS,
                getClass().getResourceAsStream("/sample-users.properties"));
        MockUtil.createAndSetHierarchyManager(RepositoryConstants.USER_GROUPS,
                getClass().getResourceAsStream("/sample-usergroups.properties"));
        userManager = new MgnlUserManager() {
            {
                setRealmName("test");
            }

            @Override
            protected Node findPrincipalNode(String name, Session session) throws RepositoryException {
                return session.getNode("/" + getRealmName() + "/" + name);
            }
        };
    }

    @Test
    public void testGetGroupMembersMails() {
        MailUtil.getGroupMembersMails(userManager, mailList, "groupA");
        assertTrue(mailList.toString().equals("kvido@test.info\npupak@test.info\n")
                || mailList.toString().equals("pupak@test.info\nkvido@test.info\n"));
    }

    @After
    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }
}
