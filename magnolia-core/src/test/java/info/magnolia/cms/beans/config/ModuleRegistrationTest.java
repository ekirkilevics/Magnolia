package info.magnolia.cms.beans.config;

import info.magnolia.cms.util.FactoryUtil;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class ModuleRegistrationTest extends TestCase {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ModuleRegistrationTest.class);

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
    }

    public void testGetModuleRootJarWIthDot() {
        URL xmlUrl = getClass().getResource("/.space test/testjar.jar");
        File moduleRoot = new ModuleRegistration().getModuleRoot(xmlUrl);

        assertNotNull("Unable to find jar", moduleRoot);
        assertTrue("Jar " + moduleRoot.getAbsolutePath() + " does not exist", moduleRoot.exists());
    }

    public void testGetModuleRootDirectory() {
        File moduleRoot = new ModuleRegistration().getModuleRoot("/testresource.txt");

        assertNotNull(moduleRoot);
        assertTrue(moduleRoot.exists());
    }

    public void testGetModuleRootDirectoryWithDot() {
        File moduleRoot = new ModuleRegistration().getModuleRoot("/.space test/testresource.txt");

        assertNotNull(moduleRoot);
        assertTrue(moduleRoot.exists());
    }

}
