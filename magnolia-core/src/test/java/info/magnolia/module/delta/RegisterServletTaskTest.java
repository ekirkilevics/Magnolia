package info.magnolia.module.delta;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleManagerImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.ServletDefinition;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

public class RegisterServletTaskTest extends RepositoryTestCase {

    protected void setUp() throws Exception {
        ComponentsTestUtil.setInstance(ModuleRegistry.class, new ModuleRegistryImpl());
        ComponentsTestUtil.setInstance(ModuleManager.class, new ModuleManagerImpl());
        super.setUp();
        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager("config");
        ContentUtil.createPath(hm, "/server/filters/servlets", ItemType.CONTENT);
    }

    public void testRegisterServletTaskWithEmptyMappings() throws Exception {
        ServletDefinition sd = new ServletDefinition();
        sd.addMapping("");
        sd.setName("dummy");

        RegisterServletTask task = new RegisterServletTask(sd);
        InstallContextImpl ctx = new InstallContextImpl();
        ModuleDefinition module = new ModuleDefinition();
        module.setName("test");
        ctx.setCurrentModule(module);
        task.execute(ctx);
        assertEquals("Empty mappings configuration is not supported and sevlet was not installed.", ctx.getMessages().values().iterator().next().get(0)
                .getMessage());
    }

    public void testRegisterServletTaskWithMappings() throws Exception {
        ServletDefinition sd = new ServletDefinition();
        sd.addMapping(null);
        sd.setName("dummy");

        RegisterServletTask task = new RegisterServletTask(sd);
        InstallContextImpl ctx = new InstallContextImpl();
        ModuleDefinition module = new ModuleDefinition();
        module.setName("test");
        ctx.setCurrentModule(module);
        task.execute(ctx);
        assertEquals("Empty mappings configuration is not supported and sevlet was not installed.", ctx.getMessages().values().iterator().next().get(0)
                .getMessage());
    }
}