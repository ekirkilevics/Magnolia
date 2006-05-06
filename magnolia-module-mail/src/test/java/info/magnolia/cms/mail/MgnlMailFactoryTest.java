package info.magnolia.cms.mail;

/**
 * 
 * Date: Mar 31, 2006
 * Time: 9:14:00 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */

import info.magnolia.cms.mail.templates.MgnlEmail;

import java.util.Iterator;

import com.dumbster.smtp.SmtpMessage;


public class MgnlMailFactoryTest extends AbstractMailTest {

    public void testSimpleMail() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_TEXT);
        String subject = "Test simple";
        email.setText("Hello bonjour");
        email.setSubject(subject);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        handler.prepareAndSendMail(email);

        assertTrue(server.getReceivedEmailSize() == 1);
        Iterator emailIter = server.getReceivedEmail();
        SmtpMessage message = (SmtpMessage) emailIter.next();
        assertTrue(message.getHeaderValue("Subject").equals(subject));
    }

}