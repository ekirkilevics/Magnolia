package info.magnolia.cms.mail;

/**
 * 
 * Date: Mar 31, 2006
 * Time: 9:14:00 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */

import info.magnolia.cms.mail.handlers.SimpleMailHandler;
import info.magnolia.cms.mail.templates.MgnlEmail;
import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MgnlMailFactoryTest extends TestCase implements TestConstants {

    Logger log = LoggerFactory.getLogger(MgnlMailFactoryTest.class);

    MgnlMailFactory factory = MgnlMailFactory.getInstance();

    SimpleMailHandler handler = new SimpleMailHandler();

    public void testResourcePath() throws Exception {
        log.info(TestUtil.getResourceRootFolder().getAbsolutePath());
    }

    public void testSimpleMail() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_TEXT);
        email.setText("Hello bonjour");
        email.setSubject("Test simple");
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        handler.prepareAndSendMail(email);
    }

}