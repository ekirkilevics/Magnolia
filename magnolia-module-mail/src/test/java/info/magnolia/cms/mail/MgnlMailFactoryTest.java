package info.magnolia.cms.mail;

/**
 *
 * Date: Mar 31, 2006
 * Time: 9:14:00 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */

import info.magnolia.cms.mail.templates.MgnlEmail;

import java.util.Iterator;

import org.subethamail.wiser.WiserMessage;

public class MgnlMailFactoryTest extends AbstractMailTest {

    public void testSimpleMail() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_TEXT);
        String subject = "Test simple";
        email.setText("Hello bonjour");
        email.setSubject(subject);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        handler.prepareAndSendMail(email);

        assertTrue(wiser.getMessages().size() == 1);
        Iterator emailIter = wiser.getMessages().iterator();
        WiserMessage message = (WiserMessage) emailIter.next();
        assertTrue(message.getMimeMessage().getSubject().equals(subject));
    }

}
