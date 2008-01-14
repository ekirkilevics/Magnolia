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
package info.magnolia.cms.mail.templates.impl;

import info.magnolia.cms.mail.MailException;
import info.magnolia.cms.mail.templates.MailAttachment;
import info.magnolia.cms.mail.templates.MgnlMultipartEmail;

import java.util.List;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;


/**
 * Date: Mar 30, 2006 Time: 2:12:53 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class HtmlEmail extends MgnlMultipartEmail {

    public static final String MAIL_ATTACHMENT = "attachment";

    public HtmlEmail(Session _session) throws Exception {
        super(_session);
        this.setHeader(CONTENT_TYPE, TEXT_HTML_UTF);
    }

    public void setBody(String body, Map parameters) throws Exception {
        // check if multipart
        if (!isMultipart()) { // it is not a multipart yet, just set the text for content
            this.setContent(body, TEXT_HTML_UTF);
        }
        else { // some attachment are already in this mail. Init the body part to set the main text
            // Create your new message part
            BodyPart messageBodyPart = new MimeBodyPart();
            // Set the _content of the body part
            messageBodyPart.setContent(body, TEXT_HTML_UTF);
            // Add body part to multipart
            this.multipart.addBodyPart(messageBodyPart, 0);
            this.setContent(this.multipart);
        }

        // process the attachments
        if (parameters != null && parameters.containsKey(MAIL_ATTACHMENT)) {
            Object attachment = parameters.get(MAIL_ATTACHMENT);
            if(attachment instanceof MailAttachment) {
                addAttachment((MailAttachment)attachment);
            }
            else if(attachment instanceof List) {
                setAttachments((List) attachment);
            }
        }
    }

    private void turnOnMultipart() {
        try {
            Object o = this.getContent();
            if (o instanceof String) {
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(o, TEXT_HTML_UTF);
                this.multipart.addBodyPart(messageBodyPart, 0);
                this.setContent(this.multipart);
            }
        }
        catch (Exception e) {
            log.info("Could not turn on multipart");
        }
    }

    public MimeBodyPart addAttachment(MailAttachment attachment) throws MailException {
        if (!isMultipart()) {
            turnOnMultipart();
        }
        return super.addAttachment(attachment);
    }

}
