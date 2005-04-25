package info.magnolia.cms.bean.config;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.runtime.SystemProperty;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.test.MagnoliaTestUtils;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

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

        String testResourcesDir = MagnoliaTestUtils.getTestResourcesDir();
        String baseTestDir = testResourcesDir + "/bootstrap-test";

        MockServletConfig config = new MockServletConfig();
        MockServletContext context = new MockServletContext();
        config.setServletContext(context);
        context.setRealPath(StringUtils.EMPTY, baseTestDir);

        config.setInitParameter(SystemProperty.MAGNOLIA_REPOSITORIES_CONFIG, baseTestDir
            + "/WEB-INF/config/repositories.xml");

        config.setInitParameter(SystemProperty.MAGNOLIA_BOOTSTRAP_ROOTDIR, MagnoliaTestUtils.getProjectRoot()
            + "/src/webapp/WEB-INF/bootstrap");

        new ConfigLoader(config);

        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        assertNotNull("Config repository not properly configured.", hm);

        Content serverConfigRoot = null;
        try {
            serverConfigRoot = hm.getPage(Server.CONFIG_PAGE);
        }
        catch (AccessDeniedException e) {
            fail("Access denied to [" + Server.CONFIG_PAGE + "]");
        }
        catch (PathNotFoundException e) {
            fail("Config repository not correctly initialized, missing [" + Server.CONFIG_PAGE + "]");
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            fail("Exception caught: " + e.getMessage());
        }

        assertNotNull(serverConfigRoot);

    }

}
