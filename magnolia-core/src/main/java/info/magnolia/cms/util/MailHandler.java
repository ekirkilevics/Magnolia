/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;


/**
 * This is a simple util class to send email from the form pages ... This class was previously included inside the jsp
 * related to the form.
 * @author niko
 * @version $Id $
 */
public class MailHandler {

    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(MailHandler.class);

    private String body;

    private String subject;

    private String from;

    private InternetAddress[] toList;

    private InternetAddress[] ccList;

    private Session session;

    public MailHandler(String mailHost, int toListLength, int ccListLength) throws Exception {
        toList = new InternetAddress[toListLength];
        ccList = new InternetAddress[ccListLength];
        Properties props = System.getProperties();
        props.put("mail.smtp.host", mailHost);
        session = Session.getInstance(props, null);
    }

    public void sendMail() throws MessagingException {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(this.getFrom()));
            message.setRecipients(Message.RecipientType.TO, this.getToList());
            message.setRecipients(Message.RecipientType.CC, this.getCcList());
            message.setSubject(subject);

            message.setContent(body, "text/plain; charset=UTF-8");
            message.setHeader("Content-Type", "text/plain; charset=UTF-8");

            Transport.send(message);
            log.info("Mail has been sent to: [" + addressesToString(this.getToList()) + "]");
        }
        catch (MessagingException e) {
            log.error("Email to: ["
                + addressesToString(this.getToList())
                + "] was not sent because of the following error: "
                + e.getMessage(), e);
            throw e;
        }
        catch (RuntimeException e) {
            // this is here in order to catch UnsupportedOperationException
            // by alternative javamail libraries
            log.error("Email to: ["
                + addressesToString(this.getToList())
                + "] was not sent because of the following error: "
                + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Convert list of addresses to a nice string
     * @param addresses list of internet addresses
     * @return a comma-separated list of email addresses as a <code>String</code>
     */
    public String addressesToString(InternetAddress[] addresses) {
        if (addresses == null)
            return "";
        int length = addresses.length;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            if (i != 0)
                sb.append(",");
            sb.append(addresses[i].getAddress());
        }
        return sb.toString();
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom() {
        return this.from;
    }

    public void setToList(String to) throws AddressException {
        String[] toObj = to.split("\n");
        for (int i = 0; i < toObj.length; i++) {
            this.toList[i] = new InternetAddress(toObj[i]);
        }
    }

    public InternetAddress[] getToList() {
        return this.toList;
    }

    public void setCcList(String to) throws AddressException {
        String[] toObj = to.split("\n");
        for (int i = 0; i < toObj.length; i++) {
            this.ccList[i] = new InternetAddress(toObj[i]);
        }
    }

    public InternetAddress[] getCcList() {
        return this.ccList;

    }
}
