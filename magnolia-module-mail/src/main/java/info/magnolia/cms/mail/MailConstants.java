package info.magnolia.cms.mail;

/**
 * Date: Mar 30, 2006 Time: 1:19:45 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public interface MailConstants {

    final static public String ATTRIBUTE_MAILTEMPLATE = "mailTemplate";
    
    final static public String ATTRIBUTE_MAILTO = "mailTo";
    
    public static final String MAIL_TEMPLATE_HTML = "html";

    public static final String MAIL_TEMPLATE_VELOCITY = "velocity";

    public static final String MAIL_TEMPLATE_TEXT = "text";

    public static final String MAIL_TEMPLATE_FREEMARKER = "freemarker";

    public static final String MAIL_TEMPLATE_MAGNOLIA = "magnolia";
    
    final static public String WORKFLOW_EMAIL_TEMPLATE = "workflowEmail";

    final static public String WORKFLOW_EMAIL_FROM_FIELD = "workflow@magnolia.info";

    final static public String WORKFLOW_EMAIL_SUBJECT_FIELD = "Workflow Request";

}
