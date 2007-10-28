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
package info.magnolia.module;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.SystemContext;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import junit.framework.TestCase;
import static org.easymock.classextension.EasyMock.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleManagerImplTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
        FactoryUtil.setInstance(ModuleRegistry.class, new ModuleRegistryImpl());
        FactoryUtil.setInstance(SystemContext.class, createStrictMock(SystemContext.class));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        FactoryUtil.clear();
    }

    // TODO : assert saves after each module?
    // TODO : assert rollbs back with TaskExecutionException

    /**
     * TODO : should check that d1 is actually called before d2
     */
    public void testUpdateAppliesSuppliedDeltasAndTasks() throws Exception {
        final String newVersion = "2.3.4";

        final InstallContextImpl ctx = createStrictMock(InstallContextImpl.class);
        final ModuleDefinition mod = new ModuleDefinition("foo", newVersion, null, null);
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
        ctx.setCurrentModule(null);

        expect(d2.getTasks()).andReturn(Arrays.asList(t3, t4));
        expect(d1.getTasks()).andReturn(Arrays.asList(t1, t2));
        t1.execute(ctx);
        t2.execute(ctx);
        t3.execute(ctx);
        t4.execute(ctx);

        replay(ctx, d1, d2, t1, t2, t3, t4, moduleNode, versionProp, allModulesNode);

        final ModuleManager.ModuleAndDeltas moduleAndDeltas = new ModuleManager.ModuleAndDeltas(mod, fromVersion, Arrays.asList(d1, d2));
        new ModuleManagerImpl().installOrUpdateModule(moduleAndDeltas, ctx);

        verify(ctx, d1, d2, t1, t2, t3, t4, moduleNode, versionProp, allModulesNode);
    }

    public void testTaskExecutionExceptionInterruptsTasksAddsExplicitErrorMessage() throws TaskExecutionException {
        final ModuleDefinition mod = new ModuleDefinition("foo", "2.3.4", null, null);
        final InstallContextImpl ctx = createStrictMock(InstallContextImpl.class);
        final Delta d1 = createStrictMock(Delta.class);
        final Task t1 = createStrictMock(Task.class);
        final Task t2 = createStrictMock(Task.class);

        ctx.setCurrentModule(mod);
        expect(d1.getTasks()).andReturn(Arrays.asList(t1, t2));
        t1.execute(ctx);
        expectLastCall().andThrow(new TaskExecutionException("boo"));
        ctx.error(eq("Could not install or update module. Please remove or update faulty jar. (boo)"), isA(TaskExecutionException.class));
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
        final ModuleDefinition mod1 = new ModuleDefinition("abc", "2.3.4", null, null);
        final ModuleDefinition mod2 = new ModuleDefinition("xyz", "2.3.4", null, null);
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

        ctx.setCurrentModule(mod2);
        expect(mvh2.getCurrentlyInstalled(ctx)).andReturn(v123);
        expect(mvh2.getDeltas(ctx, v123)).andReturn(Collections.singletonList(d2));

        ctx.setCurrentModule(null);

        // during performInstallOrUpdate()
        ctx.setCurrentModule(mod1);
        expect(d1.getConditions()).andReturn(Arrays.asList(c1, c2));
        expect(c1.check(ctx)).andReturn(Boolean.FALSE);
        expect(c1.getDescription()).andReturn("Hi, please fix condition #1");
        ctx.info("Hi, please fix condition #1");
        expect(c2.check(ctx)).andReturn(Boolean.TRUE);
        ctx.setCurrentModule(mod2);
        expect(d2.getConditions()).andReturn(Arrays.asList(c3));
        expect(c3.check(ctx)).andReturn(Boolean.FALSE);
        expect(c3.getDescription()).andReturn("Hi, please fix condition #3 too");
        ctx.info("Hi, please fix condition #3 too");
        ctx.setCurrentModule(null);

        replay(modDefReader, ctx, mvh1, mvh2, d1, d2, c1, c2, c3);

        final ModuleManagerImpl moduleManager = new TestModuleManagerImpl(moduleVersionHandlers, ctx, modDefReader);
        moduleManager.loadDefinitions();
        moduleManager.checkForInstallOrUpdates();
        moduleManager.performInstallOrUpdate();
        assertEquals("Conditions failed, so we still need to update/install", true, moduleManager.getStatus().needsUpdateOrInstall());
        verify(modDefReader, ctx, mvh1, mvh2, d1, d2, c1, c2, c3);
    }

    private static final class TestModuleManagerImpl extends ModuleManagerImpl {
        private final Map<String, ModuleVersionHandler> moduleVersionHandlers;

        protected TestModuleManagerImpl(Map<String, ModuleVersionHandler> moduleVersionHandlers, InstallContextImpl installContext, ModuleDefinitionReader moduleDefinitionReader) {
            super(installContext, moduleDefinitionReader);
            this.moduleVersionHandlers = moduleVersionHandlers;
        }

        protected ModuleVersionHandler getVersionHandler(ModuleDefinition module) {
            return moduleVersionHandlers.get(module.getName());
        }
    }
}
