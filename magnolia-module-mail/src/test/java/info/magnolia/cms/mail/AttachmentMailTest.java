package info.magnolia.cms.mail;

import info.magnolia.cms.mail.templates.MailAttachment;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AttachmentMailTest extends AbstractMailTest {

    Logger log = LoggerFactory.getLogger(AttachmentMailTest.class);

    public void testAttachmentFile() throws Exception {
        String file = TEST_FILE;
        MailAttachment att = new MailAttachment(new File(file).toURL(), "att");
        URL url = att.getURL();
        log.info(url.toString());
        File f = att.getFile();
        log.info(f.getAbsolutePath());
        assertTrue(f.exists());
        assertEquals(f.getAbsolutePath(), file);
    }

}
