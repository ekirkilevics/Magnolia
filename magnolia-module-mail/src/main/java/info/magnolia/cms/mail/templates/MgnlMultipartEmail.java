package info.magnolia.cms.mail.templates;

import info.magnolia.cms.mail.MailConstants;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.net.URL;
import java.util.ArrayList;

/**
 * Date: Apr 1, 2006
 * Time: 9:00:35 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public abstract class MgnlMultipartEmail extends MgnlEmail {

    MimeMultipart multipart;

    protected MgnlMultipartEmail(Session _session) {
        super(_session);
        // Create a related multi-part to combine the parts
        multipart = new MimeMultipart(MailConstants.RELATED);
        // Associate multi-part with message
        try {
            this.setContent(multipart);
        } catch (Exception e) {
            log.error("Could not set the content of the email.");
        }
    }

    public MimeMultipart getMailMultipart() {
        return multipart;
    }

    public MimeMultipart addAttachment(MailAttachment attachment) throws Exception {

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        String key = attachment.getName();
        log.info("Found new attachment with name :" + key);

        // access content of this mail
        MimeMultipart _multipart = this.getMailMultipart();

        // get info on the attachment
        URL url = attachment.getURL();
        String name = attachment.getFileName();
        String contentType = attachment.getContentType();

        // set the header as well as the content type
        messageBodyPart.setHeader(MailConstants.CONTENT_TYPE, contentType + "; name=\"" + name + "\"");
        // set the disposition of the file.
        messageBodyPart.setDisposition("inline; filename=\"" + name + "\"");

        // Fetch the image and associate to part
        DataSource fds = url.getProtocol().startsWith("file:") ? (DataSource) new FileDataSource(url.getFile()) : (DataSource) new URLDataSource(url);
        //DataSource fd = new FileDataSource(file);
        messageBodyPart.setDataHandler(new DataHandler(fds));
        // Add a header to connect to the HTML
        messageBodyPart.setHeader(MailConstants.CONTENT_ID, key);
        // Add part to multi-part
        _multipart.addBodyPart(messageBodyPart);

        // Set the content again
        this.setContent(_multipart);

        return _multipart;
    }


    public void setAttachments(ArrayList list) throws Exception {
        if (list == null)
            return;
        log.info("Set attachments [" + list.size() + "] for mail: [" + this.getClass().getName() + "]");
        for (int i = 0; i < list.size(); i++) {
            addAttachment((MailAttachment) list.get(i));
        }
    }
}
