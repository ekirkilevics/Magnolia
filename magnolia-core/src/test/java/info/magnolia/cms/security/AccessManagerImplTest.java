package info.magnolia.cms.security;

import info.magnolia.cms.util.SimpleUrlPattern;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class AccessManagerImplTest extends TestCase {

    private static final String TEST = "/admin/test";
    private static final String TEST_LANG = "/admin/test/language";


    public void testGetPermissions() {
        final Permission accessChildrenPermission = new PermissionImpl();
        accessChildrenPermission.setPattern(new SimpleUrlPattern("/admin/test/*"));
        accessChildrenPermission.setPermissions(8);
        final List permissionList = new ArrayList();
        permissionList.add(accessChildrenPermission);
        AccessManagerImpl ami = new AccessManagerImpl();
        // no permission to access given path by default
        assertEquals(0, ami.getPermissions(TEST));
        assertEquals(0, ami.getPermissions(TEST_LANG));
        // permission to children doesn't give rights to access the parent
        ami.setPermissionList(permissionList);
        assertEquals(0, ami.getPermissions(TEST));
        assertEquals(8, ami.getPermissions(TEST_LANG));

        permissionList.clear();
        final Permission accessParentPermission = new PermissionImpl();
        accessParentPermission.setPattern(new SimpleUrlPattern("/admin/test"));
        accessParentPermission.setPermissions(8);
        permissionList.add(accessParentPermission);
        assertEquals(8, ami.getPermissions(TEST));
        assertEquals(0, ami.getPermissions(TEST_LANG));

        permissionList.clear();
        final Permission accessAllPermission = new PermissionImpl();
        // actually while this is a valid pattern it would give access to all the nodes that start with given prefix ... dangerous when used in user management
        accessAllPermission.setPattern(new SimpleUrlPattern("/admin/test*"));
        accessAllPermission.setPermissions(8);
        permissionList.add(accessAllPermission);
        assertEquals(8, ami.getPermissions(TEST));
        assertEquals(8, ami.getPermissions(TEST_LANG));
    }

}
