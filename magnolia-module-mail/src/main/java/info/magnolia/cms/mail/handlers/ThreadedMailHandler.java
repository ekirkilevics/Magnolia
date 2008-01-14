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
package info.magnolia.cms.mail.handlers;

import info.magnolia.cms.mail.templates.MgnlEmail;

import java.util.ArrayList;
import java.util.Arrays;

import javax.mail.Transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is meant to allow async emails ... The mail is posted and send. Note that mail success or failure is only
 * displayed in the logs. Date: Mar 31, 2006 Time: 5:51:38 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class ThreadedMailHandler implements MgnlMailHandler {

    Logger log = LoggerFactory.getLogger(ThreadedMailHandler.class);

    ArrayList emails = new ArrayList();

    MailThread thread; // can be replaced with a pool of thread someday

    private ThreadedMailHandler() {
        this.thread = new MailThread();
        Thread bb = new Thread(this.thread);
        bb.start();
    }

    protected void finalize() throws Throwable {
        this.thread.setStop(true);
        super.finalize();
    }

    /**
     * Prepare the email (format it) and send it
     * @param email the email to send
     * @throws Exception if fails
     */
    public void prepareAndSendMail(MgnlEmail email) throws Exception {
        email.setBodyNotSetFlag(true);
        synchronized (this) {
            this.emails.add(email);
        }
        this.thread.notify();
    }

    /**
     * Send the email as is, without touching it
     * @param email the email to send
     * @throws Exception if fails
     */
    public void sendMail(MgnlEmail email) throws Exception {
        synchronized (this) {
            this.emails.add(email);
        }
        this.thread.notify();
    }

    /**
     * Thread doing all the job for sending emails and formatting the body
     */
    class MailThread implements Runnable {

        boolean stop = false;

        public void run() {
            while (!this.stop) {
                if (ThreadedMailHandler.this.emails.size() == 0) { // nothing to do just sleep
                    synchronized (this) {
                        try {
                            wait();
                        }
                        catch (InterruptedException e) {
                            ThreadedMailHandler.this.log.info("Mail Thread was interrupted");
                        }
                    }
                }
                else {
                    MgnlEmail email = null;
                    synchronized (this) {
                        if (ThreadedMailHandler.this.emails.size() > 0) {
                            email = (MgnlEmail) ThreadedMailHandler.this.emails.remove(0);
                        }
                    }
                    if (email != null) {
                        try {
                            if (email.isBodyNotSetFlag()) {
                                email.setBody();
                            }
                            try {
                                Transport.send(email);
                                ThreadedMailHandler.this.log.info("Mail has been sent to: ["
                                    + Arrays.asList(email.getAllRecipients())
                                    + "]");
                            }
                            catch (Exception e) {
                                ThreadedMailHandler.this.log.error("Email to: ["
                                    + Arrays.asList(email.getAllRecipients())
                                    + "] was not sent because of an error", e);
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }

        public boolean isStop() {
            return this.stop;
        }

        public void setStop(boolean stop) {
            this.stop = stop;
            this.notify();
        }
    }
}
