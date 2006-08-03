package info.magnolia.cms.mail;

import info.magnolia.cms.mail.templates.MailAttachment;
import info.magnolia.cms.mail.templates.MgnlEmail;
import info.magnolia.cms.mail.templates.impl.HtmlEmail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.subethamail.wiser.WiserMessage;


/**
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class HtmlMailTest extends AbstractMailTest {

    public void testHtmlMail() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        String subject = "test html email";
        email.setSubject(subject);
        email.setBody("<h1>Helloniko</h1>", null);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);

        handler.prepareAndSendMail(email);

        assertTrue(wiser.getMessages().size() == 1);
        Iterator emailIter = wiser.getMessages().iterator();
        WiserMessage message = (WiserMessage) emailIter.next();
        assertTrue(message.getMimeMessage().getSubject().equals(subject));
    }

    public void testHtmlMailWithImageFile() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        email.addAttachment(new MailAttachment(new File(getResourcePath(TEST_FILE_JPG)).toURL(), "att"));
        email.setBody("<h1>Helloniko</h1><img src=\"cid:att\"/>", null);
        String subject = "test html email";
        email.setSubject(subject);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);

        handler.prepareAndSendMail(email);

        assertTrue(wiser.getMessages().size() == 1);
        Iterator emailIter = wiser.getMessages().iterator();
        WiserMessage message = (WiserMessage) emailIter.next();
        assertTrue(message.getMimeMessage().getSubject().equals(subject));
    }

    public void testHtmlMailWithTwoEmbeddedContent() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        ArrayList attach = new ArrayList();
        attach.add(new MailAttachment(new File(getResourcePath(TEST_FILE_JPG)).toURL(), "att1"));
        attach.add(new MailAttachment(new File(getResourcePath(TEST_FILE_PDF)).toURL(), "att2"));
        HashMap param = new HashMap(1);
        param.put(HtmlEmail.MAIL_ATTACHMENT, attach);
        String subject = "test html email";
        email.setSubject(subject);
        email.setBody("<h1>Helloniko</h1><img src=\"cid:att1\"/><img src=\"cid:att2\"/>", param);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);

        handler.prepareAndSendMail(email);

        assertTrue(wiser.getMessages().size() == 1);
        Iterator emailIter = wiser.getMessages().iterator();
        WiserMessage message = (WiserMessage) emailIter.next();
        assertTrue(message.getMimeMessage().getSubject().equals(subject));
    }

    public void testHtmlMailWithPdf() throws Exception {
        MgnlEmail email = factory.getEmailFromType(MailConstants.MAIL_TEMPLATE_HTML);
        String subject = "test html email";
        email.setSubject(subject);
        email.setBody("<h1>Helloniko in pdf</h1>", new HashMap(0));
        email.addAttachment(new MailAttachment(new File(getResourcePath(TEST_FILE_PDF)).toURL(), "att1"));
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);

        handler.prepareAndSendMail(email);

        assertTrue(wiser.getMessages().size() == 1);
        Iterator emailIter = wiser.getMessages().iterator();
        WiserMessage message = (WiserMessage) emailIter.next();
        assertTrue(message.getMimeMessage().getSubject().equals(subject));
    }

}
