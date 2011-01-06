/**
 * This file Copyright (c) 2007-2011 Magnolia International
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

import info.magnolia.cms.security.MgnlRoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.importexport.PropertiesImportExport;
import info.magnolia.module.InstallContext;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import static org.easymock.EasyMock.*;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Properties;


/**
 * @author vsteller
 * @version $Id$
 *
 */
public class AddURIPermissionTaskTest extends MgnlTestCase {
    public void testGetPostPermissionAddedToRoleProperly() throws Exception {
        final Properties result = doTestPermissionAddedToRoleProperly(AddURIPermissionTask.GET_POST);
        assertEquals("63", result.get("/rolename/acl_uri/0.permissions"));
    }
    
    public void testGetPermissionAddedToRoleProperly() throws Exception {
        final Properties result = doTestPermissionAddedToRoleProperly(AddURIPermissionTask.GET);
        assertEquals("8", result.get("/rolename/acl_uri/0.permissions"));
    }
    
    public void testDenyPermissionAddedToRoleProperly() throws Exception {
        final Properties result = doTestPermissionAddedToRoleProperly(AddURIPermissionTask.DENY);
        assertEquals("0", result.get("/rolename/acl_uri/0.permissions"));
    }

    private Properties doTestPermissionAddedToRoleProperly(int permission) throws IOException, RepositoryException,
        TaskExecutionException, Exception {
        final String testContent = "" + 
            "/rolename";
        
        MockHierarchyManager hm = MockUtil.createAndSetHierarchyManager("userroles", testContent);
        
        SecuritySupportImpl securitySupport = new SecuritySupportImpl();
        securitySupport.setRoleManager(new MgnlRoleManager());
        ComponentsTestUtil.setInstance(SecuritySupport.class, securitySupport);

        final InstallContext installCtx = createMock(InstallContext.class);
        
        replay(installCtx);
        final AddURIPermissionTask task = new AddURIPermissionTask("Test", "Description", "rolename", "/someURI", permission);
        task.execute(installCtx);
        verify(installCtx);
        
        final Properties result = PropertiesImportExport.toProperties(hm);
        assertEquals("/someURI", result.get("/rolename/acl_uri/0.path"));
        return result;
    }
}
