/**
 * This file Copyright (c) 2009-2011 Magnolia International
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

import static info.magnolia.nodebuilder.Ops.addProperty;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.assertEquals;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.ActivationManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.admininterface.trees.WebsiteTreeConfiguration;
import info.magnolia.module.workflow.trees.WorkflowWebsiteTreeConfiguration;
import info.magnolia.nodebuilder.NodeBuilder;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;

import java.util.Arrays;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.collections.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class WorkflowModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {
    private RoleManager roleManager;
    private Role role;
    private ActivationManager activationManager;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/workflow.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList("/META-INF/magnolia/workflow.xml", "/META-INF/magnolia/admininterface.xml",
                "/META-INF/magnolia/rendering.xml", "/META-INF/magnolia/core.xml");
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new WorkflowModuleVersionHandler();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // setup security
        SecuritySupportImpl securitySupport = new SecuritySupportImpl();
        roleManager = createMock(RoleManager.class);
        role = createMock(Role.class);

        expect(roleManager.getRole("workflow-base")).andReturn(role).anyTimes();
        roleManager.addPermission(role, "Store", "/*", Permission.ALL);
        roleManager.addPermission(role, "Expressions", "/*", Permission.ALL);
        roleManager.addPermission(role, "dms", "/*", Permission.ALL);
        roleManager.addPermission(role, "data", "/*", Permission.ALL);

        roleManager.addPermission(role, "config", "/modules/workflow/config/flows", Permission.ALL);
        roleManager.addPermission(role, "config", "/modules/workflow/config/flows/*", Permission.ALL);

        expect(roleManager.getRole("superuser")).andReturn(role).anyTimes();

        securitySupport.setRoleManager(roleManager);
        activationManager = createMock(ActivationManager.class);
        expect(activationManager.getSubscribers()).andReturn(CollectionUtils.EMPTY_COLLECTION).anyTimes();
        replay(roleManager, role, activationManager);
        ComponentsTestUtil.setInstance(SecuritySupport.class, securitySupport);
        ComponentsTestUtil.setInstance(ActivationManager.class, activationManager);

        MgnlContext.getHierarchyManager("userroles").save();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        verify(roleManager, role, activationManager);
        super.tearDown();
    }

    @Test
    public void testChangeWebsiteTreeConfigurationTaskWasExecuted() throws ModuleManagementException, RepositoryException {
        final HierarchyManager hm = MgnlContext.getHierarchyManager(RepositoryConstants.CONFIG);
        Content commands =
                ContentUtil.createPath(hm, "/modules/adminInterface/trees/website", ItemType.CONTENTNODE, true);
        final NodeBuilder nodeBuilder =
                new NodeBuilder(commands, addProperty("configurationClass", WebsiteTreeConfiguration.class.getName()));
        nodeBuilder.exec();

        ContentUtil.createPath(hm, "/modules/adminInterface/config/menu/security");
        ContentUtil.createPath(hm, "/modules/adminInterface/config/menu/config");
        ContentUtil.createPath(hm, "/modules/dms/commands/dms/activate/version");
        ContentUtil.createPath(hm, "/modules/dms/trees/dms");
        Content dms = ContentUtil.createPath(hm, "/modules/adminInterface/trees/website");
        dms.setNodeData("configurationClass", WebsiteTreeConfiguration.class.getName());
        hm.save();

        // run the test itself
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // check the proper versioning commmand is used
        assertEquals(WorkflowWebsiteTreeConfiguration.class.getName(),
                hm.getNodeData("/modules/adminInterface/trees/website/configurationClass").getString());
    }

    @Override
    protected String[] getExtraWorkspaces() {
        return new String[] {"dms", "data"};
    }
}
