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
package info.magnolia.module.mail.commands;

import info.magnolia.context.WebContext;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.mail.MailModule;
import info.magnolia.module.mail.MailTemplate;
import info.magnolia.module.mail.MgnlMailFactory;
import info.magnolia.module.mail.templates.MailAttachment;
import info.magnolia.module.mail.templates.MgnlEmail;
import info.magnolia.module.mail.util.MailUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the command for sending mail.
 * @author jackie
 * @author niko
 */
public class MailCommand implements Command {

    public static Logger log = LoggerFactory.getLogger(MailCommand.class);

    public boolean execute(Context ctx) {
        if (log.isDebugEnabled()) {
            log.debug("starting sending mail");
        }

        try {
            MgnlMailFactory factory = MailModule.getInstance().getFactory();
            MgnlEmail email;
            if (log.isDebugEnabled())
                log.debug(Arrays.asList(ctx.entrySet().toArray()).toString());

            String template = (String) ctx.get("mailTemplate");
            //get parameters from mail page parameter text area
            if(ctx.containsKey(MailTemplate.MAIL_PARAMETERS)) {
                Map<String, String> temp = MailUtil.convertToMap((String)ctx.get(MailTemplate.MAIL_PARAMETERS));
                ctx.putAll(temp);
            }

            //find attachments in parameters or form if we are using one
            List<MailAttachment> attachments = null;

            if(ctx instanceof WebContext) {
                attachments = MailUtil.createAttachmentList(((WebContext)ctx).getParameters());
            }

            if (StringUtils.isNotEmpty(template)) {
                log.debug("Command using mail template: " + template);

                email = factory.getEmailFromTemplate(template, attachments, ctx);
                email.setBodyFromResourceFile();
            }
            else {
                log.debug("command using static parameters");

                email = factory.getEmail(ctx, attachments);
                if(StringUtils.isEmpty(email.getTemplate().getTemplateFile())) {
                    email.setBody();
                } else {
                    email.setBodyFromResourceFile();
                }

            }
            factory.getEmailHandler().sendMail(email);

            log.info("send mail successfully to:" + email.getTemplate().getTo());
        }
        catch (Exception e) {
            log.debug("Could not send email:" + e.getMessage(), e);
            log.error("Could not send email:" + e.getMessage());
            AlertUtil.setMessage("Error: " + e.getMessage());
            return false;
        }

        return true;
    }

}
