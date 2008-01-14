/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.setup.for3_5;

import info.magnolia.cms.core.Content;
import info.magnolia.module.InstallContext;
import info.magnolia.setup.for3_5.AddURIPermissionsToAllRoles;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import java.util.Properties;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AddURIPermissionsToAllRolesTest extends TestCase {
    private static final String SOMECONTENT = "" +
            "dummyrole.title=A Dummy Role\n" +
            "anonymous.title=The anonymous role\n";

    public void testRegularRolesGetAllUriPermOnSlashStar() throws Exception {
        final Properties result = doTestURIPermissionsGetAddedProperly("dummyrole", true);
        assertEquals("/*", result.get("/dummyrole/acl_uri/0.path"));
        assertEquals("63", result.get("/dummyrole/acl_uri/0.permissions"));
        assertEquals("The anonymous role", result.get("/anonymous.title"));
        assertEquals("A Dummy Role", result.get("/dummyrole.title"));
    }

    public void testRegularRolesGetAllUriPermOnSlashStarAlsoOnPublicInstances() throws Exception {
        final Properties result = doTestURIPermissionsGetAddedProperly("dummyrole", false);
        assertEquals(4, result.size());
        assertEquals("/*", result.get("/dummyrole/acl_uri/0.path"));
        assertEquals("63", result.get("/dummyrole/acl_uri/0.permissions"));
        assertEquals("The anonymous role", result.get("/anonymous.title"));
        assertEquals("A Dummy Role", result.get("/dummyrole.title"));
    }

    public void testAnonymousRoleGetsDenyOnAllOnAuthorInstances() throws Exception {
        final Properties result = doTestURIPermissionsGetAddedProperly("anonymous", true);
        assertEquals(4, result.size());
        assertEquals("/*", result.get("/anonymous/acl_uri/0.path"));
        assertEquals("0", result.get("/anonymous/acl_uri/0.permissions"));
        assertEquals("The anonymous role", result.get("/anonymous.title"));
        assertEquals("A Dummy Role", result.get("/dummyrole.title"));
    }

    public void testAnonymousRoleGetsAccessOnSlashStarAndDenyOnDotMagnoliaOnPublicInstances() throws Exception {
        final Properties result = doTestURIPermissionsGetAddedProperly("anonymous", false);
        assertEquals(8, result.size());
        assertEquals("/*", result.get("/anonymous/acl_uri/0.path"));
        assertEquals("63", result.get("/anonymous/acl_uri/0.permissions"));
        assertEquals("/.magnolia", result.get("/anonymous/acl_uri/00.path"));
        assertEquals("0", result.get("/anonymous/acl_uri/00.permissions"));
        assertEquals("/.magnolia/*", result.get("/anonymous/acl_uri/01.path"));
        assertEquals("0", result.get("/anonymous/acl_uri/01.permissions"));
        assertEquals("The anonymous role", result.get("/anonymous.title"));
        assertEquals("A Dummy Role", result.get("/dummyrole.title"));
    }

    private Properties doTestURIPermissionsGetAddedProperly(String roleName, boolean isAuthorInstance) throws Exception {
        final MockHierarchyManager hm = MockUtil.createHierarchyManager(SOMECONTENT);
        final InstallContext ctx = createStrictMock(InstallContext.class);

        replay(ctx);
        final AddURIPermissionsToAllRoles task = new AddURIPermissionsToAllRoles(isAuthorInstance);
        final Content roleNode = hm.getContent("/" + roleName);
        task.operateOnChildNode(roleNode, ctx);
        verify(ctx);

        return MockUtil.toProperties(hm);
    }
}
