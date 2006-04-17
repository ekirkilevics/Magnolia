package info.magnolia.cms.mail;

/**
 *
 * Date: Apr 3, 2006
 * Time: 10:51:50 AM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */

import info.magnolia.cms.mail.handlers.SimpleMailHandler;
import info.magnolia.cms.mail.templates.MailAttachment;
import info.magnolia.cms.mail.templates.MgnlEmail;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;


public class HtmlMailTest extends TestCase implements TestConstants {

    Logger log = LoggerFactory.getLogger(HtmlMailTest.class);

    MgnlMailFactory factory;

    SimpleMailHandler handler;

    SimpleSmtpServer server;

    static int count = 0;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {

        super.setUp();
        handler = new SimpleMailHandler();
        factory = MgnlMailFactory.getInstance();
        Map parameters = factory.getMailParameters();
        parameters.put(MailConstants.SMTP_SERVER, "localhost");
        parameters.put(MailConstants.SMTP_PORT, new Integer(1025));

        server = SimpleSmtpServer.start(1025);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    private String getTestMailSubject() {
        count++;
        return "Test HTML [" + (count) + "]";
    }

    public void testHtmlMail() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        String subject = getTestMailSubject();
        email.setSubject(subject);
        email.setBody("<h1>Helloniko</h1>", null);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);

        handler.prepareAndSendMail(email);

        assertTrue(server.getReceivedEmailSize() == 1);
        Iterator emailIter = server.getReceivedEmail();
        SmtpMessage message = (SmtpMessage) emailIter.next();
        assertTrue(message.getHeaderValue("Subject").equals(subject));

    }

    public void testHtmlMailWithImageURL() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        email.setBody("<h1>Helloniko</h1><img src=\"cid:att\"/>", null);
        email.addAttachment(new MailAttachment(new URL(TEST_URL_IMAGE), "att"));
        String subject = getTestMailSubject();
        email.setSubject(subject);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);

        handler.prepareAndSendMail(email);

        assertTrue(server.getReceivedEmailSize() == 1);
        Iterator emailIter = server.getReceivedEmail();
        SmtpMessage message = (SmtpMessage) emailIter.next();
        assertTrue(message.getHeaderValue("Subject").equals(subject));
    }

    public void testHtmlMailWithImageFile() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        email.addAttachment(new MailAttachment(new File(TEST_FILE), "att"));
        email.setBody("<h1>Helloniko</h1><img src=\"cid:att\"/>", null);
        String subject = getTestMailSubject();
        email.setSubject(subject);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);

        handler.prepareAndSendMail(email);

        assertTrue(server.getReceivedEmailSize() == 1);
        Iterator emailIter = server.getReceivedEmail();
        SmtpMessage message = (SmtpMessage) emailIter.next();
        assertTrue(message.getHeaderValue("Subject").equals(subject));
    }

    public void testHtmlMailWithTwoEmbeddedContent() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        ArrayList attach = new ArrayList();
        attach.add(new MailAttachment(new File(TEST_FILE), "att1"));
        attach.add(new MailAttachment(new URL(TEST_URL_IMAGE), "att2"));
        HashMap param = new HashMap(1);
        param.put(MailConstants.MAIL_ATTACHMENT, attach);
        String subject = getTestMailSubject();
        email.setSubject(subject);
        email.setBody("<h1>Helloniko</h1><img src=\"cid:att1\"/><img src=\"cid:att2\"/>", param);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);

        handler.prepareAndSendMail(email);

        assertTrue(server.getReceivedEmailSize() == 1);
        Iterator emailIter = server.getReceivedEmail();
        SmtpMessage message = (SmtpMessage) emailIter.next();
        assertTrue(message.getHeaderValue("Subject").equals(subject));
    }

    public void testHtmlMailWithPdf() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        String subject = getTestMailSubject();
        email.setSubject(subject);
        email.setBody("<h1>Helloniko in pdf</h1>", new HashMap(0));
        email.addAttachment(new MailAttachment(new File(TEST_FILE_PDF), "att1"));
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);

        handler.prepareAndSendMail(email);

        assertTrue(server.getReceivedEmailSize() == 1);
        Iterator emailIter = server.getReceivedEmail();
        SmtpMessage message = (SmtpMessage) emailIter.next();
        assertTrue(message.getHeaderValue("Subject").equals(subject));
    }
}