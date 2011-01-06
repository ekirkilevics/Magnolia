/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.module.mail.MailTemplate;
import info.magnolia.module.mail.templates.MgnlMultipartEmail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringWriter;
import java.net.URL;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;


/**
 * Sends an email using a velocity template.
 * Date: Mar 30, 2006 Time: 1:13:33 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class VelocityEmail extends MgnlMultipartEmail {

    static {
        try {
            Velocity.init();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public VelocityEmail(MailTemplate template) {
        super(template);
    }

    public void setBodyFromResourceFile() throws Exception {
        String resourceFile = this.getTemplate().getTemplateFile();
        VelocityContext context = new VelocityContext(super.getTemplate().getParameters());
        URL url = this.getClass().getResource("/" + resourceFile);
        log.info("This is the url:" + url);
        BufferedReader br = new BufferedReader(new FileReader(url.getFile()));
        StringWriter w = new StringWriter();
        Velocity.evaluate(context, w, "email", br);
        super.setBody(w.toString());
    }
}
