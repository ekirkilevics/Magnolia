package info.magnolia.module.owfe;

import info.magnolia.cms.util.MailHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One time usage thread to send an async email
 *
 * @author niko
 */
public class MailSender implements Runnable {
    static Logger logt = LoggerFactory.getLogger(MailSender.class);

    private String smtpHost = "localhost";
    private String from = "MagnoliaWorkflow";
    private String subject = "Workflow Request";
    private String list = "niko@macnica.com";
    private String pathSelected;

    public MailSender(String pathSelectedP) {
        this.pathSelected = pathSelectedP;
    }

    public void run() {
        try {
            MailHandler mh = new MailHandler(smtpHost, 1, 0);
            mh.setFrom(from);
            mh.setSubject(subject);
            mh.setToList(list);
            mh.setBody("The following page is waiting for approval" + pathSelected);
            mh.sendMail();
        } catch (Exception e) {
            logt.error("Could not send email", e);
        }
    }
}