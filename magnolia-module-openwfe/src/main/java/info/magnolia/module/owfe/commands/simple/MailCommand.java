package info.magnolia.module.owfe.commands.simple;

import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.MailHandler;
import info.magnolia.module.owfe.MgnlConstants;
import org.apache.commons.chain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class MailCommand extends MgnlCommand {
    static Logger logt = LoggerFactory.getLogger(MailCommand.class);
    static final String[] parameters = {MgnlConstants.P_MAILTO, MgnlConstants.P_PATH};

    private String smtpServer = "localhost";
    private String smtpPort = "25";
    private String smtpUser = "";
    private String smtpPassword = "";
    private String from = "MagnoliaWorkflow";
    private String subject = "Workflow Request";

    private static final String EMAIL = "email";
    private static final String SMTP_SERVER = "smtpServer";
    private static final String SMTP_PORT = "smtpPort";
    private static final String SMTP_USER = "smtpUser";
    private static final String SMTP_PASSWORD = "smtpPassword";
    private static final String SERVER_MAIL = "/server/mail";

    /**
     * List of the parameters that this command needs to run
     *
     * @return a list of string describing the parameters needed. The parameters should have a  mapping in this class.
     */
    public String[] getExpectedParameters() {
        return parameters;
    }

    public boolean exec(HashMap params, Context ctx) {
    	log.info("starting sending mail");
        //init server parameters ...        
        getMailParameter();

        log.info("converting receiver list");
        String mailTo = convertEmailList((String) params.get(MgnlConstants.P_MAILTO));
        String path = (String) params.get(MgnlConstants.P_PATH);

        if (log.isDebugEnabled())
            log.debug("mail receiver list: " + mailTo);
        try {
        	log.info("creating mail handler");
            MailHandler mh = new MailHandler(smtpServer, 1, 0);
            mh.setFrom(from);
            mh.setSubject(subject);
            mh.setToList(mailTo);
            mh.setBody(getMessageBody(path, from));
            log.info("sending mail");
            mh.sendMail();
        } catch (Exception e) {
            log.error("Could not send email", e);
        }
        log.info("send mail successfully to:" + mailTo);

        return true;
    }


    private static final String BODY = "The following page is waiting for approval:";

    /**
     * TODO: use velocity here for mail templating
     *
     * @param path
     * @param from
     * @return
     */
    public String getMessageBody(String path, String from) {
        return BODY + path;
    }

    /**
     * convert the parameter mailTo of flow to real email address list
     *
     * @param mailTo
     * @return
     */
    String convertEmailList(String mailTo) {
        StringBuffer ret = new StringBuffer();
        String[] list = mailTo.split(";");
        if (list == null)
            return "";
        for (int i = 0; i < list.length; i++) { // for each item
            String userName = list[i];
            if (userName.startsWith(MgnlConstants.PREFIX_USER)) {
                userName = userName.substring(MgnlConstants.PREFIX_USER_LEN);
                if (log.isDebugEnabled())
                    log.debug("username =" + userName);
                ret.append(getUserMail(userName));
            } else if (userName.startsWith(MgnlConstants.PREFIX_GROUP)) {

            } else if (userName.startsWith(MgnlConstants.PREFIX_ROLE)) {

            }
        }
        return ret.toString();
    }


    String getGroupMail(String groupName) {
        return "";
    }

    String getRoleMail(String groupName) {
        return "";
    }


    /**
     * retrieve email address fo user
     *
     * @param userName
     * @return
     */
    String getUserMail(String userName) {
        try {
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
            Content user = hm.getContent(userName);
//            log.info("username = " + userName);
//            log.info("username = " + user.getTitle());
            if (user != null)
                return user.getNodeData(EMAIL).getValue().getString();
        } catch (Exception e) {
            log.error("can not get user email info.", e);
        }
        return "";
    }

    /**
     * get mail parameters from JCR
     */
    void getMailParameter() {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);

        Content node = null;
        try {
            node = hm.getContent(SERVER_MAIL);
        } catch (Exception e) {
        	log.error("get content node for mail parameter failed", e);
            return;
        }

        if (node == null)
            return;

        NodeData nd = null;

        try {
            nd = node.getNodeData(SMTP_SERVER);
            if (nd != null)
                smtpServer = nd.getValue().getString();

            nd = node.getNodeData(SMTP_PORT);
            if (nd != null)
                smtpPort = nd.getValue().getString();

            nd = node.getNodeData(SMTP_USER);
            if (nd != null)
                smtpUser = nd.getValue().getString();

            nd = node.getNodeData(SMTP_PASSWORD);
            if (nd != null)
                smtpPassword = nd.getValue().getString();
        }
        catch (Exception e) {
        	log.error("get mail parameter failed", e);
//            e.printStackTrace();
        }
    }

}
