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
package info.magnolia.module;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.logging.AuditLoggingManager;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.reader.BetwixtModuleDefinitionReader;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.*;

/**
 * A base class for testing implementations of ModuleVersionHandler.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class ModuleVersionHandlerTestCase extends RepositoryTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        // this should disable observation for audit logging (mgnl-beans.properties registers a path for this componenent)
        FactoryUtil.setInstance(AuditLoggingManager.class, new AuditLoggingManager());
    }

    /**
     * A helper method to quickly set up a few properties to simulate a given environment.
     * Could be advantageously replaced by a dsl-like api, see MAGNOLIA-2828.
     */
    protected void setupProperty(final String workspace, String path, String propertyName, String value) throws RepositoryException {
        final HierarchyManager hm = MgnlContext.getHierarchyManager(workspace);
        final Content content = ContentUtil.createPath(hm, path);
        NodeDataUtil.getOrCreateAndSet(content, propertyName, value);
        hm.save();
    }

    /**
     * This essentially fakes calls to ModuleManager and ensures only the ModuleVersionHandler under test
     * is in use.
     * It's likely that this will need improvements when we want to test ModuleVersionHandler which
     * use IsModuleInstalledOrRegistered tasks, for example.
     */
    protected void executeUpdatesAsIfTheCurrentlyInstalledVersionWas(final Version currentlyInstalledVersion) throws ModuleManagementException {
        final BetwixtModuleDefinitionReader reader = new BetwixtModuleDefinitionReader();
        final ModuleDefinition moduleDefinition = reader.readFromResource(getModuleDescriptorPath());

        final ModuleDefinitionReader readerMock = createStrictMock(ModuleDefinitionReader.class);
        expect(readerMock.readAll()).andReturn(Collections.singletonMap(moduleDefinition.getName(), moduleDefinition));
        replay(readerMock);

        final ModuleVersionHandler versionHandlerUnderTest = newModuleVersionHandlerForTests();
        final InstallContextImpl ctx = new InstallContextImpl();
        final ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        final ModuleManagerImpl mm = new ModuleManagerImpl(ctx, readerMock, moduleRegistry) {
            protected ModuleVersionHandler newVersionHandler(ModuleDefinition module) {
                assertEquals("this test doesn't behave as expected", moduleDefinition, module);
                // wrap and delegate so that we control getCurrentlyInstalled()
                return new ModuleVersionHandler() {
                    public Version getCurrentlyInstalled(InstallContext ctx) {
                        return currentlyInstalledVersion;
                    }

                    public List getDeltas(InstallContext installContext, Version from) {
                        return versionHandlerUnderTest.getDeltas(installContext, from);
                    }

                    public Delta getStartupDelta(InstallContext installContext) {
                        return versionHandlerUnderTest.getStartupDelta(installContext);
                    }
                };
            }
        };

        final List<ModuleDefinition> defs = mm.loadDefinitions();
        assertEquals(1, defs.size());
        assertEquals(moduleDefinition, defs.get(0));

        mm.checkForInstallOrUpdates();
        assertTrue(mm.getStatus().needsUpdateOrInstall());

        mm.performInstallOrUpdate();
        assertEquals(InstallStatus.installDone, mm.getInstallContext().getStatus());

        verify(readerMock);
    }

    protected abstract String getModuleDescriptorPath();

    protected abstract ModuleVersionHandler newModuleVersionHandlerForTests();
}
