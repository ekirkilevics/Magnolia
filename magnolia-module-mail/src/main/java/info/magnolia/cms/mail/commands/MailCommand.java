/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
