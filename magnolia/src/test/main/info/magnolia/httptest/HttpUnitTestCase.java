package info.magnolia.httptest;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import com.meterware.servletunit.ServletRunner;


/**
 * Base TestCase class for httpunit tests.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public abstract class HttpUnitTestCase extends TestCase {

    /**
     * Context mapped to the test application.
     */
    public static final String CONTEXT = "/context";

    /**
     * logger.
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * HttpUnit ServletRunner.
     */
    protected ServletRunner runner;

    /**
     * Returns the tested jsp name.
     * @return jsp name
     */
    public abstract String getJspName();

    /**
     * Runs the test.
     * @param jspName jsp name, with full path
     * @throws Exception any axception thrown during test.
     */
    public abstract void doTest(String jspName) throws Exception;

    /**
     * run the test with the jsp 11 tld.
     * @throws Exception any axception thrown during test.
     */
    public void testHtmlPage() throws Exception {
        doTest("http://localhost" + CONTEXT + "/tags/" + getJspName());
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        // remove any compiled jsp from a previous run.
        cleanupTempFile("tags/" + getJspName());

        // need to pass a web.xml file to setup servletunit working directory
        ClassLoader classLoader = getClass().getClassLoader();
        URL webXmlUrl = classLoader.getResource("WEB-INF/web.xml");
        String path = webXmlUrl.getFile();

        // start servletRunner
        runner = new ServletRunner(new File(path), CONTEXT);
        log.debug("ServletRunner setup OK");

        super.setUp();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        // shutdown servlet engine
        runner.shutDown();
        super.tearDown();
    }

    /**
     * @see junit.framework.TestCase#getName()
     */
    public String getName() {
        return getClass().getName() + "." + super.getName() + " (" + getJspName() + ")";
    }

    /**
     * Clean up temporary files from a previous test.
     * @param jspName jsp name, with full path
     */
    private void cleanupTempFile(String jspName) {
        URL resourceUrl = getClass().getResource("/" + jspName);
        if (resourceUrl != null && SystemUtils.JAVA_IO_TMPDIR != null) {
            File jspFile = new File(resourceUrl.getFile());
            long jspModified = jspFile.lastModified();

            String path = SystemUtils.JAVA_IO_TMPDIR + jspName;

            File tempFile = new File(StringUtils.replace(path, ".jsp", "$jsp.java"));

            // delete file only if jsp has been modified
            if (tempFile.exists() && tempFile.lastModified() < jspModified) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting temporary file " + tempFile.getPath());
                }
                tempFile.delete();
            }
            tempFile = new File(StringUtils.replace(path, ".jsp", "$jsp.class"));
            if (tempFile.exists() && tempFile.lastModified() < jspModified) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting temporary file " + tempFile.getPath());
                }
                tempFile.delete();
            }
        }
    }

}