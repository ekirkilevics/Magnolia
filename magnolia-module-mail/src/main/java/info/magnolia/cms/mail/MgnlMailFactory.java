package info.magnolia.cms.mail;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.mail.handlers.MgnlMailHandler;
import info.magnolia.cms.mail.templates.*;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.MgnlCoreConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

/**
 * This reads the repository to know what kind of email to instanciate
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlMailFactory {

    private static Logger log = LoggerFactory.getLogger(MgnlMailFactory.class);

    private static MgnlMailFactory factory = new MgnlMailFactory();
    private static final String MailHandlerInterface = "info.magnolia.cms.mail.handlers.MgnlMailHandler";

    private String smtpServer = "localhost";
    private String smtpPort = "25";
    private String smtpUser;
    private String smtpPassword;
    private String smtpAuth = "false";
    private String smtpSendPartial = "true";

    private static Class mailHandlerClass;


    private MgnlMailFactory() {
        try {
            mailHandlerClass = Class.forName(MailConstants.MAIL_HANDLER_INTERFACE);
        } catch (Exception e) {
            log.error("Could not init MgnlMailFactory", e);
        }
        try {
            initMailParameter();
        } catch (Exception e) {
            e.printStackTrace();
            // should go for the moment
        }
    }

    public static MgnlMailFactory getInstance() {
        return factory;
    }

    public MgnlMailHandler getEmailHandler() {
        return (MgnlMailHandler) FactoryUtil.getSingleton(mailHandlerClass);
    }

    public MgnlEmail getEmail(String id) throws Exception {
        if (id == null)
            return new StaticEmail(getSession());
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        Content node = hm.getContent(MailConstants.MAIL_TEMPLATES_PATH + "/" + id);
        NodeData data = node.getNodeData(MailConstants.MAIL_TEMPLATE);
        String type = data.getValue().getString();

        if (type.equalsIgnoreCase(MailConstants.MAIL_TEMPLATE_VELOCITY)) {
            VelocityEmail mail = new VelocityEmail(getSession());
            mail.setTemplate(id);
            return mail;
        } else if (type.equalsIgnoreCase(MailConstants.MAIL_TEMPLATE_HTML))
            return new HtmlEmail(getSession());
        else if (type.equalsIgnoreCase(MailConstants.MAIL_TEMPLATE_HTML))
            return new SimpleEmail(getSession());
        else return new StaticEmail(getSession());
    }

    public Session getSession() {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.smtp.port", smtpPort);
        Authenticator auth = null;
        if (Boolean.valueOf(smtpAuth).booleanValue()) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.user", smtpUser);
            auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPassword);
                }
            };
        }
        props.put("mail.smtp.sendpartial", smtpSendPartial);
        return Session.getInstance(props, auth);
    }

    void initMailParameter() throws Exception {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        Content node = hm.getContent(MailConstants.SERVER_MAIL);

        NodeData nd;
        nd = node.getNodeData(MailConstants.SMTP_SERVER);
        if (nd != null)
            smtpServer = nd.getValue().getString();

        nd = node.getNodeData(MailConstants.SMTP_PORT);
        if (nd != null)
            smtpPort = nd.getValue().getString();

        nd = node.getNodeData(MailConstants.SMTP_USER);
        if (nd != null)
            smtpUser = nd.getValue().getString();

        nd = node.getNodeData(MailConstants.SMTP_PASSWORD);
        if (nd != null)
            smtpPassword = nd.getValue().getString();

        nd = node.getNodeData(MailConstants.SMTP_AUTH);
        if (nd != null)
            smtpAuth = nd.getValue().getString();

        nd = node.getNodeData(MailConstants.SMTP_SEND_PARTIAL);
        if (nd != null)
            smtpAuth = nd.getValue().getString();
    }

    /**
     * convert email address mapping<br>
     * <code>user-</code> will be replace by the email address of the user as stored in the user repository
     * <code>group-</code> will
     *
     * @param mailTo
     * @return
     */
    public String convertEmailList(String mailTo) {
        StringBuffer ret = new StringBuffer();
        String[] list = mailTo.split(";");
        if (list == null)
            return "";
        for (int i = 0; i < list.length; i++) { // for each item
            String userName = list[i];
            if (i != 0)
                ret.append("\n");
            if (userName.startsWith(MgnlCoreConstants.PREFIX_USER)) {
                userName = userName.substring(MgnlCoreConstants.PREFIX_USER_LEN);
                if (log.isDebugEnabled())
                    log.debug("username =" + userName);
                ret.append(getUserMail(userName));
            } else if (userName.startsWith(MgnlCoreConstants.PREFIX_GROUP)) {

            } else if (userName.startsWith(MgnlCoreConstants.PREFIX_ROLE)) {

            } else {
                // none of the above, just add the mail to the list
                ret.append(userName);
            }

        }
        return ret.toString();
    }


    /**
     * retrieve email address fo user
     *
     * @param userName
     * @return the email of the user as stored in the repository, if not found returns the parameter userName
     */
    public String getUserMail(String userName) {
        try {
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
            Content user = hm.getContent(userName);
            if (user != null)
                return user.getNodeData(MailConstants.EMAIL).getValue().getString();
        } catch (Exception e) {
            log.error("can not get user email info.", e);
        }
        return userName;
    }
}
