package info.magnolia.cms.mail.templates;

import info.magnolia.cms.mail.MailConstants;
import info.magnolia.cms.mail.MailException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Date: Mar 30, 2006
 * Time: 1:01:37 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public abstract class MgnlEmail extends MimeMessage {

    public static Logger log = LoggerFactory.getLogger(MgnlEmail.class);
    public static final MimetypesFileTypeMap map = new MimetypesFileTypeMap();
    private String template;
    private HashMap parameters;
    private boolean bodyNotSetFlag; // used for threads

    public boolean isBodyNotSetFlag() {
        return bodyNotSetFlag;
    }

    public void setBodyNotSetFlag(boolean _bodyNotSetFlag) {
        this.bodyNotSetFlag = _bodyNotSetFlag;
    }

    public MgnlEmail(Session _session) {
        super(_session);
    }

    public abstract void setBody(String body, HashMap _parameters) throws Exception;

    public void setTemplate(String _template) {
        this.template = _template;
    }

    public HashMap getParameters() {
        return parameters;
    }

    public String getTemplate() {
        return template;
    }

    public void setParameters(HashMap _parameters) {
        this.parameters = _parameters;
    }

    public void setBody() throws Exception {
        if (template != null)
            this.setBody(template, parameters);
    }

    /**
     * @noinspection MethodOverloadsMethodOfSuperclass
     */
    public void setFrom(String _from) {
        try {
            this.setFrom(new InternetAddress(_from));
        }
        catch (Exception e) {
            log.error("Could not set from field of email:" + e.getMessage());
        }
    }

    public void setCharsetHeader(String charset) throws MailException {
        try {
            StringBuffer contentType = new StringBuffer(this.getHeader(MailConstants.CONTENT_TYPE, MailConstants.TEXT_PLAIN_UTF));
            int index = contentType.lastIndexOf(";");
            if (index != -1)
                contentType.substring(0, index);
            contentType.append(MailConstants.CHARSET_HEADER_STRING).append(charset);
        } catch (Exception e) {
            throw new MailException("Content type is not set. Set the content type before setting the charset");
        }
    }

    public void setToList(String to) throws Exception {
        setListByString(to, Message.RecipientType.TO);
    }

    public void setCcList(String to) throws Exception {
        setListByString(to, Message.RecipientType.CC);
    }

    public void setBccList(String to) throws Exception {
        setListByString(to, Message.RecipientType.BCC);
    }

    private void setListByString(String to, Message.RecipientType type) throws Exception {
        if (to == null || to.equals(StringUtils.EMPTY))
            return;
        String[] toObj = to.split("\n");
        Address[] ato = new Address[toObj.length];
        for (int i = 0; i < toObj.length; i++) {
            ato[i] = new InternetAddress(toObj[i]);
        }
        this.setRecipients(type, ato);
    }


    public void setAttachments(ArrayList list) throws MailException {
        if (list == null)
            return;
        if (log.isDebugEnabled())
            log.debug("Set attachments [" + list.size() + "] for mail: [" + this.getClass().getName() + "]");
        for (int i = 0; i < list.size(); i++) {
            addAttachment((MailAttachment) list.get(i));
        }
    }

    public MimeBodyPart addAttachment(MailAttachment attachment) throws MailException {
        throw new MailException("Cannot add attachment to this email. It is not a Multimime email");
    }

    public void setBodyFromResourceFile(String resourceFile, HashMap map) throws Exception {
        URL url = this.getClass().getResource("/" + resourceFile);
        log.info("This is the url:" + url);
        BufferedReader br = new BufferedReader(new FileReader(url.getFile()));
        String line;
        StringBuffer buffer = new StringBuffer();
        while ((line = br.readLine()) != null) {
            buffer.append(line).append(File.separator);
        }
        this.setBody(buffer.toString(), map);
    }

}
