package info.magnolia.cms.mail.templates.impl;

import info.magnolia.cms.mail.MailConstants;
import info.magnolia.cms.mail.MailException;
import info.magnolia.cms.mail.templates.MailAttachment;
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
        // check if multipart
        if (!isMultipart()) { // it is not a multipart yet, just set the text for content
            this.setContent(body, MailConstants.TEXT_HTML_UTF);
        } else { // some attachment are already in this mail. Init the body part to set the main text
            // Create your new message part
            BodyPart messageBodyPart = new MimeBodyPart();
            // Set the _content of the body part
            messageBodyPart.setContent(body, MailConstants.TEXT_HTML_UTF);
            // Add body part to multipart
            multipart.addBodyPart(messageBodyPart, 0);
            this.setContent(multipart);
        }

        // process the attachments
        if (parameters != null && parameters.containsKey(MailConstants.MAIL_ATTACHMENT)) {
            ArrayList attachment = (ArrayList) parameters.get(MailConstants.MAIL_ATTACHMENT);
            setAttachments(attachment);
        }
    }

    private void turnOnMultipart() {
        try {
            Object o = this.getContent();
            if (o instanceof String) {
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(o, MailConstants.TEXT_HTML_UTF);
                multipart.addBodyPart(messageBodyPart, 0);
                this.setContent(multipart);
            }
        }
        catch (Exception e) {
            log.info("Could not turn on multipart");
        }
    }

    public MimeBodyPart addAttachment(MailAttachment attachment) throws MailException {
        if (!isMultipart()) {
            turnOnMultipart();
        }
        return super.addAttachment(attachment);
    }

}
