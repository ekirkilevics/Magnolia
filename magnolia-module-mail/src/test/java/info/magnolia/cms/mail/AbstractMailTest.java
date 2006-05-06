package info.magnolia.cms.mail;

import info.magnolia.cms.mail.handlers.SimpleMailHandler;
import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dumbster.smtp.SimpleSmtpServer;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractMailTest extends TestCase implements TestConstants {

    /**
     * Logger
     */
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected MgnlMailFactory factory;

    protected SimpleMailHandler handler;

    protected SimpleSmtpServer server;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        handler = new SimpleMailHandler();
        factory = MgnlMailFactory.getInstance();
        factory.initParam(MgnlMailFactory.SMTP_SERVER, "localhost");
        factory.initParam(MgnlMailFactory.SMTP_PORT, "1025");

        server = SimpleSmtpServer.start(1025);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

}
