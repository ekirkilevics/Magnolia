/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
import info.magnolia.cms.core.NodeData;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.delta.WarnTask;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import junit.framework.TestCase;
import static org.easymock.classextension.EasyMock.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleManagerImplTest extends TestCase {
    private ModuleRegistry moduleRegistry;

    protected void setUp() throws Exception {
        super.setUp();
        moduleRegistry = new ModuleRegistryImpl();
        ComponentsTestUtil.setInstance(ModuleRegistry.class, moduleRegistry);
        ComponentsTestUtil.setInstance(SystemContext.class, createStrictMock(SystemContext.class));

        // shunt log4j
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    public void testCheckModuleAndDeltasToStringForUpdate() {
        final ModuleDefinition mod = new ModuleDefinition("foo", Version.parseVersion("2.3.4"), null, null);
        final Delta d1 = DeltaBuilder.update("1.1", "New version").addTask(new WarnTask("t1", "test 1")).addTask(new WarnTask("t2", "test 2"));
        final Delta d2 = DeltaBuilder.update("2.0", "New version 2").addTask(new WarnTask("t3", "test 3")).addTask(new WarnTask("t3", "test 4"));
        ModuleManager.ModuleAndDeltas mad = new ModuleManager.ModuleAndDeltas(mod, Version.parseVersion("1.0"), Arrays.asList(d1, d2));
        assertEquals("ModuleAndDeltas for foo: current version is 1.0.0, updating to 2.3.4 with 2 deltas.", mad.toString());
    }
    
    public void testCheckModuleAndDeltasToStringForInstall() {
        final ModuleDefinition mod = new ModuleDefinition("foo", Version.parseVersion("2.3.4"), null, null);
        final Delta d1 = DeltaBuilder.update("1.1", "New version").addTask(new WarnTask("t1", "test 1")).addTask(new WarnTask("t2", "test 2"));
        ModuleManager.ModuleAndDeltas mad = new ModuleManager.ModuleAndDeltas(mod, null, Arrays.asList(d1));
        assertEquals("ModuleAndDeltas for foo: installing version 2.3.4 with 1 deltas.", mad.toString());
    }

    // TODO : assert saves after each module?
    // TODO : assert rollbs back with TaskExecutionException

    /**
     * TODO : should check that d1 is actually called before d2
     *
     * TODO : since ContentRepository.getAllRepositoryNames() returns an empty iterator, we don't actually check the save operations
     */
    public void testUpdateAppliesSuppliedDeltasAndTasks() throws Exception {
        final String newVersion = "2.3.4";

        final InstallContextImpl ctx = createStrictMock(InstallContextImpl.class);
        final ModuleDefinition mod = new ModuleDefinition("foo", Version.parseVersion(newVersion), null, null);
        final Content allModulesNode = createStrictMock(Content.class);
        final Content moduleNode = createStrictMock(Content.class);
        final NodeData versionProp = createStrictMock(NodeData.class);
        final Delta d1 = createStrictMock(Delta.class);
        final Delta d2 = createStrictMock(Delta.class);
        final Task t1 = createStrictMock(Task.class);
        final Task t2 = createStrictMock(Task.class);
        final Task t3 = createStrictMock(Task.class);
        final Task t4 = createStrictMock(Task.class);

        final Version fromVersion = Version.parseVersion("1.2.3");

        ctx.setCurrentModule(mod);
        expect(d2.getTasks()).andReturn(Arrays.asList(t3, t4));
        expect(d1.getTasks()).andReturn(Arrays.asList(t1, t2));
        t1.execute(ctx);
        ctx.incExecutedTaskCount();
        t2.execute(ctx);
        ctx.incExecutedTaskCount();
        t3.execute(ctx);
        ctx.incExecutedTaskCount();
        t4.execute(ctx);
        ctx.incExecutedTaskCount();
        ctx.setCurrentModule(null);

        replay(ctx, d1, d2, t1, t2, t3, t4, moduleNode, versionProp, allModulesNode);

        final ModuleManager.ModuleAndDeltas moduleAndDeltas = new ModuleManager.ModuleAndDeltas(mod, fromVersion, Arrays.asList(d1, d2));
        new ModuleManagerImpl(null,null,null,null).installOrUpdateModule(moduleAndDeltas, ctx);

        verify(ctx, d1, d2, t1, t2, t3, t4, moduleNode, versionProp, allModulesNode);
    }

    public void testTaskExecutionExceptionInterruptsTasksAddsExplicitErrorMessage() throws TaskExecutionException {
        final ModuleDefinition mod = new ModuleDefinition("foo", Version.parseVersion("2.3.4"), null, null);
        final InstallContextImpl ctx = createStrictMock(InstallContextImpl.class);
        final Delta d1 = createStrictMock(Delta.class);
        final Task t1 = createStrictMock(Task.class);
        final Task t2 = createStrictMock(Task.class);

        ctx.setCurrentModule(mod);
        expect(d1.getTasks()).andReturn(Arrays.asList(t1, t2));
        t1.execute(ctx);
        expectLastCall().andThrow(new TaskExecutionException("boo"));
        expect(t1.getName()).andReturn("task#1").anyTimes();
        ctx.error(eq("Could not install or update foo module. Task 'task#1' failed. (TaskExecutionException: boo)"), isA(TaskExecutionException.class));
        ctx.setCurrentModule(null);

        replay(ctx, d1, t1, t2);

        final ModuleManager.ModuleAndDeltas moduleAndDeltas = new ModuleManager.ModuleAndDeltas(mod, Version.parseVersion("1.2.3"), Arrays.asList(d1));
        new ModuleManagerImpl().installOrUpdateModule(moduleAndDeltas, ctx);

        verify(ctx, d1, t1, t2);
    }

    public void testFailedConditionsPreventsFurtherModulesToBeInstalledOrUpdated() throws TaskExecutionException, ModuleManagementException {
        final ModuleDefinitionReader modDefReader = createStrictMock(ModuleDefinitionReader.class);
        final InstallContextImpl ctx = createStrictMock(InstallContextImpl.class);
        final ModuleVersionHandler mvh1 = createStrictMock(ModuleVersionHandler.class);
        final ModuleVersionHandler mvh2 = createStrictMock(ModuleVersionHandler.class);
        final Delta d1 = createStrictMock(Delta.class);
        final Delta d2 = createStrictMock(Delta.class);
        final Condition c1 = createStrictMock(Condition.class);
        final Condition c2 = createStrictMock(Condition.class);
        final Condition c3 = createStrictMock(Condition.class);
        final Task t1 = createStrictMock(Task.class);
        final Task t2 = createStrictMock(Task.class);
        final ModuleDefinition mod1 = new ModuleDefinition("abc", Version.parseVersion("2.3.4"), null, null);
        final ModuleDefinition mod2 = new ModuleDefinition("xyz", Version.parseVersion("2.3.4"), null, null);
        final Map modMap = new HashMap();
        modMap.put("abc", mod1);
        modMap.put("xyz", mod2);
        final Map<String, ModuleVersionHandler> moduleVersionHandlers = new HashMap<String, ModuleVersionHandler>();
        moduleVersionHandlers.put("abc", mvh1);
        moduleVersionHandlers.put("xyz", mvh2);
        final Version v123 = Version.parseVersion("1.2.3");

        // loading defs
        expect(modDefReader.readAll()).andReturn(modMap);

        // during checkForInstallOrUpdates()
        ctx.setCurrentModule(mod1);
        expect(mvh1.getCurrentlyInstalled(ctx)).andReturn(v123);
        expect(mvh1.getDeltas(ctx, v123)).andReturn(Collections.singletonList(d1));
        expect(d1.getTasks()).andReturn(Collections.singletonList(t1));

        ctx.setCurrentModule(mod2);
        expect(mvh2.getCurrentlyInstalled(ctx)).andReturn(v123);
        expect(mvh2.getDeltas(ctx, v123)).andReturn(Collections.singletonList(d2));
        expect(d2.getTasks()).andReturn(Collections.singletonList(t2));

        ctx.setCurrentModule(null);
        ctx.setTotalTaskCount(2);

        // during performInstallOrUpdate()
        expect(ctx.getStatus()).andReturn(null);
        ctx.setStatus(InstallStatus.inProgress);
        ctx.setCurrentModule(mod1);
        expect(d1.getConditions()).andReturn(Arrays.asList(c1, c2));
        expect(c1.check(ctx)).andReturn(Boolean.FALSE);
        expect(c1.getDescription()).andReturn("Hi, please fix condition #1");
        ctx.warn("Hi, please fix condition #1");
        expect(c2.check(ctx)).andReturn(Boolean.TRUE);
        ctx.setCurrentModule(mod2);
        expect(d2.getConditions()).andReturn(Arrays.asList(c3));
        expect(c3.check(ctx)).andReturn(Boolean.FALSE);
        expect(c3.getDescription()).andReturn("Hi, please fix condition #3 too");
        ctx.warn("Hi, please fix condition #3 too");
        ctx.setCurrentModule(null);
        ctx.setStatus(InstallStatus.stoppedConditionsNotMet);

        replay(modDefReader, ctx, mvh1, mvh2, d1, d2, c1, c2, c3, t1, t2);

        final ModuleManagerImpl moduleManager = new TestModuleManagerImpl(moduleVersionHandlers, ctx, modDefReader);
        moduleManager.loadDefinitions();
        moduleManager.checkForInstallOrUpdates();
        moduleManager.performInstallOrUpdate();
        assertEquals("Conditions failed, so we still need to update/install", true, moduleManager.getStatus().needsUpdateOrInstall());
        verify(modDefReader, ctx, mvh1, mvh2, d1, d2, c1, c2, c3, t1, t2);
    }

    public void testPerformCantBeCalledTwiceByDifferentThreads() throws Exception {
        final ModuleDefinitionReader modDefReader = createStrictMock(ModuleDefinitionReader.class);
        final InstallContextImpl ctx = new InstallContextImpl(moduleRegistry);
        final ModuleVersionHandler mvh1 = createStrictMock(ModuleVersionHandler.class);
        final ModuleVersionHandler mvh2 = createStrictMock(ModuleVersionHandler.class);
        final Task t1 = new AbstractTask("sleep", "sleeeeep") {
            public void execute(InstallContext installContext) throws TaskExecutionException {
                installContext.info("t1 executing");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    fail("can't test ... :(");
                }
                installContext.info("t1 executed");
            }
        };

        final Task t2 = createStrictMock(Task.class);
        final Delta d1 = DeltaBuilder.update(Version.parseVersion("1.0"), "", t1);
        final Delta d2 = DeltaBuilder.update(Version.parseVersion("2.0"), "", t2);

        final ModuleDefinition mod1 = new ModuleDefinition("abc", Version.parseVersion("2.3.4"), null, null);
        final ModuleDefinition mod2 = new ModuleDefinition("xyz", Version.parseVersion("2.3.4"), null, null);
        final Map modMap = new HashMap();
        modMap.put("abc", mod1);
        modMap.put("xyz", mod2);
        final Map<String, ModuleVersionHandler> moduleVersionHandlers = new HashMap<String, ModuleVersionHandler>();
        moduleVersionHandlers.put("abc", mvh1);
        moduleVersionHandlers.put("xyz", mvh2);
        final Version v123 = Version.parseVersion("1.2.3");

        // loading defs
        expect(modDefReader.readAll()).andReturn(modMap);

        // during checkForInstallOrUpdates()
        expect(mvh1.getCurrentlyInstalled(ctx)).andReturn(v123);
        expect(mvh1.getDeltas(ctx, v123)).andReturn(Collections.singletonList(d1));
        expect(mvh2.getCurrentlyInstalled(ctx)).andReturn(v123);
        expect(mvh2.getDeltas(ctx, v123)).andReturn(Collections.singletonList(d2));

        // during performInstallOrUpdate()
        t2.execute(ctx);

        replay(modDefReader, mvh1, mvh2, t2);

        final ModuleManagerImpl moduleManager = new TestModuleManagerImpl(moduleVersionHandlers, ctx, modDefReader);
        moduleManager.loadDefinitions();
        moduleManager.checkForInstallOrUpdates();
        performInstallOrUpdateInThread(moduleManager, false);
        performInstallOrUpdateInThread(moduleManager, true);
        Thread.sleep(800);
        assertEquals(false, moduleManager.getStatus().needsUpdateOrInstall());
        assertEquals(InstallStatus.installDone, ctx.getStatus());
        assertEquals(1, ctx.getMessages().size());
        final List msgs = (List) ctx.getMessages().get(mod1.toString());
        assertEquals(2, msgs.size());
        assertEquals("t1 executing", ((InstallContext.Message) msgs.get(0)).getMessage());
        assertEquals("t1 executed", ((InstallContext.Message) msgs.get(1)).getMessage());
        verify(modDefReader, mvh1, mvh2, t2);
    }

    private void performInstallOrUpdateInThread(final ModuleManagerImpl moduleManager, final boolean shouldFail) {
        final Runnable runnable = new Runnable() {
            public void run() {
                try {
                    moduleManager.performInstallOrUpdate();
                    if (shouldFail) {
                        fail("should have failed");
                    }
                } catch (IllegalStateException e) {
                    if (shouldFail) {
                        assertEquals("ModuleManager.performInstallOrUpdate() was already started !", e.getMessage());
                    } else {
                        throw e;
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    private static final class TestModuleManagerImpl extends ModuleManagerImpl {
        private final Map<String, ModuleVersionHandler> moduleVersionHandlers;

        protected TestModuleManagerImpl(Map<String, ModuleVersionHandler> moduleVersionHandlers, InstallContextImpl installContext, ModuleDefinitionReader moduleDefinitionReader) {
            super(installContext, moduleDefinitionReader);
            this.moduleVersionHandlers = moduleVersionHandlers;
        }

        protected ModuleVersionHandler newVersionHandler(ModuleDefinition module) {
            return moduleVersionHandlers.get(module.getName());
        }
    }
}
