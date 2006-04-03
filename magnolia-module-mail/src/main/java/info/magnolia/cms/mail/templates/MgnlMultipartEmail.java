package info.magnolia.cms.mail.templates;

import info.magnolia.cms.mail.MailConstants;
import info.magnolia.cms.mail.MailException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.net.URL;

/**
 * Date: Apr 1, 2006
 * Time: 9:00:35 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public abstract class MgnlMultipartEmail extends MgnlEmail {

    protected MimeMultipart multipart;

    protected MgnlMultipartEmail(Session _session) {
        super(_session);
        // Create a related multi-part to combine the parts
        multipart = new MimeMultipart(MailConstants.RELATED);
    }

    public boolean isMultipart() {
        try {
            int count = multipart.getCount();
            return (count > 0);
        } catch (MessagingException e) {
            return false;
        }
    }

    public MimeMultipart getMailMultipart() {
        return multipart;
    }

    public MimeBodyPart addAttachment(MailAttachment attachment) throws MailException {

        try {
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            String key = attachment.getName();
            log.info("Found new attachment with name :" + key);

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
            multipart.addBodyPart(messageBodyPart);

            // Set the content again
            this.setContent(multipart);

            return messageBodyPart;
        }
        catch (Exception e) {
            throw new MailException(e.getMessage(), e);
        }
    }

    //public abstract String getContentDescription();
}
