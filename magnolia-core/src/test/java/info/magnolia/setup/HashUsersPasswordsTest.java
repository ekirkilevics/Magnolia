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
package info.magnolia.setup;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.Digester;
import info.magnolia.module.InstallContext;
import info.magnolia.test.mock.MockContent;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for password hashing update task.
 * 
 * @version $Id$
 * 
 */
public class HashUsersPasswordsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testEncryption() throws Exception {
        HashUsersPasswords hash = new HashUsersPasswords();
        InstallContext installContext = mock(InstallContext.class);
        HierarchyManager hm = mock(HierarchyManager.class);
        when(installContext.getHierarchyManager("users")).thenReturn(hm);
        MockContent root = new MockContent("");
        MockContent folder = new MockContent("system", ItemType.FOLDER);
        root.addContent(folder);
        MockContent superuser = new MockContent("superuser", ItemType.USER);
        folder.addContent(superuser);
        String encodedPwd = Base64.encodeBase64String("blaboo".getBytes());
        superuser.addNodeData("pswd", encodedPwd);
        when(hm.getContent("/")).thenReturn(root);
        hash.execute(installContext);
        String hashPwd = superuser.getNodeData("pswd").getString();
        assertFalse(encodedPwd.equals(hashPwd));
        assertTrue(Digester.matchBCrypted("blaboo", hashPwd));
    }

}
