package info.magnolia.cms.mail;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.mail.handlers.MgnlMailHandler;
import info.magnolia.cms.mail.templates.MailAttachment;
import info.magnolia.cms.mail.templates.MgnlEmail;
import info.magnolia.cms.mail.templates.impl.*;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.MgnlCoreConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.net.URL;
import java.util.*;

/**
 * This reads the repository to know what kind of email to instanciate
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlMailFactory {

    private static Logger log = LoggerFactory.getLogger(MgnlMailFactory.class);
    private static MgnlMailFactory factory = new MgnlMailFactory();
    private Hashtable mailParameters;
    private static Class mailHandlerClass;


    private MgnlMailFactory() {
        try {
            mailHandlerClass = Class.forName(MailConstants.MAIL_HANDLER_INTERFACE);
        } catch (Exception e) {
            log.error("Could not init MgnlMailFactory", e);
        }
        try {
            mailParameters = new Hashtable();
            initMailParameter();
        } catch (Exception e) {
            log.error("Could not init parameters", e);
            // should go for the moment
        }
    }

    public static MgnlMailFactory getInstance() {
        return factory;
    }

    public MgnlMailHandler getEmailHandler() {
        return (MgnlMailHandler) FactoryUtil.getSingleton(mailHandlerClass);
    }

    /**
     * Data is fetch into the repository to get the different parameters of the email
     *
     * @param id the id to find under the template section of the repository
     * @return a new <code>MgnlMail</code> instance, with the template set
     * @throws Exception if fails
     */
    public MgnlEmail getEmailFromTemplate(String id, HashMap map) throws Exception {
        if (id == null)
            return new StaticEmail(getSession());
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        String nodeTemplatePath = MailConstants.MAIL_TEMPLATES_PATH + "/" + id;
        if (!hm.isExist(nodeTemplatePath))
            throw new MailException("Template:[" + id + "] configuration was not found in repository");

        Content node = hm.getContent(nodeTemplatePath);

        // type
        NodeData typeNode = node.getNodeData(MailConstants.MAIL_TYPE);
        String type = typeNode.getValue().getString();

        MgnlEmail mail = getEmailFromType(type);

        // body
        NodeData bodyNode = node.getNodeData(MailConstants.MAIL_BODY);
        String body = bodyNode.getValue().getString();
        mail.setBodyFromResourceFile(body, map);

        // from
        NodeData fromNode = node.getNodeData(MailConstants.MAIL_FROM);
        String from = fromNode.getValue().getString();
        mail.setFrom(from);

        // subject
        NodeData subjectNode = node.getNodeData(MailConstants.MAIL_SUBJECT);
        String subject = subjectNode.getValue().getString();
        mail.setSubject(subject);

        String attachNodePath = node.getHandle() + "/" + MailConstants.MAIL_ATTACHMENT;
        if (hm.isExist(attachNodePath)) {
            Content attachments = hm.getContent(attachNodePath);
            Collection atts = attachments.getChildren();
            Iterator iter = atts.iterator();
            while (iter.hasNext()) {
                Content att = (Content) iter.next();
                String cid = att.getNodeData("cid").getString();
                String url = att.getNodeData("url").getString();
                MailAttachment a = new MailAttachment(new URL(url), cid);
                mail.addAttachment(a);
            }
        }

        return mail;
    }


    /**
     * Return an instance of the mail type, given a string description.
     *
     * @param type the type of the email as defined in <code>MailConstants</code>
     * @return a new <code>MgnlEmail</code> instance, template is not set.
     * @throws Exception if fails
     */
    public MgnlEmail getEmailFromType(String type) throws Exception {
        if (type.equalsIgnoreCase(MailConstants.MAIL_TEMPLATE_VELOCITY))
            return new VelocityEmail(getSession());
        else if (type.equalsIgnoreCase(MailConstants.MAIL_TEMPLATE_HTML))
            return new HtmlEmail(getSession());
        else if (type.equalsIgnoreCase(MailConstants.MAIL_TEMPLATE_FREEMARKER))
            return new FreemarkerEmail(getSession());
        else if (type.equalsIgnoreCase(MailConstants.MAIL_TEMPLATE_SIMPLE))
            return new SimpleEmail(getSession());
        else {
            return new StaticEmail(getSession());
        }
    }

    /**
     * List the templates stored in the repository
     * //TODO: this should be loaded once and reloaded when needed
     *
     * @return <code>ArrayList</code> of <code>String</code> containing the template name
     */
    public ArrayList listTemplatesFromRepository() {
        ArrayList list = new ArrayList();
        try {
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
            Content node = hm.getContent(MailConstants.MAIL_TEMPLATES_PATH);
            Iterator iter = node.getChildren().iterator();
            while (iter.hasNext()) {
                Content temp = (Content) iter.next();
                list.add(temp.getName());
            }
        } catch (javax.jcr.PathNotFoundException pne) {
            log.error("Path for templates was not found");
        } catch (Exception e) {
            log.error("Error while listing templates", e);
        }

        return list;

    }


    public void setMailParameters(Hashtable _mailParameters) {
        this.mailParameters = _mailParameters;
    }

    public Hashtable getMailParameters() {
        return mailParameters;
    }

    public Session getSession() {
        Properties props = new Properties(); //System.getProperties(); should I try to use the system properties ?
        props.put("mail.smtp.host", mailParameters.get(MailConstants.SMTP_SERVER));
        props.put("mail.smtp.port", mailParameters.get(MailConstants.SMTP_PORT));
        Authenticator auth = null;
        if (Boolean.valueOf((String) mailParameters.get(MailConstants.SMTP_AUTH)).booleanValue()) {
            props.put("mail.smtp.auth", MgnlCoreConstants.TRUE);
            props.put("mail.smtp.user", mailParameters.get(MailConstants.SMTP_USER));
            auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication((String) mailParameters.get(MailConstants.SMTP_USER), (String) mailParameters.get(MailConstants.SMTP_PASSWORD));
                }
            };
        }
        props.put("mail.smtp.sendpartial", mailParameters.get(MailConstants.SMTP_SEND_PARTIAL));
        return Session.getInstance(props, auth);
    }

    void initMailParameter() throws Exception {
        HierarchyManager hm = null;
        Content node = null;
        try {
            hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
            node = hm.getContent(MailConstants.SERVER_MAIL);
        } catch (Exception e) {
            log.info("Cannot access repository configuration");
        }

        initParam(hm, node, MailConstants.SMTP_SERVER, MailConstants.SMTP_DEFAULT_HOST);
        initParam(hm, node, MailConstants.SMTP_PORT, MailConstants.SMTP_DEFAULT_PORT);
        initParam(hm, node, MailConstants.SMTP_USER, StringUtils.EMPTY);
        initParam(hm, node, MailConstants.SMTP_PASSWORD, StringUtils.EMPTY);
        initParam(hm, node, MailConstants.SMTP_AUTH, StringUtils.EMPTY);
        initParam(hm, node, MailConstants.SMTP_SEND_PARTIAL, StringUtils.EMPTY);
    }

    /**
     * Method to init a stmp parameter
     */
    void initParam(HierarchyManager hm, Content configNode, String param, String defaultValue) {
        try {
            if (hm != null && hm.isExist(MailConstants.SERVER_MAIL + "/" + param)) {
                NodeData nd = configNode.getNodeData(param);
                String value = nd.getValue().getString();
                if (!value.equalsIgnoreCase(StringUtils.EMPTY)) {
                    log.info("Init param[" + param + "] with value:[" + value + "]");
                    mailParameters.put(param, value);
                } else {
                    log.info("Init param[" + param + "] with value:[" + defaultValue + " ] (default)");
                    mailParameters.put(param, defaultValue);
                }
            } else {
                log.info("No path for param[" + param + "]. Using default:[" + defaultValue + "]");
                mailParameters.put(param, defaultValue);
            }
        } catch (Exception e) {
            log.error("Failed to load value for param[" + param + "]. Using default:[" + defaultValue + "]", e);
            mailParameters.put(param, defaultValue);
        }
    }

    /**
     * convert email address mapping<br>
     * <code>user-</code> will be replace by the email address of the user as stored in the user repository
     * <code>group-</code> will
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
