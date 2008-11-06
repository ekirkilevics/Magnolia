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

import info.magnolia.freemarker.FreemarkerHelper;

import javax.mail.MessagingException;
import javax.mail.Session;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Date: Apr 5, 2006 Time: 8:59:18 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class FreemarkerEmail extends HtmlEmail {

    public void setFrom(String _from, Map parameters) throws Exception {
        _from = proccesFreemarkerString(_from, parameters);
        super.setFrom(_from);
    }


    public void setSubject(String subject, Map parameters) throws MessagingException {
        try {
            subject = proccesFreemarkerString(subject, parameters);
            super.setSubject(subject);
        } catch (Exception e) {
            throw new MessagingException();
        }

    }

    public void setToList(String list, Map parameters) throws Exception {
        list = proccesFreemarkerString(list, parameters);
        super.setToList(list);
    }

    public FreemarkerEmail(Session _session) throws Exception {
        super(_session);
    }

    public void setBody(String body, Map parameters, Map attachments) throws Exception {
        body = proccesFreemarkerString(body, parameters);
        super.setBody(body, attachments);
    }


    public void setBodyFromResourceFile(String resourceFile, Map _map) throws Exception {
        final StringWriter writer = new StringWriter();
        FreemarkerHelper.getInstance().render(resourceFile, _map, writer);
        super.setBody(writer.toString(), _map);
    }

    protected String proccesFreemarkerString(String text, Map parameters) throws Exception {

        Reader reader = new StringReader(text);
        final StringWriter writer = new StringWriter();
        FreemarkerHelper.getInstance().render(reader, parameters, writer);
        reader.close();
        return writer.toString();
    }


}
