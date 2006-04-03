package info.magnolia.cms.mail;
/**
 * 
 * Date: Mar 31, 2006
 * Time: 9:14:00 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */

import info.magnolia.cms.mail.handlers.SimpleMailHandler;
import info.magnolia.cms.mail.templates.MailAttachment;
import info.magnolia.cms.mail.templates.MgnlEmail;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MgnlMailFactoryTest extends TestCase {
    Logger log = LoggerFactory.getLogger(MgnlMailFactoryTest.class);
    MgnlMailFactory factory = MgnlMailFactory.getInstance();
    SimpleMailHandler handler = new SimpleMailHandler();

    public final static String TEST_RECIPIENT = "niko@macnica.com";
    public final static String TEST_SENDER = "hellonico@hotmail.com";
    public static final String TEST_URL = "http://freemarker.sourceforge.net/images/logo_e0e0e0.png";
    public static final String TEST_FILE = TestUtil.getResourcePath("magnolia.jpg");
    public static final String TEST_FILE_PDF = TestUtil.getResourcePath("magnolia.pdf");
    public static final String TEST_URL_IMAGE = "http://www.sushicam.com/Pics/sakura.jpg";

    public void testResourcePath() throws Exception {
        log.info(TestUtil.getResourceRootFolder().getAbsolutePath());
    }

    public void testAttachmentFile() throws Exception {
        String file = TEST_FILE;
        MailAttachment att = new MailAttachment(new File(file), "att");
        URL url = att.getURL();
        log.info(url.toString());
        File f = att.getFile();
        log.info(f.getAbsolutePath());
        assertTrue(f.exists());
        assertEquals(f.getAbsolutePath(), file);
    }

    public void testAttachmentUrl() throws Exception {
        String urlString = TEST_URL;
        MailAttachment att = new MailAttachment(new URL(urlString), "att");
        URL url = att.getURL();
        log.info(url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        int len = con.getContentLength();
        assertTrue(len > 0);
    }

    public void testStaticMail() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_STATIC);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        handler.prepareAndSendMail(email);
    }

    public void testSimpleMail() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_SIMPLE);
        email.setText("Hello bonjour");
        email.setSubject("Test simple");
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        handler.prepareAndSendMail(email);
    }

    public void testHtmlMail() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        email.setBody("<h1>Helloniko</h1>", null);
        email.setSubject("Test Html");
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        handler.prepareAndSendMail(email);
    }

    public void testHtmlMailWithImageURL() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        email.setBody("<h1>Helloniko</h1><img src=\"cid:att\"/>", null);
        email.addAttachment(new MailAttachment(new URL(TEST_URL_IMAGE), "att"));
        email.setSubject("Test Html");
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        handler.prepareAndSendMail(email);
    }

    public void testHtmlMailWithImageFile() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        email.addAttachment(new MailAttachment(new File(TEST_FILE), "att"));
        email.setBody("<h1>Helloniko</h1><img src=\"cid:att\"/>", null);
        email.setSubject("Test Html");
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        handler.prepareAndSendMail(email);
    }

    public void testHtmlMailWithTwoEmbeddedContent() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        ArrayList attach = new ArrayList();
        attach.add(new MailAttachment(new File(TEST_FILE), "att1"));
        attach.add(new MailAttachment(new URL(TEST_URL_IMAGE), "att2"));
        HashMap param = new HashMap(1);
        param.put(MailConstants.MAIL_ATTACHMENT, attach);
        email.setBody("<h1>Helloniko</h1><img src=\"cid:att1\"/><img src=\"cid:att2\"/>", param);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        handler.prepareAndSendMail(email);
    }

    public void testHtmlMailWithPdf() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        email.setBody("<h1>Helloniko in pdf</h1>", new HashMap(0));
        email.addAttachment(new MailAttachment(new File(TEST_FILE_PDF), "att1"));
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);
        handler.prepareAndSendMail(email);
    }


}