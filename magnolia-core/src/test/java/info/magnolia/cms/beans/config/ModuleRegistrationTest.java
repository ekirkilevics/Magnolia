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
        File moduleRoot = new ModuleRegistration(). getModuleRoot(xmlUrl);

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


}
