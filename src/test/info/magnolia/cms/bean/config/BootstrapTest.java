package info.magnolia.cms.bean.config;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.test.MagnoliaTestUtils;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

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

        MockServletContext context = new MockServletContext();

        Map config = new HashMap();
        context.setRealPath(StringUtils.EMPTY, baseTestDir);

        config.put(SystemProperty.MAGNOLIA_REPOSITORIES_CONFIG, baseTestDir + "/WEB-INF/config/repositories.xml");

        config.put(SystemProperty.MAGNOLIA_BOOTSTRAP_ROOTDIR, MagnoliaTestUtils.getProjectRoot()
            + "/src/webapp/WEB-INF/bootstrap");

        new ConfigLoader(context, config);

        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        assertNotNull("Config repository not properly configured.", hm);

        try {
            hm.getContent(Server.CONFIG_PAGE);
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

        hm = ContentRepository.getHierarchyManager(ContentRepository.WEBSITE);
        assertNotNull("Website repository not properly configured.", hm);

        try {
            hm.getContent("features");
        }
        catch (AccessDeniedException e) {
            fail("Access denied to [features] page");
        }
        catch (PathNotFoundException e) {
            fail("Website repository not correctly initialized, missing [features] page");
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            fail("Exception caught: " + e.getMessage());
        }

        try {
            hm.getContent("thispagedoesntexist");
            fail("Test doesn't get a PathNotFoundException while it should.");
        }
        catch (PathNotFoundException e) {
            // ok
            log.debug("PathNotFoundException caught as expected");
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            fail("Exception caught: " + e.getMessage());
        }

    }

}
