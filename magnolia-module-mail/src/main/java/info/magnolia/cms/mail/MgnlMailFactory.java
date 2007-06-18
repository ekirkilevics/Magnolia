package info.magnolia.cms.mail;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.mail.handlers.MgnlMailHandler;
import info.magnolia.cms.mail.templates.MailAttachment;
import info.magnolia.cms.mail.templates.MgnlEmail;
import info.magnolia.cms.mail.templates.impl.FreemarkerEmail;
import info.magnolia.cms.mail.templates.impl.HtmlEmail;
import info.magnolia.cms.mail.templates.impl.MagnoliaEmail;
import info.magnolia.cms.mail.templates.impl.SimpleEmail;
import info.magnolia.cms.mail.templates.impl.VelocityEmail;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.core.HierarchyManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This reads the repository to know what kind of email to instanciate
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlMailFactory extends ObservedManager {

    protected static final String MAIL_FROM = "from";

    protected static final String MAIL_SUBJECT = "subject";

    protected static final String MAIL_BODY = "body";

    protected static final String EMAIL = "email";

    protected static final String SMTP_SERVER = "smtpServer";

    protected static final String SMTP_PORT = "smtpPort";

    protected static final String SMTP_USER = "smtpUser";

    protected static final String SMTP_PASSWORD = "smtpPassword";

    protected static final String SMTP_AUTH = "smtpAuth";

    protected static final String SMTP_DEFAULT_HOST = "127.0.0.1";

    protected static final String SMTP_DEFAULT_PORT = "25";

    protected static final String SMTP_SEND_PARTIAL = "smtpSendPartial";

    protected static final String MAIL_TYPE = "type";

    protected static final String MAIL_ATTACHMENT = "attachment";

    private static Logger log = LoggerFactory.getLogger(MgnlMailFactory.class);

    private static MgnlMailFactory factory = (MgnlMailFactory) FactoryUtil.getSingleton(MgnlMailFactory.class);

    protected Map mailParameters;

    private List templates;

    private Class mailHandlerClass;

    private String templatePath;

    private Map mailTypeHandlers = new HashMap();


    /**
     * Use getInstance to get the current used instance
     */
    public MgnlMailFactory() {
        mailHandlerClass = info.magnolia.cms.mail.handlers.MgnlMailHandler.class;
        this.mailParameters = new Hashtable();
        this.templates = new ArrayList();

        // default handlers
        registerMailType(MailConstants.MAIL_TEMPLATE_VELOCITY, new MgnlMailTypeFactory(){
            public MgnlEmail createEmail() throws Exception {
                return new VelocityEmail(getSession());
            }
        });

        registerMailType(MailConstants.MAIL_TEMPLATE_HTML, new MgnlMailTypeFactory(){
            public MgnlEmail createEmail() throws Exception {
                return new HtmlEmail(getSession());
            }
        });

        registerMailType(MailConstants.MAIL_TEMPLATE_FREEMARKER, new MgnlMailTypeFactory(){
            public MgnlEmail createEmail() throws Exception {
                return new FreemarkerEmail(getSession());
            }
        });

        registerMailType(MailConstants.MAIL_TEMPLATE_MAGNOLIA, new MgnlMailTypeFactory(){
            public MgnlEmail createEmail() throws Exception {
                return new MagnoliaEmail(getSession());
            }
        });
    }

    public static MgnlMailFactory getInstance() {
        return factory;
    }

    public MgnlMailHandler getEmailHandler() {
        return (MgnlMailHandler) FactoryUtil.getSingleton(mailHandlerClass);
    }

    public void registerMailType(String name, MgnlMailTypeFactory handler) {
        mailTypeHandlers.put(name.toLowerCase(), handler);
    }

    /**
     * Data is fetch into the repository to get the different parameters of the email
     * @param id the id to find under the template section of the repository
     * @return a new <code>MgnlMail</code> instance, with the template set
     * @throws Exception if fails
     */
    public MgnlEmail getEmailFromTemplate(String id, Map map) throws Exception {
        if (id == null) {
            return new SimpleEmail(getSession());
        }
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        String nodeTemplatePath = templatePath + "/" + id;
        if (!hm.isExist(nodeTemplatePath)) {
            throw new MailException("Template:[" + id + "] configuration was not found in repository");
        }

        Content node = hm.getContent(nodeTemplatePath);

        // type
        NodeData typeNode = node.getNodeData(MAIL_TYPE);
        String type = typeNode.getValue().getString();

        MgnlEmail mail = getEmailFromType(type);

        // body
        NodeData bodyNode = node.getNodeData(MAIL_BODY);
        String body = bodyNode.getValue().getString();
        mail.setBodyFromResourceFile(body, map);

        // from
        NodeData fromNode = node.getNodeData(MAIL_FROM);
        String from = fromNode.getValue().getString();
        mail.setFrom(from);

        // subject
        NodeData subjectNode = node.getNodeData(MAIL_SUBJECT);
        String subject = subjectNode.getValue().getString();
        mail.setSubject(subject);

        String attachNodePath = node.getHandle() + "/" + MAIL_ATTACHMENT;
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

        // parameters
        mail.setParameters(map);

        return mail;
    }

    protected void onRegister(Content node) {
        if (node.getHandle().endsWith("templates")) {
            log.info("Loading mail templates from node:" + node.getHandle());
            templates = listTemplatesFromRepository(node);
        }
        else if (node.getHandle().endsWith("smtp")) {
            log.info("Loading mail smtp settings from node:" + node.getHandle());
            initMailParameter(node);
        }

    }

    protected void onClear() {
        log.info("Clearing mail parameters");
        this.templates.clear();
        this.mailParameters.clear();
    }

    /**
     * Return an instance of the mail type, given a string description.
     * @param type the type of the email as defined in <code>MailConstants</code>
     * @return a new <code>MgnlEmail</code> instance, template is not set.
     * @throws Exception if fails
     */
    public MgnlEmail getEmailFromType(String type) throws Exception {
        if(mailTypeHandlers.containsKey(type.toLowerCase())){
            return ((MgnlMailTypeFactory) mailTypeHandlers.get(type.toLowerCase())).createEmail();
        }
        else {
            return new SimpleEmail(getSession());
        }
    }

    public List listTemplates() {
        return this.templates;
    }

    /**
     * List the templates stored in the repository
     * @return <code>ArrayList</code> of <code>String</code> containing the template name
     */
    private ArrayList listTemplatesFromRepository(Content templatesNode) {
        ArrayList list = new ArrayList();
        this.templatePath = templatesNode.getHandle();
        try {
            Iterator iter = templatesNode.getChildren().iterator();
            while (iter.hasNext()) {
                Content temp = (Content) iter.next();
                String templateName = temp.getName();
                log.info("Loading template:" + templateName);
                list.add(templateName);
            }
        }
        catch (Exception e) {
            log.error("Error while listing templates", e);
        }

        return list;
    }

    public void setMailParameters(Map _mailParameters) {
        this.mailParameters = _mailParameters;
    }

    public Map getMailParameters() {
        return this.mailParameters;
    }

    public Session getSession() {
        Properties props = new Properties(); // System.getProperties(); should I try to use the system properties ?
        props.put("mail.smtp.host", this.mailParameters.get(SMTP_SERVER));
        props.put("mail.smtp.port", this.mailParameters.get(SMTP_PORT));
        Authenticator auth = null;
        if (Boolean.valueOf((String) this.mailParameters.get(SMTP_AUTH)).booleanValue()) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.user", this.mailParameters.get(SMTP_USER));
            auth = new Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        (String) MgnlMailFactory.this.mailParameters.get(SMTP_USER),
                        (String) MgnlMailFactory.this.mailParameters.get(SMTP_PASSWORD));
                }
            };
        }
        props.put("mail.smtp.sendpartial", StringUtils.defaultString((String) this.mailParameters
            .get(SMTP_SEND_PARTIAL)));
        return Session.getInstance(props, auth);
    }

    protected void initMailParameter(Content node) {
        initParam(node, SMTP_SERVER, SMTP_DEFAULT_HOST);
        initParam(node, SMTP_PORT, SMTP_DEFAULT_PORT);
        initParam(node, SMTP_USER, StringUtils.EMPTY);
        initParam(node, SMTP_PASSWORD, StringUtils.EMPTY);
        initParam(node, SMTP_AUTH, StringUtils.EMPTY);
        initParam(node, SMTP_SEND_PARTIAL, StringUtils.EMPTY);
    }

    /**
     * Method to init a stmp parameter
     */
    protected void initParam(Content configNode, String paramName, String defaultValue) {
        String value = configNode.getNodeData(paramName).getString();
        if (!StringUtils.isEmpty(value)) {
            log.info("Init param[{}] with value:[{}]", paramName, value);
            initParam(paramName, value);
        }
        else {
            log.info("Init param[{}] with value:[{}] (default)", paramName, defaultValue);
            initParam(paramName, defaultValue);
        }
    }

    protected void initParam(String paramName, String paramValue) {
        this.mailParameters.put(paramName, paramValue);
    }

    /**
     * convert email address mapping<br>
     * <code>user-</code> will be replace by the email address of the user as stored in the user repository
     * <code>group-</code> will
     */
    public String convertEmailList(String mailTo) {
        StringBuffer ret = new StringBuffer();
        if(StringUtils.isEmpty(mailTo)){
            return "";
        }

        String[] list = mailTo.split(";");
        if (list == null) {
            return "";
        }
        for (int i = 0; i < list.length; i++) { // for each item
            String userName = list[i];
            if (i != 0) {
                ret.append("\n");
            }
            if (userName.startsWith(MailConstants.PREFIX_USER)) {
                userName = StringUtils.removeStart(userName, MailConstants.PREFIX_USER);
                if (log.isDebugEnabled()) {
                    log.debug("username =" + userName);
                }
                ret.append(getUserMail(userName));
            }
            else if (userName.startsWith(MailConstants.PREFIX_GROUP)) {
                userName = StringUtils.removeStart(userName, MailConstants.PREFIX_GROUP);
                try {
                    HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
                    Collection users = hm.getRoot().getChildren(ItemType.USER);
                    Iterator iter = users.iterator();
                    while(iter.hasNext()){
                        Content userNode = ((Content) iter.next());
                        UserManager manager = Security.getUserManager();
                        User user = manager.getUser(userNode.getName());
                        if (user.getGroups().contains(userName)){
                            ret.append(getUserMail(user.getName()));
                        }
                    }
                }
                catch (Exception e) {
                    log.error("can not get user email info.");
                }
            }
            else if (userName.startsWith(MailConstants.PREFIX_ROLE)) {
                userName = StringUtils.removeStart(userName, MailConstants.PREFIX_ROLE);
                try {
                    HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
                    Collection users = hm.getRoot().getChildren(ItemType.USER);
                    Iterator iter = users.iterator();
                    while(iter.hasNext()){
                        Content userNode = ((Content) iter.next());
                        UserManager manager = Security.getUserManager();
                        User user = manager.getUser(userNode.getName());
                        if (user.getRoles().contains(userName)){
                            ret.append(getUserMail(user.getName()));
                        }
                    }
                }
                catch (Exception e) {
                    log.error("can not get user email info.");
                }
            }
            else {
                // none of the above, just add the mail to the list
                ret.append(userName);
            }
        }
        return ret.toString();
    }

    /**
     * retrieve email address fo user
     * @param userName
     * @return the email of the user as stored in the repository, if not found returns the parameter userName
     */
    public String getUserMail(String userName) {
        try {
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
            Content user = hm.getContent(userName);
            if (user != null) {
                return user.getNodeData(EMAIL).getValue().getString();
            }
        }
        catch (Exception e) {
            log.error("can not get user email info.");
        }
        return userName;
    }
}
