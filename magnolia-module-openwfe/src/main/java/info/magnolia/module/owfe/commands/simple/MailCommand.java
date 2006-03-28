package info.magnolia.module.owfe.commands.simple;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.MailHandler;
import info.magnolia.module.owfe.commands.MgnlCommand;
import org.apache.commons.chain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class MailCommand extends MgnlCommand {
    static Logger logt = LoggerFactory.getLogger(MailCommand.class);

    private String smtpServer = "localhost";
    private String smtpPort = "25";
    private String smtpUser = "";
    private String smtpPassword = "";
    private String from = "MagnoliaWorkflow";
    private String subject = "Workflow Request";

    public boolean exec(HashMap params, Context ctx) {
        String mailTo = convertEmailList((String) params.get(P_MAILTO));
        String path = (String) params.get(P_PATH);

        if (log.isDebugEnabled())
            log.debug("mail receiver list: " + mailTo);
        try {
            MailHandler mh = new MailHandler(smtpServer, 1, 0);
            mh.setFrom(from);
            mh.setSubject(subject);
            mh.setToList(mailTo);
            mh.setBody(getMessageBody(path, from));
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
            if (userName.startsWith(MgnlCommand.PREFIX_USER)) {
                userName = userName.substring(PREFIX_USER_LEN);
                if (log.isDebugEnabled())
                    log.debug("username =" + userName);
                ret.append(getUserMail(userName));
            } else if (userName.startsWith(MgnlCommand.PREFIX_GROUP)) {

            } else if (userName.startsWith(MgnlCommand.PREFIX_ROLE)) {

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
            if (user != null)
                return user.getNodeData("email").toString();
        } catch (Exception e) {
            log.error("can not add group reference to user.", e);
        }
        return "";
    }

    /**
     * get mail parameters from JCR
     */
    void getMailParameter() {
        HierarchyManager hm = ContentRepository
                .getHierarchyManager(ContentRepository.CONFIG);

        Content node = null;
        try {
            node = hm.getContent("/server/server/mail");
        } catch (Exception e) {
            return;
        }

        if (node == null)
            return;

        NodeData nd = null;

        nd = node.getNodeData("smtpServer");
        if (nd != null)
            smtpServer = nd.toString();

        nd = node.getNodeData("smtpPort");
        if (nd != null)
            smtpPort = nd.toString();

        nd = node.getNodeData("smtpUser");
        if (nd != null)
            smtpUser = nd.toString();

        nd = node.getNodeData("smtpPassword");
        if (nd != null)
            smtpPassword = nd.toString();
    }

}
