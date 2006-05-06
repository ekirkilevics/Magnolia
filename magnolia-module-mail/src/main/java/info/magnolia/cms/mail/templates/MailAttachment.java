package info.magnolia.cms.mail.templates;

import java.io.File;
import java.net.URL;

import org.apache.commons.lang.StringUtils;


/**
 * Date: Apr 1, 2006 Time: 8:38:07 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MailAttachment {

    private static final String ATTACHMENT_INLINE = "inline";

    private static final String ATTACHMENT_NORMAL = "normal";

    private static final String FILE_URL_PREFIX = "file://";

    String description;

    String disposition;

    String name;

    URL url;

    public MailAttachment(File file, String name, String description, String disposition) {
        this.setPath(file.getAbsolutePath());
        this.name = name;
        this.description = description;
        this.disposition = disposition;
    }

    public MailAttachment(URL url, String _name, String _description, String _disposition) {
        this.url = url;
        this.name = _name;
        this.description = _description;
        this.disposition = _disposition;
    }

    public MailAttachment(URL _url, String name) {
        this.url = _url;
        this.name = name;
        this.disposition = ATTACHMENT_INLINE;
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
        return this.url.getFile();
    }

    public java.net.URL getURL() {
        if (this.url.getProtocol().startsWith("file:")) {
            try {
                return new URL("file://" + this.url.toExternalForm());
            }
            catch (Exception e) {
                return null;
            }
        }

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
            this.url = new URL(FILE_URL_PREFIX + aPath);
        }
        catch (Exception e) {
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
        return new File(this.url.getFile()).getName();
    }

}
