/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.mail.templates.impl;

import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.module.mail.MailTemplate;
import info.magnolia.module.mail.templates.MgnlMultipartEmail;

import javax.mail.MessagingException;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Sends an email using a freemarker template.
 * Date: Apr 5, 2006 Time: 8:59:18 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class FreemarkerEmail extends MgnlMultipartEmail {

    public FreemarkerEmail(MailTemplate template) {
        super(template);
    }

    public void setFrom(String from) {
        try {
            from = proccesFreemarkerString(from);
        } catch (Exception e) {
            log.error("Couldn't set from: " + from);
        }
        super.setFrom(from);
    }

    public void setSubject(String subject) throws MessagingException {
        try {
            subject = proccesFreemarkerString(subject);
            super.setSubject(subject);
        } catch (Exception e) {
            throw new MessagingException();
        }

    }

    public void setToList(String list) throws Exception {
        list = proccesFreemarkerString(list);
        super.setToList(list);
    }

    public void setBody(String body) throws Exception {
        body = proccesFreemarkerString(body);
        super.setBody(body);
    }


    public void setBodyFromResourceFile() throws Exception {
        final StringWriter writer = new StringWriter();
        FreemarkerHelper.getInstance().render(super.getTemplate().getTemplateFile(), super.getTemplate().getParameters(), writer);
        super.setBody(writer.toString());
    }

    protected String proccesFreemarkerString(String text) throws Exception {

        Reader reader = new StringReader(text);
        final StringWriter writer = new StringWriter();
        FreemarkerHelper.getInstance().render(reader, super.getTemplate().getParameters(), writer);
        reader.close();
        return writer.toString();
    }


}
