/**
 * This file Copyright (c) 2009 Magnolia International
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
package info.magnolia.module.workflow.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.nodebuilder.NodeBuilder;
import static info.magnolia.nodebuilder.Ops.addNode;
import static info.magnolia.nodebuilder.Ops.addProperty;

import javax.jcr.RepositoryException;

import static org.easymock.classextension.EasyMock.*;

/**
 * A test class for WorkflowModuleVersionHandler.
 * @author had
 */
public class WorkflowModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {
    private RoleManager roleManager;
    private Role role;

    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/workflow.xml";
    }

    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new WorkflowModuleVersionHandler();
    }

    protected void setUp() throws Exception {
        super.setUp();
        // setup security
        SecuritySupportImpl securitySupport = new SecuritySupportImpl();
        roleManager = createStrictMock(RoleManager.class);
        role = createStrictMock(Role.class);
        
        expect(roleManager.getRole("workflow-base")).andReturn(role).anyTimes();
        role.addPermission("config", "/modules/workflow/config/flows", Permission.READ);
        role.addPermission("config", "/modules/workflow/config/flows/*", Permission.READ);

        // 4.3
        role.removePermission("userroles", "/workflow-base", Permission.READ);
        role.removePermission("userroles", "/workflow-base/*", Permission.READ);

        securitySupport.setRoleManager(roleManager);
        replay(roleManager, role);
        ComponentsTestUtil.setInstance(SecuritySupport.class, securitySupport);
        
        // the update tries 
        bootstrapSingleResource("/mgnl-bootstrap/workflow/userroles.workflow-base.xml");
        MgnlContext.getHierarchyManager("userroles").save();
    }

    @Override
    protected void tearDown() throws Exception {
        verify(roleManager, role);
        super.tearDown();
    }

    /**
     * Workflow have been overwriting versioning command of DMS when introducing wkf support to DMS. The task to rememdy the situation have been introduced now
     *
     * Testing update to 4.2
     */
    public void testDMSVersioningCommandUpdate() throws ModuleManagementException, RepositoryException {
        // prepare nodes that should exist if the dms was really installed ...
        final HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        Content commands = ContentUtil.createPath(hm, "/modules/dms/commands/dms", ItemType.CONTENTNODE, true);
        final NodeBuilder nodeBuilder = new NodeBuilder(commands,
                addNode("activate").then(
                        addNode("version").then(
                                addProperty("class", "info.magnolia.module.admininterface.commands.VersionCommand")
                        )
                )
        );
        nodeBuilder.exec();
        hm.save();
        
        // run the test itself
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.1.1"));

        // check the proper versioning commmand is used
        assertEquals("info.magnolia.module.dms.commands.DocumentVersionCommand", hm.getNodeData("/modules/dms/commands/dms/activate/version/class").getString());
    }

    /**
     * Workflow should enable itself for data module content activation if data module is installed.
     * Testing update to 4.2
     */
    public void testDataActivationCommandUpdate() throws ModuleManagementException, RepositoryException {
        final HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        // workflow configuration
        Content path = ContentUtil.createPath(hm, "modules/data/commands/data/", ItemType.CONTENT);
        path.createContent("activate", ItemType.CONTENTNODE);

        hm.save();
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.1.1"));

        // workflow is not installed so the wkf config class will not be set
        assertFalse(hm.isExist("/modules/data/trees/data/configurationClass"));
    }
    
    protected String[] getExtraWorkspaces() {
        return new String[]{"dms", "data"};
    }
}
