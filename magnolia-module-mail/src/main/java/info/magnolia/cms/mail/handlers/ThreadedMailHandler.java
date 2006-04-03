package info.magnolia.cms.mail.handlers;

import info.magnolia.cms.mail.templates.MgnlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Transport;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class is meant to allow async emails ... The mail is posted and send. Note that mail success or failure is only
 * displayed in the logs.
 * Date: Mar 31, 2006
 * Time: 5:51:38 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class ThreadedMailHandler implements MgnlMailHandler {
    Logger log = LoggerFactory.getLogger(ThreadedMailHandler.class);
    ArrayList emails = new ArrayList();
    MailThread thread; // can be replaced with a pool of thread someday

    public ThreadedMailHandler() {
        thread = new MailThread();
        Thread bb = new Thread(thread);
        bb.start();
    }

    protected void finalize() throws Throwable {
        thread.setStop(true);
        super.finalize();
    }


    /**
     * Prepare the email (format it) and send it
     *
     * @param email the email to send
     * @throws Exception if fails
     */
    public void prepareAndSendMail(MgnlEmail email) throws Exception {
        email.setBodyNotSetFlag(true);
        synchronized (emails) {
            emails.add(email);
        }
        thread.notify();
    }

    /**
     * Send the email as is, without touching it
     *
     * @param email the email to send
     * @throws Exception if fails
     */
    public void sendMail(MgnlEmail email) throws Exception {
        synchronized (emails) {
            emails.add(email);
        }
        thread.notify();
    }

    /**
     * Thread doing all the job for sending emails and formatting the body
     */
    class MailThread implements Runnable {
        boolean stop = false;

        public void run() {
            while (!stop) {
                if (emails.size() == 0) { // nothing to do just sleep
                    synchronized (this) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            log.info("Mail Thread was interrupted");
                        }
                    }
                } else {
                    MgnlEmail email = null;
                    synchronized (emails) {
                        if (emails.size() > 0) {
                            email = (MgnlEmail) emails.remove(0);
                        }
                    }
                    if (email != null) {
                        try {
                            if (email.isBodyNotSetFlag())
                                email.setBody();
                            try {
                                Transport.send(email);
                                log.info("Mail has been sent to: [" + Arrays.toString(email.getAllRecipients()) + "]");
                            }
                            catch (Exception e) {
                                log.error("Email to: [" + Arrays.toString(email.getAllRecipients()) + "] was not sent because of an error", e);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }

        public boolean isStop() {
            return stop;
        }

        public void setStop(boolean stop) {
            this.stop = stop;
            this.notify();
        }
    }
}
