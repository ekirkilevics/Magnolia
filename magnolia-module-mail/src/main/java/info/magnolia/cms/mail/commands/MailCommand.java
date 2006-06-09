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
import info.magnolia.cms.mail.templates.MgnlEmail;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;


/**
 * the command for sending mail
 * @author jackie
 */
public class MailCommand implements Command {

    static Logger log = LoggerFactory.getLogger(MailCommand.class);

    public boolean execute(Context ctx) {
        if (log.isDebugEnabled()) {
            log.debug("starting sending mail");
        }

        try {
            String mailTo = (String) ctx.get(MailConstants.ATTRIBUTE_MAILTO);;
            ctx.put("user",mailTo);
            if (log.isDebugEnabled()) {
                log.debug("mail receiver list: " + mailTo);
            }
            String mailTemplate = (String) ctx.get(MailConstants.ATTRIBUTE_MAILTEMPLATE);
            log.info("mail template: " + mailTemplate);

            MgnlMailFactory factory = MgnlMailFactory.getInstance();
            MgnlMailHandler handler = factory.getEmailHandler();
            log.info(ctx.toString());

            MgnlEmail email = factory.getEmailFromTemplate(mailTemplate, ctx);
            email.setFrom(MailConstants.WORKFLOW_EMAIL_FROM_FIELD);
            email.setSubject(MailConstants.WORKFLOW_EMAIL_SUBJECT_FIELD);
            email.setToList(factory.convertEmailList(mailTo));
            handler.prepareAndSendMail(email);

            /*String template = (String)ctx.get("template");
            String to = (String)ctx.get("to");
            String cc = (String)ctx.get()

            if (StringUtils.isNotEmpty(template)) {
                MgnlEmail email = factory.getEmailFromTemplate(template, ctx);
                email.setToList(factory.convertEmailList(to));
                email.setCcList(factory.convertEmailList(cc));
                handler.prepareAndSendMail(email);
            }
            else {
                MgnlEmail email = factory.getEmailFromType(type);
                email.setFrom(from);
                email.setSubject(subject);
                email.setToList(factory.convertEmailList(to));
                email.setBody(text, map);
                if (attachment != null) {
                    email.addAttachment(attachment);
                }
                email.setCcList(factory.convertEmailList(cc));
                handler.prepareAndSendMail(email);
            }*/

            log.info("send mail successfully to:" + mailTo);
        }
        catch (Exception e) {
            log.error("Could not send email:"+e.getMessage());
        }

        return false;
    }

}
