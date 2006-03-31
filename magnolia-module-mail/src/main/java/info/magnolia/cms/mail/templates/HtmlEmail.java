package info.magnolia.cms.mail.templates;

import info.magnolia.cms.mail.MailConstants;

import javax.mail.BodyPart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Date: Mar 30, 2006
 * Time: 2:12:53 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class HtmlEmail extends MgnlEmail {
    public HtmlEmail(Session session) throws Exception {
        super(session);
        this.setHeader(MailConstants.CONTENT_TYPE, MailConstants.TEXT_HTML_UTF);
    }

    public void setBody(String body, HashMap parameters) throws Exception {
        // Create your new message part
        BodyPart messageBodyPart = new MimeBodyPart();

        // Set the HTML content, be sure it references the attachment
        //String htmlText = "<H1>Hello</H1>" + "<img src=\"cid:memememe\">";
        String htmlText = body;

        // Set the content of the body part
        messageBodyPart.setContent(htmlText, MailConstants.TEXT_HTML_UTF);

        // Create a related multi-part to combine the parts
        MimeMultipart multipart = new MimeMultipart(MailConstants.RELATED);

        // Add body part to multipart
        multipart.addBodyPart(messageBodyPart);

        // Associate multi-part with message
        this.setContent(multipart);

        // Create part for the image
        if (parameters.containsKey(MailConstants.MAIL_ATTACHMENT)) {
            Hashtable attachment = (Hashtable) parameters.get(MailConstants.MAIL_ATTACHMENT);
            setAttachments(attachment);
        }
    }

}
