package info.magnolia.cms.mail;

import info.magnolia.cms.mail.templates.MailAttachment;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AttachmentMailTest extends AbstractMailTest {

    Logger log = LoggerFactory.getLogger(AttachmentMailTest.class);

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
}
