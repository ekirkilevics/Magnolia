package info.magnolia.cms.mail.handlers;

import info.magnolia.cms.mail.templates.MgnlEmail;

/**
 * Date: Mar 30, 2006
 * Time: 1:06:23 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public interface MgnlMailHandler {

    /**
     * Prepare the email (format it) and send it
     *
     * @param email the email to send
     * @throws Exception if fails
     */
    void prepareAndSendMail(MgnlEmail email) throws Exception;

    /**
     * Send the email as is, without touching it
     *
     * @param email the email to send
     * @throws Exception if fails
     */
    void sendMail(MgnlEmail email) throws Exception;

}
