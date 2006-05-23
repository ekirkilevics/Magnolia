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
package info.magnolia.module.owfe.commands.simple;

import info.magnolia.cms.mail.MgnlMailFactory;
import info.magnolia.cms.mail.handlers.MgnlMailHandler;
import info.magnolia.cms.mail.templates.MgnlEmail;
import info.magnolia.commands.ContextAttributes;
import info.magnolia.module.owfe.WorkflowConstants;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
            String mailTo = (String) ctx.get(ContextAttributes.P_MAILTO);;
            ctx.put("user",mailTo);
            if (log.isDebugEnabled()) {
                log.debug("mail receiver list: " + mailTo);
            }
            String mailTemplate = (String) ctx.get(ContextAttributes.P_MAILTEMPLATE);
            log.info("mail template: " + mailTemplate);

            MgnlMailFactory factory = MgnlMailFactory.getInstance();
            MgnlMailHandler handler = factory.getEmailHandler();
            // FIXME
            // MgnlEmail email = factory.getEmailFromTemplate(mailTemplate, ctx);
            MgnlEmail email= null;
            email.setFrom(WorkflowConstants.WORKFLOW_EMAIL_FROM_FIELD);
            email.setSubject(WorkflowConstants.WORKFLOW_EMAIL_SUBJECT_FIELD);
            // FIXME
            //email.setParameters(params);
            email.setToList(factory.convertEmailList(mailTo));
            handler.prepareAndSendMail(email);

            log.info("send mail successfully to:" + mailTo);
        }
        catch (Exception e) {
            log.error("Could not send email:"+e.getMessage());
        }

        return false;
    }

}
