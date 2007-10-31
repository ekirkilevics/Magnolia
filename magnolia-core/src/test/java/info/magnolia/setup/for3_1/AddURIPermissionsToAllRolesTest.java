/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.setup.for3_1;

import info.magnolia.cms.core.Content;
import info.magnolia.module.InstallContext;
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
