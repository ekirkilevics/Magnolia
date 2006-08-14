package info.magnolia.cms.mail;

import info.magnolia.cms.mail.handlers.SimpleMailHandler;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;


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

    protected Wiser wiser = new Wiser();

    public File getResourceFile(String filename) {
        return new File(getClass().getResource("/" + filename).getFile());
    }

    public String getResourcePath(String filename) {
        return getResourceFile(filename).getAbsolutePath();
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();

        handler = new SimpleMailHandler();
        factory = MgnlMailFactory.getInstance();
        factory.initParam(MgnlMailFactory.SMTP_SERVER, "localhost");
        factory.initParam(MgnlMailFactory.SMTP_PORT, Integer.toString(SMTP_PORT));

        wiser.setPort(SMTP_PORT);
        wiser.start();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception {
        wiser.stop();
        super.tearDown();
    }

    /**
     * this test will fail when subject is not US-ASCII TODO: replace with mail parser or handle encoding and improve
     * pattern
     * @param message
     * @param subject
     * @return true is <code>message</code>'s subject equals <code>subject</code>
     */
    protected boolean hasMatchingSubject(String message, String subject) {
        Pattern pattern = Pattern.compile("Subject: " + subject);
        Matcher matcher = pattern.matcher(message);
        return matcher.find();
    }

}
