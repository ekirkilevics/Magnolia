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
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class HtmlMailTest extends TestCase implements TestConstants {
    Logger log = LoggerFactory.getLogger(HtmlMailTest.class);
    MgnlMailFactory factory = MgnlMailFactory.getInstance();
    SimpleMailHandler handler = new SimpleMailHandler();

    static int count = 0;

    private String getTestMailSubject() {
        count ++;
        return "Test HTML [" + (count) + "]";
    }


    public void testHtmlMail() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        email.setSubject(getTestMailSubject());
        email.setBody("<h1>Helloniko</h1>", null);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        //handler.prepareAndSendMail(email);
    }

    public void testHtmlMailWithImageURL() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        email.setBody("<h1>Helloniko</h1><img src=\"cid:att\"/>", null);
        email.addAttachment(new MailAttachment(new URL(TEST_URL_IMAGE), "att"));
        email.setSubject(getTestMailSubject());
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        //handler.prepareAndSendMail(email);
    }

    public void testHtmlMailWithImageFile() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        email.addAttachment(new MailAttachment(new File(TEST_FILE), "att"));
        email.setBody("<h1>Helloniko</h1><img src=\"cid:att\"/>", null);
        email.setSubject(getTestMailSubject());
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        //handler.prepareAndSendMail(email);
    }

    public void testHtmlMailWithTwoEmbeddedContent() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        ArrayList attach = new ArrayList();
        attach.add(new MailAttachment(new File(TEST_FILE), "att1"));
        attach.add(new MailAttachment(new URL(TEST_URL_IMAGE), "att2"));
        HashMap param = new HashMap(1);
        param.put(MailConstants.MAIL_ATTACHMENT, attach);
        email.setSubject(getTestMailSubject());
        email.setBody("<h1>Helloniko</h1><img src=\"cid:att1\"/><img src=\"cid:att2\"/>", param);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        //handler.prepareAndSendMail(email);
    }

    public void testHtmlMailWithPdf() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        email.setSubject(getTestMailSubject());
        email.setBody("<h1>Helloniko in pdf</h1>", new HashMap(0));
        email.addAttachment(new MailAttachment(new File(TEST_FILE_PDF), "att1"));
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        //handler.prepareAndSendMail(email);
    }
}