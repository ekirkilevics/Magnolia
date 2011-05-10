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
package info.magnolia.module;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.logging.AuditLoggingManager;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.RepositoryDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.reader.BetwixtModuleDefinitionReader;
import info.magnolia.module.model.reader.DependencyChecker;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import info.magnolia.module.model.reader.ModuleDependencyException;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * A base class for testing implementations of ModuleVersionHandler.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class ModuleVersionHandlerTestCase extends RepositoryTestCase {

    @Override
    protected void setUp() throws Exception {
        //        ComponentsTestUtil.setInstance(ModuleRegistry.class, new ModuleRegistryImpl());
        //        assertNotNull(Components.getComponent(ModuleRegistry.class));
        super.setUp();
        // this should disable observation for audit logging (mgnl-beans.properties registers a path for this component)
        ComponentsTestUtil.setInstance(AuditLoggingManager.class, new AuditLoggingManager());
    }

    @Override
    protected List<ModuleDefinition> getModuleDefinitionsForTests() throws ModuleManagementException {
        final BetwixtModuleDefinitionReader reader = new BetwixtModuleDefinitionReader();
        final List<ModuleDefinition> modules = new ArrayList<ModuleDefinition>();
        final List<String> moduleDescriptorPaths = getModuleDescriptorPathsForTests();
        for (String moduleDescriptorPath : moduleDescriptorPaths) {
            final ModuleDefinition module = reader.readFromResource(moduleDescriptorPath);
            modules.add(module);
        }
        return modules;
    }

    protected List<String> getModuleDescriptorPathsForTests() {
        return Collections.singletonList(getModuleDescriptorPath());
    }

    /**
     * A helper method to quickly set up a few properties to simulate a given environment.
     * Could be advantageously replaced by a dsl-like api, see MAGNOLIA-2828.
     * @param itemType an instance of {@link ItemType}. If <code>null</code>, defaults to {@link ItemType#CONTENT}
     */
    protected void setupProperty(final String workspace, String path, String propertyName, String value, ItemType itemType) throws RepositoryException {
        final HierarchyManager hm = MgnlContext.getHierarchyManager(workspace);
        final Content content = ContentUtil.createPath(hm, path, itemType != null ? itemType : ItemType.CONTENT);
        if (propertyName != null) {
            NodeDataUtil.getOrCreateAndSet(content, propertyName, value);
        }
        hm.save();
    }

    /**
     * Helper to set a property in the config workspace.
     * @see #setupProperty(String, String, String, String, ItemType)
     */
    protected void setupConfigProperty(String path, String propertyName, String value) throws RepositoryException {
        setupProperty(ContentRepository.CONFIG, path, propertyName, value, null);
    }

    /**
     * Helper to set a property in the config workspace.
     * @see #setupProperty(String, String, String, String, ItemType)
     */
    protected void setupNode(String workspace, String path) throws RepositoryException {
        setupProperty(workspace, path, null, null, null);
    }

    /**
     * Helper to set a property in the config workspace.
     * @see #setupProperty(String, String, String, String, ItemType)
     */
    protected void setupNode(String workspace, String path, ItemType type) throws RepositoryException {
        setupProperty(workspace, path, null, null, type);
    }

    /**
     * Helper to create an empty node.
     * @see #setupProperty(String, String, String, String, ItemType)
     */
    protected void setupConfigNode(String path) throws RepositoryException {
        setupNode(ContentRepository.CONFIG, path);
    }

    /**
     * Helper to create an empty node.
     * @see #setupProperty(String, String, String, String, ItemType)
     */
    protected void setupConfigNode(String path, ItemType type) throws RepositoryException {
        setupNode(ContentRepository.CONFIG, path, type);
    }

    /**
     * Asserts that a property at the given path as the expected value.
     */
    protected void assertConfig(String expectedValue, String path) throws RepositoryException {
        assertEquals(expectedValue, MgnlContext.getHierarchyManager("config").getNodeData(path).getString());
    }

    protected void assertNoMessages(InstallContext ctx) {
        assertTrue(ctx.getMessages().isEmpty());
    }

    /**
     * Asserts that the install context contains one single message, with the expected contents and priority.
     */
    protected void assertSingleMessage(InstallContext installContext, String expectedMessage, InstallContext.MessagePriority expectedPriority) {
        final Map<String, List<InstallContext.Message>> messages = installContext.getMessages();
        assertEquals(1, messages.size());
        final List<InstallContext.Message> messagesForModule = messages.values().iterator().next();
        assertEquals(1, messagesForModule.size());
        final InstallContext.Message msg = messagesForModule.get(0);
        assertEquals(expectedMessage, msg.getMessage());
        assertEquals(expectedPriority, msg.getPriority());
    }

    /**
     * This essentially fakes calls to ModuleManager and ensures only the ModuleVersionHandler under test
     * is in use.
     * Returns the InstallContext so one can further assert the state of the install/update (status, messages, ...)
     *
     * It's likely that this will need improvements when we want to test ModuleVersionHandler which
     * use IsModuleInstalledOrRegistered tasks, for example.
     */
    protected InstallContext executeUpdatesAsIfTheCurrentlyInstalledVersionWas(final Version currentlyInstalledVersion) throws ModuleManagementException {
        final BetwixtModuleDefinitionReader reader = new BetwixtModuleDefinitionReader();
        final ModuleDefinition moduleDefinition = reader.readFromResource(getModuleDescriptorPath());

        final String[] extraWorkspaces = getExtraWorkspaces();
        final String nodeTypeFile = getExtraNodeTypes();
        if (extraWorkspaces.length > 0 || nodeTypeFile != null) {
            final RepositoryDefinition repo = new RepositoryDefinition();
            repo.setName("magnolia");
            for (String wsName : extraWorkspaces) {
                repo.addWorkspace(wsName);
            }
            repo.setNodeTypeFile(nodeTypeFile);
            moduleDefinition.addRepository(repo);
        }

        final ModuleDefinitionReader readerMock = createStrictMock(ModuleDefinitionReader.class);
        expect(readerMock.readAll()).andReturn(Collections.singletonMap(moduleDefinition.getName(), moduleDefinition));
        replay(readerMock);

        final ModuleVersionHandler versionHandlerUnderTest = newModuleVersionHandlerForTests();
        final ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        final DependencyChecker depCheck = new NullDependencyChecker();
        final InstallContextImpl ctx = new InstallContextImpl(moduleRegistry) {
            @Override
            public void error(String message, Throwable th) {
                th.printStackTrace();
                // let's fail the test if we encounter a logged error, because ModuleManagerImpl.applyDeltas() swallows TaskExecutionException and RuntimeException and logs errors instead
                fail(message);
            }
        };
        final ModuleManagerImpl mm = new ModuleManagerImpl(ctx, readerMock, moduleRegistry, depCheck) {
            @Override
            protected ModuleVersionHandler newVersionHandler(ModuleDefinition module) {
                assertEquals("this test doesn't behave as expected", moduleDefinition, module);
                // wrap and delegate so that we control getCurrentlyInstalled()
                return new ModuleVersionHandler() {
                    @Override
                    public Version getCurrentlyInstalled(InstallContext ctx) {
                        return currentlyInstalledVersion;
                    }

                    @Override
                    public List<Delta> getDeltas(InstallContext installContext, Version from) {
                        return versionHandlerUnderTest.getDeltas(installContext, from);
                    }

                    @Override
                    public Delta getStartupDelta(InstallContext installContext) {
                        return versionHandlerUnderTest.getStartupDelta(installContext);
                    }
                };
            }
        };

        final List<ModuleDefinition> definitions = mm.loadDefinitions();
        assertEquals(1, definitions.size());
        assertEquals(moduleDefinition, definitions.get(0));

        mm.checkForInstallOrUpdates();
        assertTrue(mm.getStatus().needsUpdateOrInstall());

        mm.performInstallOrUpdate();
        assertEquals(InstallStatus.installDone, mm.getInstallContext().getStatus());

        verify(readerMock);

        return ctx;
    }

    /**
     * Extend this method if you need more workspaces than the default ones.
     * This can be useful if your MVH adds content to workspaces registered by
     * a module it depends upon.
     * Be aware that this is used in such a way that the ModuleDefinition of the
     * module under test will be modified to register those repositories itself.
     */
    protected String[] getExtraWorkspaces() {
        return new String[]{};
    }

    /**
     * Extend this method if you need more node types than the default ones.
     * This can be useful if your MVH needs node types registered by a module
     * it depends upon.
     * Be aware that this is used in such a way that the ModuleDefinition of the
     * module under test will be modified to register those repositories itself.
     * @return the path to the node type definition resource, as found in a module descriptor
     */
    protected String getExtraNodeTypes() {
        return null;
    }

    protected abstract String getModuleDescriptorPath();

    protected abstract ModuleVersionHandler newModuleVersionHandlerForTests();

    private static class NullDependencyChecker implements DependencyChecker {
        @Override
        public void checkDependencies(Map<String, ModuleDefinition> moduleDefinitions) throws ModuleDependencyException {
            // do nothing
        }

        @Override
        public List<ModuleDefinition> sortByDependencyLevel(Map<String, ModuleDefinition> moduleDefinitions) {
            return new ArrayList<ModuleDefinition>(moduleDefinitions.values());
        }
    }
}
