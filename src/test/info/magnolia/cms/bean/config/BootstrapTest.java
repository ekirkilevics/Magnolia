package info.magnolia.cms.bean.config;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.runtime.SystemProperty;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mockrunner.mock.web.MockServletConfig;
import com.mockrunner.mock.web.MockServletContext;


/**
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class BootstrapTest extends TestCase {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(BootstrapTest.class);

    /**
     * Test bootstrap from files. This test can be easily used as a target for profiling the repository initialization
     * process.
     */
    public void testBootstrap() {

        String testResourcesDir = new File(getClass().getResource("/test-resources.dir").getFile()).getParent();
        String baseTestDir = testResourcesDir + "/bootstrap-test";

        MockServletConfig config = new MockServletConfig();
        MockServletContext context = new MockServletContext();
        config.setServletContext(context);
        context.setRealPath(StringUtils.EMPTY, baseTestDir);

        config.setInitParameter(SystemProperty.MAGNOLIA_REPOSITORIES_CONFIG, baseTestDir
            + "/WEB-INF/config/repositories.xml");

        config.setInitParameter(SystemProperty.MAGNOLIA_BOOTSTRAP_ROOTDIR, baseTestDir + "/bootstrap");

        new ConfigLoader(config);

    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        // @todo cleanup repositories
        super.tearDown();
    }
}
