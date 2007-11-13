/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
