package info.magnolia.cms.mail.templates;

import info.magnolia.cms.mail.MailConstants;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.net.URL;

/**
 * Date: Apr 1, 2006
 * Time: 8:38:07 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MailAttachment {
    String description;
    String disposition;
    String name;
    URL url;

    public MailAttachment(File file, String _name, String _description, String _disposition) {
        this.setPath(file.getAbsolutePath());
        this.name = _name;
        this.description = _description;
        this.disposition = _disposition;
    }

    public MailAttachment(URL _url, String _name, String _description, String _disposition) {
        this.url = _url;
        this.name = _name;
        this.description = _description;
        this.disposition = _disposition;
    }

    public MailAttachment(File file, String _name) {
        this.setPath(file.getAbsolutePath());
        this.name = _name;
        this.disposition = MailConstants.ATTACHMENT_INLINE;
        this.description = StringUtils.EMPTY;
    }

    public MailAttachment(URL _url, String _name) {
        this.url = _url;
        this.name = _name;
        this.disposition = MailConstants.ATTACHMENT_INLINE;
        this.description = StringUtils.EMPTY;
    }

    public java.lang.String getDescription() {
        return this.description;
    }

    public java.lang.String getDisposition() {
        return this.disposition;
    }

    public java.lang.String getName() {
        return this.name;
    }

    public java.lang.String getPath() {
        return url.getFile();
    }

    public java.net.URL getURL() {
        if (url.getProtocol().startsWith("file:")) {
            try {
                return new URL("file://" + url.toExternalForm());
            }
            catch (Exception e) {
                return null;
            }
        } else
            return this.url;
    }

    public java.io.File getFile() {
        return new File(this.url.getFile());
    }

    public void setDescription(java.lang.String desc) {
        this.description = desc;
    }

    public void setDisposition(java.lang.String aDisposition) {
        this.disposition = aDisposition;
    }


    public void setName(java.lang.String aName) {
        this.name = aName;
    }

    public void setPath(java.lang.String aPath) {
        try {
            this.url = new URL(MailConstants.FILE_URL_PREFIX + aPath);
        } catch (Exception e) {
            e.printStackTrace();
            this.url = null;
        }
    }

    public void setURL(java.net.URL aUrl) {
        this.url = aUrl;
    }

    public String getContentType() {
        return MgnlEmail.map.getContentType(this.getPath());
    }

    public String getFileName() {
        return new File(url.getFile()).getName();
    }

}
