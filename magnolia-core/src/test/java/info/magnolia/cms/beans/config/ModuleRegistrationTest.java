package info.magnolia.cms.beans.config;

import info.magnolia.cms.module.DependencyDefinition;
import info.magnolia.cms.module.ModuleDefinition;
import info.magnolia.cms.util.FactoryUtil;
import junit.framework.TestCase;

import java.io.File;
import java.net.URL;

/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class ModuleRegistrationTest extends TestCase {
    private static final File MGNL_CORE_ROOT = new File(ModuleRegistrationTest.class.getResource("/testresource.txt").getFile()).
            getParentFile().getParentFile().getParentFile();

    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception {
        FactoryUtil.setDefaultImplementation(ModuleRegistration.class, ModuleRegistration.class.getName());
        super.setUp();
    }

    public void testGetModuleRootJar() {
        URL xmlUrl = getClass().getResource("/testjar.jar");
        File moduleRoot = new ModuleRegistration().getModuleRoot(xmlUrl);

        assertNotNull("Unable to find jar", moduleRoot);
        assertTrue("Jar " + moduleRoot.getAbsolutePath() + " does not exist", moduleRoot.exists());
        assertEquals(MGNL_CORE_ROOT, moduleRoot);
    }

    public void testGetModuleRootJarWIthDot() {
        URL xmlUrl = getClass().getResource("/.space test/testjar.jar");
        File moduleRoot = new ModuleRegistration().getModuleRoot(xmlUrl);

        assertNotNull("Unable to find jar", moduleRoot);
        assertTrue("Jar " + moduleRoot.getAbsolutePath() + " does not exist", moduleRoot.exists());
        assertEquals(new File(MGNL_CORE_ROOT, "target"), moduleRoot);
    }

    public void testGetModuleRootDirectory() {
        File moduleRoot = new ModuleRegistration().getModuleRoot("/testresource.txt");

        assertNotNull(moduleRoot);
        assertTrue(moduleRoot.exists());
        assertEquals(MGNL_CORE_ROOT, moduleRoot);
    }

    public void testGetModuleRootDirectoryWithDot() {
        File moduleRoot = new ModuleRegistration().getModuleRoot("/.space test/testresource.txt");

        assertNotNull(moduleRoot);
        assertTrue(moduleRoot.exists());
        assertEquals(new File(MGNL_CORE_ROOT, "target"), moduleRoot);
    }

    public void testCalcDepencyLevelWithNonOptionalDependencies() {
        final ModuleDefinition modDefA = new ModuleDefinition("mod-a", "v1", "fake.Module");
        final ModuleDefinition modDefB = new ModuleDefinition("mod-b", "v1", "fake.Module");
        final ModuleDefinition modDefC = new ModuleDefinition("mod-c", "v1", "fake.Module");
        final DependencyDefinition depOnA = new DependencyDefinition();
        depOnA.setName("mod-a");
        depOnA.setVersion("v1");
        final DependencyDefinition depOnB = new DependencyDefinition();
        depOnB.setName("mod-b");
        depOnB.setVersion("v1");
        modDefB.addDependency(depOnA);
        modDefC.addDependency(depOnB);

        final ModuleRegistration reg = new ModuleRegistration();
        reg.getModuleDefinitions().put(modDefA.getName(), modDefA);
        reg.getModuleDefinitions().put(modDefB.getName(), modDefB);
        reg.getModuleDefinitions().put(modDefC.getName(), modDefC);

        assertEquals(0, reg.calcDependencyLevel(modDefA));
        assertEquals(1, reg.calcDependencyLevel(modDefB));
        assertEquals(2, reg.calcDependencyLevel(modDefC));
    }

    public void testCalcDepencyLevelIgnoresUnregisteredOptionalDependencies() {
        final ModuleDefinition modDefB = new ModuleDefinition("mod-b", "v1", "fake.Module");
        final ModuleDefinition modDefC = new ModuleDefinition("mod-c", "v1", "fake.Module");
        final DependencyDefinition depOnA = new DependencyDefinition();
        depOnA.setName("mod-a");
        depOnA.setVersion("v1");
        depOnA.setOptional(true);
        final DependencyDefinition depOnB = new DependencyDefinition();
        depOnB.setName("mod-b");
        depOnB.setVersion("v1");
        modDefC.addDependency(depOnA);
        modDefC.addDependency(depOnB);

        final ModuleRegistration reg = new ModuleRegistration();
        // mod-a is not registered in this case
        reg.getModuleDefinitions().put(modDefB.getName(), modDefB);
        reg.getModuleDefinitions().put(modDefC.getName(), modDefC);

        assertEquals(0, reg.calcDependencyLevel(modDefB));
        assertEquals(1, reg.calcDependencyLevel(modDefC));
    }
    
    public void testCalcDepencyLevelDoesNotIgnoreRegisteredOptionalDependencies() {
        final ModuleDefinition modDefA = new ModuleDefinition("mod-a", "v1", "fake.Module");
        final ModuleDefinition modDefB = new ModuleDefinition("mod-b", "v1", "fake.Module");
        final ModuleDefinition modDefC = new ModuleDefinition("mod-c", "v1", "fake.Module");
        final DependencyDefinition depOnA = new DependencyDefinition();
        depOnA.setName("mod-a");
        depOnA.setVersion("v1");
        depOnA.setOptional(true);
        final DependencyDefinition depOnB = new DependencyDefinition();
        depOnB.setName("mod-b");
        depOnB.setVersion("v1");
        modDefB.addDependency(depOnA);
        modDefC.addDependency(depOnA);
        modDefC.addDependency(depOnB);

        final ModuleRegistration reg = new ModuleRegistration();
        reg.getModuleDefinitions().put(modDefA.getName(), modDefA);
        reg.getModuleDefinitions().put(modDefB.getName(), modDefB);
        reg.getModuleDefinitions().put(modDefC.getName(), modDefC);

        assertEquals(0, reg.calcDependencyLevel(modDefA));
        assertEquals(1, reg.calcDependencyLevel(modDefB));
        assertEquals(2, reg.calcDependencyLevel(modDefC));
    }

}
