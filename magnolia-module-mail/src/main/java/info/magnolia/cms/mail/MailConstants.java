package info.magnolia.cms.mail;

/**
 * Date: Mar 30, 2006 Time: 1:19:45 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public interface MailConstants {

    public static final String ATTRIBUTE_MAILTEMPLATE = "mailTemplate";
    
    public static final String ATTRIBUTE_MAILTO = "mailTo";
    
    public static final String MAIL_TEMPLATE_HTML = "html";

    public static final String MAIL_TEMPLATE_VELOCITY = "velocity";

    public static final String MAIL_TEMPLATE_TEXT = "text";

    public static final String MAIL_TEMPLATE_FREEMARKER = "freemarker";

    public static final String MAIL_TEMPLATE_MAGNOLIA = "magnolia";
    
    public static final String WORKFLOW_EMAIL_TEMPLATE = "workflowEmail";

    public static final String WORKFLOW_EMAIL_FROM_FIELD = "workflow@magnolia.info";

    public static final String WORKFLOW_EMAIL_SUBJECT_FIELD = "Workflow Request";

    final static public String PREFIX_USER = "user-";

    final static public String PREFIX_GROUP = "group-";

    final static public String PREFIX_ROLE = "role-";

}
