package info.magnolia.cms.mail;

/**
 * Date: Mar 30, 2006 Time: 1:19:45 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public interface MailConstants {

    public static final String MAIL_TEMPLATE_HTML = "html";
    public static final String MAIL_TEMPLATE_VELOCITY = "velocity";
    public static final String MAIL_TEMPLATE_TEXT = "text";
    public static final String MAIL_TEMPLATE_FREEMARKER = "freemarker";
    public static final String MAIL_TEMPLATE_MAGNOLIA = "magnolia";

    public static final String WORKFLOW_EMAIL_TEMPLATE = "workflowEmail";
    public static final String WORKFLOW_EMAIL_FROM_FIELD = "workflow@magnolia.info";
    public static final String WORKFLOW_EMAIL_SUBJECT_FIELD = "Workflow Request";

    public static final String PREFIX_USER = "user-";
    public static final String PREFIX_GROUP = "group-";
    public static final String PREFIX_ROLE = "role-";

    public static final String ATTRIBUTE_CC = "cc";
    public static final String ATTRIBUTE_FROM = "from";
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_SUBJECT = "subject";
    public static final String ATTRIBUTE_TEXT = "text";
    public static final String ATTRIBUTE_ATTACHMENT = "attachment";
    public static final String ATTRIBUTE_TEMPLATE = "mailTemplate";
    public static final String ATTRIBUTE_TO = "mailTo";
}
