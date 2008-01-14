/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.mail.templates;

import info.magnolia.cms.mail.MailException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Date: Mar 30, 2006 Time: 1:01:37 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public abstract class MgnlEmail extends MimeMessage {

    protected static final String CONTENT_TYPE = "Content-Type";

    protected static final String TEXT_PLAIN_UTF = "text/plain; charset=UTF-8";

    protected static final String TEXT_HTML_UTF = "text/html; charset=UTF-8";

    protected static final String CHARSET_HEADER_STRING = "charset=";
    
    protected static final Pattern EMAIL_WITH_PERSONAL_PATTERN = Pattern.compile("\"+(.*)\"+<(.*)>");

    public static Logger log = LoggerFactory.getLogger(MgnlEmail.class);

    private String template;

    private Map parameters;

    private boolean bodyNotSetFlag; // used for threads

    public boolean isBodyNotSetFlag() {
        return this.bodyNotSetFlag;
    }

    public void setBodyNotSetFlag(boolean _bodyNotSetFlag) {
        this.bodyNotSetFlag = _bodyNotSetFlag;
    }

    public MgnlEmail(Session _session) {
        super(_session);
    }

    public abstract void setBody(String body, Map _parameters) throws Exception;
    
    public void setSubject(String arg0) throws MessagingException {
        this.setSubject(arg0, "UTF-8");
    }

    public void setTemplate(String _template) {
        this.template = _template;
    }

    public Map getParameters() {
        return this.parameters;
    }

    public String getTemplate() {
        return this.template;
    }

    public void setParameters(Map _parameters) {
        this.parameters = _parameters;
    }

    public void addParameters(Map params) {
        this.parameters.putAll(params);
    }

    public void setBody() throws Exception {
        if (this.template != null) {
            this.setBody(this.template, this.parameters);
        }
    }

    /**
     * @noinspection MethodOverloadsMethodOfSuperclass
     */
    public void setFrom(String _from) {
        try {
            InternetAddress address;
            Matcher matcher = MgnlEmail.EMAIL_WITH_PERSONAL_PATTERN.matcher(_from);
            if (matcher.matches()) {
                String email = matcher.group(2);
                String personal = matcher.group(1);
                address = new InternetAddress(email, personal, "UTF8");
            } else {
                address = new InternetAddress(_from);
            }
            this.setFrom(address);
        }
        catch (Exception e) {
            log.error("Could not set from field of email:" + e.getMessage());
        }
    }

    public void setCharsetHeader(String charset) throws MailException {
        try {
            StringBuffer contentType = new StringBuffer(this.getHeader(CONTENT_TYPE, TEXT_PLAIN_UTF));
            int index = contentType.lastIndexOf(";");
            if (index != -1) {
                contentType.substring(0, index);
            }
            contentType.append(CHARSET_HEADER_STRING).append(charset);
        }
        catch (Exception e) {
            throw new MailException("Content type is not set. Set the content type before setting the charset");
        }
    }

    public void setToList(String list) throws Exception {
        setRecipients(Message.RecipientType.TO, createAdressList(list));
    }

    public void setCcList(String list) throws Exception {
        setRecipients(Message.RecipientType.CC, createAdressList(list));
    }

    public void setBccList(String list) throws Exception {
        setRecipients(Message.RecipientType.BCC, createAdressList(list));
    }
    
    public void setReplyToList(String list) throws Exception {
        setReplyTo(createAdressList(list));
    }

    private Address[] createAdressList(String adresses) throws AddressException {
        if (adresses == null || adresses.equals(StringUtils.EMPTY)) {
            return new Address[0];
        }
        String[] toObj = adresses.split("\n");
        List atos = new ArrayList();
        for (int i = 0; i < toObj.length; i++) {
            try {
                atos.add(new InternetAddress(toObj[i]));
            } catch (AddressException e) {
                log.warn("Error while parsing address.", e);
            }
        }
        return (Address[]) atos.toArray(new Address[atos.size()]);
    }

    public void setAttachments(List list) throws MailException {
        if (list == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Set attachments [" + list.size() + "] for mail: [" + this.getClass().getName() + "]");
        }
        for (int i = 0; i < list.size(); i++) {
            addAttachment((MailAttachment) list.get(i));
        }
    }

    public MimeBodyPart addAttachment(MailAttachment attachment) throws MailException {
        throw new MailException("Cannot add attachment to this email. It is not a Multimime email");
    }

    public void setBodyFromResourceFile(String resourceFile, Map map) throws Exception {
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
