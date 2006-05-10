package info.magnolia.cms.mail;

import info.magnolia.cms.mail.handlers.SimpleMailHandler;

import java.io.File;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dumbster.smtp.SimpleSmtpServer;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractMailTest extends TestCase {

    /**
     * Logger
     */
    protected Logger log = LoggerFactory.getLogger(getClass());

    public final static String TEST_RECIPIENT = "niko@macnica.com";

    public final static String TEST_SENDER = "niko@magnolia.com";

    public static final String TEST_FILE_JPG = "magnolia.jpg";

    public static final String TEST_FILE_PDF = "magnolia.pdf";

    public static int SMTP_PORT = 25025;

    protected MgnlMailFactory factory;

    protected SimpleMailHandler handler;

    protected SimpleSmtpServer server;

    public File getResourceFile(String filename) {
        return new File(getClass().getResource("/" + filename).getFile());
    }

    public String getResourcePath(String filename) {
        return getResourceFile(filename).getAbsolutePath();
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        handler = new SimpleMailHandler();
        factory = MgnlMailFactory.getInstance();
        factory.initParam(MgnlMailFactory.SMTP_SERVER, "localhost");
        factory.initParam(MgnlMailFactory.SMTP_PORT, Integer.toString(SMTP_PORT));

        server = SimpleSmtpServer.start(SMTP_PORT);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

}
