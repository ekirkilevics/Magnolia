package info.magnolia.cms.mail.templates.impl;

import info.magnolia.cms.mail.MailException;
import info.magnolia.cms.mail.templates.MailAttachment;
import info.magnolia.cms.mail.templates.MgnlMultipartEmail;

import java.util.List;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;


/**
 * Date: Mar 30, 2006 Time: 2:12:53 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class HtmlEmail extends MgnlMultipartEmail {

    public static final String MAIL_ATTACHMENT = "attachment";

    public HtmlEmail(Session _session) throws Exception {
        super(_session);
        this.setHeader(CONTENT_TYPE, TEXT_HTML_UTF);
    }

    public void setBody(String body, Map parameters) throws Exception {
        // check if multipart
        if (!isMultipart()) { // it is not a multipart yet, just set the text for content
            this.setContent(body, TEXT_HTML_UTF);
        }
        else { // some attachment are already in this mail. Init the body part to set the main text
            // Create your new message part
            BodyPart messageBodyPart = new MimeBodyPart();
            // Set the _content of the body part
            messageBodyPart.setContent(body, TEXT_HTML_UTF);
            // Add body part to multipart
            this.multipart.addBodyPart(messageBodyPart, 0);
            this.setContent(this.multipart);
        }

        // process the attachments
        if (parameters != null && parameters.containsKey(MAIL_ATTACHMENT)) {
        	Object attachment = parameters.get(MAIL_ATTACHMENT);
        	if(attachment instanceof MailAttachment) {
        		addAttachment((MailAttachment)attachment);
        	}
        	else if(attachment instanceof List) {
        		setAttachments((List) attachment);
        	};
        }
    }

    private void turnOnMultipart() {
        try {
            Object o = this.getContent();
            if (o instanceof String) {
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(o, TEXT_HTML_UTF);
                this.multipart.addBodyPart(messageBodyPart, 0);
                this.setContent(this.multipart);
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
