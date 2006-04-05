package info.magnolia.cms.mail.handlers;

import info.magnolia.cms.mail.templates.MgnlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Transport;
import java.util.Arrays;

/**
 * This is a simple util class to send email from the form pages ... This class was previously included inside the jsp
 * related to the form.
 *
 * @author niko
 * @version $Id $
 */
public class SimpleMailHandler implements MgnlMailHandler {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(SimpleMailHandler.class);

    public synchronized void prepareAndSendMail(MgnlEmail email) throws Exception {
        email.setBody();
        sendMail(email);
    }

    public synchronized void sendMail(MgnlEmail email) throws Exception {
        try {
            Transport.send(email);
            log.info("Mail has been sent to: [" + Arrays.asList(email.getAllRecipients()) + "]");
        }
        catch (Exception e) {
            log.error("Email to: [" + Arrays.asList(email.getAllRecipients()) + "] was not sent because of an error", e);
            throw e;
        }
    }
}
