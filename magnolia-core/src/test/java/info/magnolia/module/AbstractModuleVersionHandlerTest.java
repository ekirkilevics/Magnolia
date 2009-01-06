/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.ModuleFilesExtraction;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AbstractModuleVersionHandlerTest extends TestCase {
    private Delta d1;
    private Delta d2;
    private Delta d3;
    private Delta d4;
    private Delta d5;
    private AbstractModuleVersionHandler versionHandler;

    protected void setUp() throws Exception {
        super.setUp();
        d1 = createNiceMock(Delta.class);
        d2 = createNiceMock(Delta.class);
        d3 = createNiceMock(Delta.class);
        d4 = createNiceMock(Delta.class);
        d5 = createNiceMock(Delta.class);
        expect(d1.getVersion()).andReturn(Version.parseVersion("1.0.0"));
        expect(d2.getVersion()).andReturn(Version.parseVersion("1.0.1"));
        expect(d3.getVersion()).andReturn(Version.parseVersion("1.1"));
        expect(d4.getVersion()).andReturn(Version.parseVersion("1.2"));
        expect(d5.getVersion()).andReturn(Version.parseVersion("1.3"));
        expect(d1.getTasks()).andReturn(new ArrayList());
        expect(d2.getTasks()).andReturn(new ArrayList());
        expect(d3.getTasks()).andReturn(new ArrayList());
        expect(d4.getTasks()).andReturn(new ArrayList());
        expect(d5.getTasks()).andReturn(new ArrayList());
        expect(d1.getConditions()).andReturn(new ArrayList());
        expect(d2.getConditions()).andReturn(new ArrayList());
        expect(d3.getConditions()).andReturn(new ArrayList());
        expect(d4.getConditions()).andReturn(new ArrayList());
        expect(d5.getConditions()).andReturn(new ArrayList());
        replay(d1, d2, d3, d4, d5);
        versionHandler = newTestModuleVersionHandler();
        versionHandler.register(d1);
        versionHandler.register(d2);
        versionHandler.register(d3);
        versionHandler.register(d4);
        versionHandler.register(d5);
    }

    public void testCantRegisterMultipleDeltasForSameVersion() {
        final Delta d1 = DeltaBuilder.update(Version.parseVersion("1.0.0"), "", new NullTask("", ""));
        final Delta d2 = DeltaBuilder.update(Version.parseVersion("1.0.0"), "", new NullTask("", ""));
        final AbstractModuleVersionHandler versionHandler = newTestModuleVersionHandler();
        versionHandler.register(d1);
        try {
            versionHandler.register(d2);
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("Version 1.0.0 was already registered in this ModuleVersionHandler.", e.getMessage());
        }
    }

    public void testRetrievesTheAppropriateListOfDeltas() {
        final List deltas = versionHandler.getDeltas(makeInstallContext("1.3"), Version.parseVersion("1.0.1"));
        assertEquals(3, deltas.size());
        assertEquals(d3, deltas.get(0));
        assertEquals(d4, deltas.get(1));
        assertEquals(d5, deltas.get(2));
    }

    public void testHasExtraDeltaIfVersionBeingInstalledIsNewerThanLatestRegisteredDelta() {
        final List deltas = versionHandler.getDeltas(makeInstallContext("1.5"), Version.parseVersion("1.0.1"));
        assertEquals(4, deltas.size());
        assertEquals(d3, deltas.get(0));
        assertEquals(d4, deltas.get(1));
        assertEquals(d5, deltas.get(2));
        assertDefaultUpdateDelta((Delta) deltas.get(3));
    }

    public void testRetrievesTheAppropriateDeltaForIntermediateUnregisteredVersion() {
        final List deltas = versionHandler.getDeltas(makeInstallContext("1.5"), Version.parseVersion("1.2.5"));
        assertEquals(2, deltas.size());
        assertEquals(d5, deltas.get(0));
        assertDefaultUpdateDelta((Delta) deltas.get(1));
    }

    public void testReturnsDefaultUpdateDeltaIfNoDeltaWasRegisteredForNewerVersion() {
        final List deltas = versionHandler.getDeltas(makeInstallContext("1.5"), Version.parseVersion("1.4"));
        assertNotNull(deltas);
        assertEquals(1, deltas.size());
        final Delta d = (Delta) deltas.get(0);
        assertDefaultUpdateDelta(d);
    }

    public void testReturnsEmptyListIfLatestDeltaWasRegisteredForCurrentVersion() {
        final List deltas = versionHandler.getDeltas(makeInstallContext("1.3"), Version.parseVersion("1.3"));
        assertNotNull(deltas);
        assertEquals(0, deltas.size());
    }

    public void testReturnsEmptyListIfCurrentVersionIsInstalledVersion() {
        final List deltas = versionHandler.getDeltas(makeInstallContext("1.5"), Version.parseVersion("1.5"));
        assertEquals(0, deltas.size());
    }

    public void testReturnsDefaultUpdateDeltaIfNoDeltaWasRegisteredAtAll() {
        final AbstractModuleVersionHandler versionHandler = newTestModuleVersionHandler();
        final List deltas = versionHandler.getDeltas(makeInstallContext("1.5"), Version.parseVersion("1.0.1"));
        assertNotNull(deltas);
        assertEquals(1, deltas.size());
        assertDefaultUpdateDelta((Delta) deltas.get(0));
    }

    private void assertDefaultUpdateDelta(Delta d) {
        assertEquals(0, d.getConditions().size());
        final List tasks = d.getTasks();
        assertEquals(2, tasks.size());
        assertTrue(tasks.get(0) instanceof ModuleFilesExtraction);
        assertTrue(tasks.get(1) instanceof AbstractModuleVersionHandler.ModuleVersionUpdateTask);
    }

    public void testDeltasAreSorted() {
        // yes, this test might pass by accident.
        final List deltas = versionHandler.getDeltas(makeInstallContext("0.5"), Version.parseVersion("0.4"));
        assertEquals(6, deltas.size());
        assertEquals(d1, deltas.get(0));
        assertEquals(d2, deltas.get(1));
        assertEquals(d3, deltas.get(2));
        assertEquals(d4, deltas.get(3));
        assertEquals(d5, deltas.get(4));
        assertDefaultUpdateDelta((Delta) deltas.get(5));
    }

    public void testVersionUpdateTaskAndFileExtractionAreAdded() {
        final NullTask nullTask = new NullTask("test", "test");
        final NullTask nullTask2 = new NullTask("test2", "test2");
        final Delta delta = DeltaBuilder.update(Version.parseVersion("2.0"), "").addTask(nullTask).addTask(nullTask2);

        final AbstractModuleVersionHandler versionHandler = newTestModuleVersionHandler();
        versionHandler.register(delta);

        final List retrievedDeltas = versionHandler.getDeltas(makeInstallContext("2.0"), Version.parseVersion("1.0"));
        assertEquals(1, retrievedDeltas.size());
        final Delta retrievedDelta = (Delta) retrievedDeltas.get(0);
        final List tasks = retrievedDelta.getTasks();
        assertEquals(4, tasks.size());
        // in our test, the first 2 tasks should be NullTask instances.
        for (int i = 0; i < 2; i++) {
            assertTrue(tasks.get(i) instanceof NullTask);
        }
        assertTrue(tasks.get(2) instanceof ModuleFilesExtraction);
        assertTrue(tasks.get(3) instanceof AbstractModuleVersionHandler.ModuleVersionUpdateTask);
    }

    public void testStoresTheModuleDescriptorVersionOnUpdateOfVersionThatDoesNotHaveSpecificDeltaAndIsSnapshot() {
        doTestStoresModuleDescriptorVersion("2.2-SNAPSHOT", Version.parseVersion("1.0"));
    }
    
    public void testStoresTheModuleDescriptorVersionOnUpdateOfVersionThatDoesNotHaveSpecificDelta() {
        doTestStoresModuleDescriptorVersion("2.2", Version.parseVersion("1.0"));
    }

    public void testStoresTheModuleDescriptorVersionOnUpdateOfVersionThatHasSpecificDelta() {
        doTestStoresModuleDescriptorVersion("2.0", Version.parseVersion("1.0"));
    }

//    public void testStoresTheModuleDescriptorVersionOnUpdateOfVersionThatHasSpecificDeltaButIsSnapshot() {
//        doTestStoresModuleDescriptorVersion("2.0-SNAPSHOT", Version.parseVersion("1.0"));
//    }

    public void testStoresTheModuleDescriptorVersionOnInstall() {
        doTestStoresModuleDescriptorVersion("2.2-SNAPSHOT", null);
    }

    private void doTestStoresModuleDescriptorVersion(String moduleDescriptorVersionStr, Version currentVersion) {
        final NullTask nullTask = new NullTask("test", "test");
        final NullTask nullTask2 = new NullTask("test2", "test2");
        final Delta delta = DeltaBuilder.update(Version.parseVersion("2.0"), "").addTask(nullTask).addTask(nullTask2);

        final AbstractModuleVersionHandler versionHandler = new AbstractModuleVersionHandler() {
            protected List getBasicInstallTasks(InstallContext installContext) {
                return Collections.EMPTY_LIST;
            }
        };
        versionHandler.register(delta);

        final Version moduleDescriptorVersion = Version.parseVersion(moduleDescriptorVersionStr);
        final InstallContext installContext = makeInstallContext(moduleDescriptorVersionStr);
        final List retrievedDeltas = versionHandler.getDeltas(installContext, currentVersion);
        final Delta lastDelta = (Delta) retrievedDeltas.get(retrievedDeltas.size() - 1);
        final List tasks = lastDelta.getTasks();
        final Task lastTask = (Task) tasks.get(tasks.size() - 1);
        assertTrue(lastTask instanceof AbstractModuleVersionHandler.ModuleVersionToLatestTask);
        assertEquals(moduleDescriptorVersion, ((AbstractModuleVersionHandler.ModuleVersionToLatestTask)lastTask).getVersion(installContext));
    }

    private AbstractModuleVersionHandler newTestModuleVersionHandler() {
        return new AbstractModuleVersionHandler() {
            protected List getBasicInstallTasks(InstallContext installContext) {
                throw new IllegalStateException("test not supposed to go here.");
            }
        };
    }

    private InstallContext makeInstallContext(String currentModuleCurrentVersion) {
        final InstallContextImpl ctx = new InstallContextImpl();
        ModuleDefinition mod = new ModuleDefinition("test", Version.parseVersion(currentModuleCurrentVersion), null, null);
        ctx.setCurrentModule(mod);
        return ctx;
    }

    private final static class NullTask extends AbstractTask {
        public NullTask(String name, String description) {
            super(name, description);
        }

        public void execute(InstallContext installContext) {
        }
    }

}
