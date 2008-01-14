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
package info.magnolia.cms.mail.templates;

import info.magnolia.cms.mail.MailException;

import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;


/**
 * Date: Apr 1, 2006 Time: 9:00:35 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public abstract class MgnlMultipartEmail extends MgnlEmail {

    private static final String CONTENT_ID = "Content-ID";

    private static final String RELATED = "related";

    protected MimeMultipart multipart;

    protected MgnlMultipartEmail(Session _session) {
        super(_session);
        // Create a related multi-part to combine the parts
        this.multipart = new MimeMultipart(RELATED);
    }

    public boolean isMultipart() {
        try {
            int count = this.multipart.getCount();
            return (count > 0);
        }
        catch (MessagingException e) {
            return false;
        }
    }

    public MimeMultipart getMailMultipart() {
        return this.multipart;
    }

    public MimeBodyPart addAttachment(MailAttachment attachment) throws MailException {

        try {
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            String key = attachment.getName();
            log.info("Found new attachment with name :" + key);

            // get info on the attachment
            URL url = attachment.getURL();
            String name = attachment.getFileName();
            String contentType = attachment.getContentType();

            // set the disposition of the file.
            messageBodyPart.setDisposition(attachment.getDisposition() + "; filename=\"" + name + "\"");

            // Fetch the image and associate to part
            DataSource fds = url.getProtocol().startsWith("file:")
                ? (DataSource) new FileDataSource(url.getFile())
                : (DataSource) new URLDataSource(url);
            // DataSource fd = new FileDataSource(file);
            messageBodyPart.setDataHandler(new DataHandler(fds));
            // Add a header to connect to the HTML

            // MAGNOLIA-1649 this is really important!
            if (!key.startsWith("<")) {
                key = "<" + key + ">";
            }
            messageBodyPart.setHeader(CONTENT_ID, key);

            // set the header as well as the content type do this AFTER setting the data source
            messageBodyPart.setHeader(CONTENT_TYPE, contentType + "; name=\"" + name + "\"");

            // Add part to multi-part
            this.multipart.addBodyPart(messageBodyPart);

            // Set the content again
            this.setContent(this.multipart);

            return messageBodyPart;
        }
        catch (Exception e) {
            throw new MailException(e.getMessage(), e);
        }
    }

    // public abstract String getContentDescription();
}
