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
package info.magnolia.cms.mail.commands;

import info.magnolia.cms.mail.MailConstants;
import info.magnolia.cms.mail.MgnlMailFactory;
import info.magnolia.cms.mail.handlers.MgnlMailHandler;
import info.magnolia.cms.mail.templates.MailAttachment;
import info.magnolia.cms.mail.templates.MgnlEmail;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the command for sending mail
 * @author jackie
 * @author niko
 */
public class MailCommand implements Command {

    static Logger log = LoggerFactory.getLogger(MailCommand.class);

    public boolean execute(Context ctx) {
        if (log.isDebugEnabled()) {
            log.debug("starting sending mail");
        }

        try {
            MgnlMailFactory factory = MgnlMailFactory.getInstance();
            MgnlMailHandler handler = factory.getEmailHandler();

            if (log.isDebugEnabled())
                log.debug(Arrays.asList(ctx.entrySet().toArray()).toString());

            String template = (String) ctx.get(MailConstants.ATTRIBUTE_TEMPLATE);
            String to = (String) ctx.get(MailConstants.ATTRIBUTE_TO);
            String cc = (String) ctx.get(MailConstants.ATTRIBUTE_CC);

            if (StringUtils.isNotEmpty(template)) {
                log.info("Command using mail template: " + template);
                MgnlEmail email = factory.getEmailFromTemplate(template, ctx);
                email.setToList(factory.convertEmailList(to));
                email.setCcList(factory.convertEmailList(cc));
                handler.prepareAndSendMail(email);
            }
            else {
                log.info("command using static parameters");
                String from = (String) ctx.get(MailConstants.ATTRIBUTE_FROM);
                String type = (String) ctx.get(MailConstants.ATTRIBUTE_TYPE);
                String subject = (String) ctx.get(MailConstants.ATTRIBUTE_SUBJECT);
                String text = (String) ctx.get(MailConstants.ATTRIBUTE_TEXT);

                MgnlEmail email = factory.getEmailFromType(type);
                email.setFrom(from);
                email.setSubject(subject);
                email.setToList(factory.convertEmailList(to));
                email.setBody(text, ctx);

                Object attachment = ctx.get(MailConstants.ATTRIBUTE_ATTACHMENT);

                if (attachment != null) {
                    if(attachment instanceof MailAttachment) {
                        email.addAttachment((MailAttachment)attachment);
                    }
                    else if(attachment instanceof List) {
                        email.setAttachments((List) attachment);
                    }
                }

                email.setCcList(factory.convertEmailList(cc));
                handler.prepareAndSendMail(email);
            }

            log.info("send mail successfully to:" + to);
        }
        catch (Exception e) {
            log.error("Could not send email:" + e.getMessage());
        }

        return false;
    }

}
