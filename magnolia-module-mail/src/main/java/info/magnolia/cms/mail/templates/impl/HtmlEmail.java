package info.magnolia.cms.mail.templates.impl;

import info.magnolia.cms.mail.MailConstants;
import info.magnolia.cms.mail.templates.MgnlMultipartEmail;

import javax.mail.BodyPart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Date: Mar 30, 2006
 * Time: 2:12:53 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class HtmlEmail extends MgnlMultipartEmail {
    public HtmlEmail(Session _session) throws Exception {
        super(_session);
        this.setHeader(MailConstants.CONTENT_TYPE, MailConstants.TEXT_HTML_UTF);
    }

    public void setBody(String body, HashMap parameters) throws Exception {

        // Create your new message part
        BodyPart messageBodyPart = new MimeBodyPart();

        // Set the content of the body part
        messageBodyPart.setContent(body, MailConstants.TEXT_HTML_UTF);

        // Add body part to multipart
        getMailMultipart().addBodyPart(messageBodyPart);

        // Create part for the image
        if (parameters != null && parameters.containsKey(MailConstants.MAIL_ATTACHMENT)) {
            ArrayList attachment = (ArrayList) parameters.get(MailConstants.MAIL_ATTACHMENT);
            setAttachments(attachment);
        }
    }

}
