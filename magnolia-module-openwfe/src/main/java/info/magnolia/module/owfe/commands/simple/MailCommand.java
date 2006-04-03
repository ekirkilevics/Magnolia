package info.magnolia.module.owfe.commands.simple;

import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.cms.mail.MgnlMailFactory;
import info.magnolia.cms.mail.handlers.MgnlMailHandler;
import info.magnolia.cms.mail.templates.MgnlEmail;
import info.magnolia.cms.mail.templates.impl.SimpleEmail;
import info.magnolia.module.owfe.MgnlConstants;
import org.apache.commons.chain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class MailCommand extends MgnlCommand {
    static Logger logt = LoggerFactory.getLogger(MailCommand.class);
    static final String[] parameters = {MgnlConstants.P_MAILTO, MgnlConstants.P_PATH};


    /**
     * List of the parameters that this command needs to run
     *
     * @return a list of string describing the parameters needed. The parameters should have a  mapping in this class.
     */
    public String[] getExpectedParameters() {
        return parameters;
    }

    public boolean exec(HashMap params, Context ctx) {
        if (log.isDebugEnabled())
            log.debug("starting sending mail");

        try {
            String mailTo = (String) params.get(MgnlConstants.P_MAILTO);
            if (log.isDebugEnabled())
                log.debug("mail receiver list: " + mailTo);

            MgnlMailFactory factory = MgnlMailFactory.getInstance();
            MgnlMailHandler handler = factory.getEmailHandler();
            //MgnlEmail email = factory.getEmail(MgnlConstants.WORKFLOW_EMAIL_TEMPLATE);
            MgnlEmail email = new SimpleEmail(factory.getSession());
            email.setFrom(MgnlConstants.WORKFLOW_EMAIL_FROM_FIELD);
            email.setSubject(MgnlConstants.WORKFLOW_EMAIL_SUBJECT_FIELD);
            email.setParameters(params);
            email.setToList(factory.convertEmailList(mailTo));
            handler.prepareAndSendMail(email);

            log.info("send mail successfully to:" + mailTo);
        } catch (Exception e) {
            log.error("Could not send email", e);
        }

        return true;
    }

}
