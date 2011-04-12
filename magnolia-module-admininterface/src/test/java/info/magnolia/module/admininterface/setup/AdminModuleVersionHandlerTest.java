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
package info.magnolia.module.admininterface.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.DefaultVirtualURIMapping;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.UnicodeNormalizer;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.test.ComponentsTestUtil;

import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AdminModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {
    private static final String QUICKSTART = "redirect:/.magnolia/pages/quickstart.html";
    private static final String ADMIN_CENTRAL = "redirect:/.magnolia/pages/adminCentral.html";

    @Override
    protected void setUp() throws Exception {
        ComponentsTestUtil.setInstance(ModuleRegistry.class, new ModuleRegistryImpl());
        ComponentsTestUtil.setImplementation(UnicodeNormalizer.Normalizer.class, "info.magnolia.cms.util.UnicodeNormalizer$NonNormalizer");

        super.setUp();
    }

    @Override
    public String getModuleDescriptorPath() {
        return "/META-INF/magnolia/admininterface.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/core.xml",
                "/META-INF/magnolia/templating.xml",
                "/META-INF/magnolia/admininterface.xml"
        );
    }

    @Override
    public ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new AdminModuleVersionHandler();
    }

    public void testDefaultURISetOnAuthorInstancesIsSetToAdminCentral() throws ModuleManagementException, RepositoryException {
        setupConfigProperty("/server/", "admin", "true");
        // fake a core install:
        setupConfigProperty("/server/filters/servlets/", "foo", "bar");

        final InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        checkDefaultUriMapping(ADMIN_CENTRAL);
        assertNoMessages(installContext);
    }


    public void testDefaultURISetOnPublicInstancesIsSetToQuickStartIfNoTemplatesExist() throws ModuleManagementException, RepositoryException {
        setupConfigProperty("/server/", "admin", "false");
        // fake a core install:
        setupConfigProperty("/server/filters/servlets/", "foo", "bar");

        final InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        checkDefaultUriMapping(QUICKSTART);
        assertNoMessages(installContext);
    }

    public void testDefaultUriOnPublicIsNotChangedIfTemplatesExist() throws ModuleManagementException, RepositoryException {
        setupConfigProperty("/server/", "admin", "false");
        // fake a pre-install:
        setupConfigProperty("/server/filters/servlets/", "foo", "bar");
        setupConfigProperty("/modules/adminInterface/config/menu/security/usersSystem", "onclick", "MgnlAdminCentral.showTree('users', '/system', true)");
        setupConfigProperty("/modules/adminInterface/config/menu/security/usersAdmin", "onclick", "MgnlAdminCentral.showTree('users', '/admin', true)");

        // fake old users tree config
        setupConfigProperty("/modules/adminInterface/trees/users", "class", "info.magnolia.module.admininterface.AdminTreeMVCHandler");
        setupConfigProperty("/modules/adminInterface/trees/userSystem", "class", "info.magnolia.module.admininterface.AdminTreeMVCHandler");
        setupConfigProperty("/modules/adminInterface/trees/userAdmin", "class", "info.magnolia.module.admininterface.AdminTreeMVCHandler");

        // fake website tree
        setupConfigNode("/modules/adminInterface/trees/website");
        // fake website commands
        setupConfigNode("/modules/adminInterface/commands/website");

        setupDummyTemplate();

        setupExistingDefaultUriMapping("custom-value");

        final InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("3.7"));

        checkDefaultUriMapping("custom-value");
        assertNoMessages(installContext);
    }

    public void testWarnsIfDefaultUriIsQuickstartOnPublicAndTemplatesExist() throws ModuleManagementException, RepositoryException {
        setupConfigProperty("/server/", "admin", "false");
        // fake a pre-install:
        setupConfigProperty("/server/filters/servlets/", "foo", "bar");
        setupConfigProperty("/modules/adminInterface/config/menu/security/usersSystem", "onclick", "MgnlAdminCentral.showTree('users', '/system', true)");
        setupConfigProperty("/modules/adminInterface/config/menu/security/usersAdmin", "onclick", "MgnlAdminCentral.showTree('users', '/admin', true)");

        // fake old users tree config
        setupConfigProperty("/modules/adminInterface/trees/users", "class", "info.magnolia.module.admininterface.AdminTreeMVCHandler");
        setupConfigProperty("/modules/adminInterface/trees/userSystem", "class", "info.magnolia.module.admininterface.AdminTreeMVCHandler");
        setupConfigProperty("/modules/adminInterface/trees/userAdmin", "class", "info.magnolia.module.admininterface.AdminTreeMVCHandler");

        // fake website tree
        setupConfigNode("/modules/adminInterface/trees/website");
        // fake website commands
        setupConfigNode("/modules/adminInterface/commands/website");

        setupDummyTemplate();

        setupExistingDefaultUriMapping(QUICKSTART);

        final InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.1"));

        checkDefaultUriMapping(QUICKSTART);
        assertSingleMessage(installContext, "Please set the default virtual URI mapping; it was incorrectly reset by a previous update.", InstallContext.MessagePriority.warning);
    }

    public void testWarnsOnlyOnceIfDefaultUriIsQuickstartOnPublicAndTemplatesExistWhenUpdatingFromPre4_0_3() throws ModuleManagementException, RepositoryException {
        setupConfigProperty("/server/", "admin", "false");
        // fake a pre-install:
        setupConfigProperty("/server/filters/servlets/", "foo", "bar");
        setupConfigProperty("/modules/adminInterface/config/menu/security/usersSystem", "onclick", "MgnlAdminCentral.showTree('users', '/system', true)");
        setupConfigProperty("/modules/adminInterface/config/menu/security/usersAdmin", "onclick", "MgnlAdminCentral.showTree('users', '/admin', true)");

        // fake old users tree config
        setupConfigProperty("/modules/adminInterface/trees/users", "class", "info.magnolia.module.admininterface.AdminTreeMVCHandler");
        setupConfigProperty("/modules/adminInterface/trees/userSystem", "class", "info.magnolia.module.admininterface.AdminTreeMVCHandler");
        setupConfigProperty("/modules/adminInterface/trees/userAdmin", "class", "info.magnolia.module.admininterface.AdminTreeMVCHandler");

        // fake website tree
        setupConfigNode("/modules/adminInterface/trees/website");
        // fake website commands
        setupConfigNode("/modules/adminInterface/commands/website");

        setupDummyTemplate();

        setupExistingDefaultUriMapping(QUICKSTART);

        final InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.0.2"));

        checkDefaultUriMapping(QUICKSTART);
        assertSingleMessage(installContext, "Please set the default virtual URI mapping; it was incorrectly reset by a previous update.", InstallContext.MessagePriority.warning);
    }

    public void testReplaceWrongNodeTypeForDialogsOnUpdateFrom410() throws Exception {
        // fake a pre-install
        setupConfigProperty("/modules/adminInterface/config/menu/security/usersSystem", "onclick", "MgnlAdminCentral.showTree('users', '/system', true)");
        setupConfigProperty("/modules/adminInterface/config/menu/security/usersAdmin", "onclick", "MgnlAdminCentral.showTree('users', '/admin', true)");

        // fake old users tree config
        setupConfigProperty("/modules/adminInterface/trees/users", "class", "info.magnolia.module.admininterface.AdminTreeMVCHandler");
        setupConfigProperty("/modules/adminInterface/trees/userSystem", "class", "info.magnolia.module.admininterface.AdminTreeMVCHandler");
        setupConfigProperty("/modules/adminInterface/trees/userAdmin", "class", "info.magnolia.module.admininterface.AdminTreeMVCHandler");

        // fake website tree
        setupConfigNode("/modules/adminInterface/trees/website");
        // fake website commands
        setupConfigNode("/modules/adminInterface/commands/website");

        // setup some dialogs
        final String path = "/modules/myModule/dialogs/myDialog";
        final String path2 = "/modules/myModule/dialogs/anotherDialog";
        final String path3 = "/modules/anotherModule/dialogs/aCorrectDialog";
        final String path4 = "/modules/anotherModule/foo/leaveMeAlone";
        setupConfigNode(path);
        setupConfigProperty(path, "class", "do.not.touch.Me");
        setupConfigNode(path2);
        //these two shouldn't be updated
        setupProperty("config", path3, null, null, ItemType.CONTENTNODE);
        setupConfigNode(path4);

        HierarchyManager hm = MgnlContext.getHierarchyManager("config");

        final String pathUUID = hm.getContent(path).getUUID();
        final String path2UUID = hm.getContent(path2).getUUID();
        final String path3UUID = hm.getContent(path3).getUUID();

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.1"));

        final String expectedNodeType = "mgnl:contentNode";
        assertTrue(hm.isExist(path));
        //as we're replacing the old node, after updating we expect the same uuid
        assertEquals(pathUUID, hm.getContent(path).getUUID());
        assertEquals(expectedNodeType, getNodeType(path));
        assertEquals("do.not.touch.Me", hm.getContent(path).getNodeData("class").getString());

        assertTrue(hm.isExist(path2));
        assertEquals(path2UUID, hm.getContent(path2).getUUID());
        assertEquals(expectedNodeType, getNodeType(path2));

        assertTrue(hm.isExist(path3));
        assertEquals(path3UUID, hm.getContent(path3).getUUID());
        assertEquals(expectedNodeType, getNodeType(path3));

        assertTrue(hm.isExist(path4));
        assertEquals("mgnl:content", getNodeType(path4));

    }

    private void setupDummyTemplate() throws RepositoryException {
        final HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        final Content content = ContentUtil.createPath(hm, "/modules/foobar/templates");
        final Content fakeTemplateNode = content.createContent("baz", ItemType.CONTENTNODE);
        NodeDataUtil.getOrCreateAndSet(fakeTemplateNode, "visible", "true");
        hm.save();
    }

    private void setupExistingDefaultUriMapping(String toURI) throws RepositoryException {
        setupConfigProperty("/modules/adminInterface/virtualURIMapping/default/", "toURI", toURI);
        setupConfigProperty("/modules/adminInterface/virtualURIMapping/default/", "fromURI", "/");
        setupConfigProperty("/modules/adminInterface/virtualURIMapping/default/", "class", DefaultVirtualURIMapping.class.getName());
    }

    private void checkDefaultUriMapping(String expectedValue) throws RepositoryException {
        assertConfig(expectedValue, "/modules/adminInterface/virtualURIMapping/default/toURI");
        assertConfig("/", "/modules/adminInterface/virtualURIMapping/default/fromURI");
        assertConfig(DefaultVirtualURIMapping.class.getName(),"/modules/adminInterface/virtualURIMapping/default/class");
    }

    private String getNodeType(String path) throws RepositoryException {
        return MgnlContext.getHierarchyManager("config").getContent(path).getNodeData("jcr:primaryType").getString();
    }
}
