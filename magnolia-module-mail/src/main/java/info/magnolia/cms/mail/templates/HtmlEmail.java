package info.magnolia.cms.mail.templates;

import info.magnolia.cms.mail.MailConstants;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.net.URL;
import java.util.HashMap;

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
        String htmlText = "<H1>Hello</H1>" +
                "<img src=\"cid:memememe\">";

        // Set the content of the body part
        messageBodyPart.setContent(htmlText, MailConstants.TEXT_HTML_UTF);

        // Create a related multi-part to combine the parts
        MimeMultipart multipart = new MimeMultipart(MailConstants.RELATED);

        // Add body part to multipart
        multipart.addBodyPart(messageBodyPart);

        // Create part for the image
        messageBodyPart = new MimeBodyPart();

        URL url = new URL("http://www.google.com");

        // Fetch the image and associate to part
        DataSource fds = new URLDataSource(url);
        messageBodyPart.setDataHandler(new DataHandler(fds));

        // Add a header to connect to the HTML
        messageBodyPart.setHeader("Content-ID", "<memememe>");

        // Add part to multi-part
        multipart.addBodyPart(messageBodyPart);

        // Associate multi-part with message
        this.setContent(multipart);
    }

}
