package info.magnolia.cms.beans.config;

import info.magnolia.cms.util.FactoryUtil;

import java.io.File;

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
        File moduleRoot = new ModuleRegistration().getModuleRoot("/org/apache/commons/collections/Buffer.class");

        assertNotNull(moduleRoot);
        assertTrue(moduleRoot.exists());

    }

    public void testGetModuleRootDirectory() {
        File moduleRoot = new ModuleRegistration().getModuleRoot("/mgnl-nodetypes/magnolia-nodetypes.xml");

        assertNotNull(moduleRoot);
        assertTrue(moduleRoot.exists());

    }

}
