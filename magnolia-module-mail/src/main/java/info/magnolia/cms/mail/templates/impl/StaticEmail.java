package info.magnolia.cms.mail.templates.impl;

import info.magnolia.cms.mail.MailConstants;
import info.magnolia.cms.mail.templates.MgnlEmail;

import javax.mail.Session;
import java.util.HashMap;

/**
 * Date: Mar 30, 2006
 * Time: 3:29:49 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class StaticEmail extends MgnlEmail {

    //public static final String TEST_STRING = "This is a test email";
    public static String STATIC_TEST_SUBJECT = "ありがとう";
    public static String STATIC_TEST_CONTENT = "元気ですか？";

    public StaticEmail(Session _session) throws Exception {
        super(_session);
        this.setHeader(MailConstants.CONTENT_TYPE, MailConstants.TEXT_PLAIN_UTF);
        this.setContent(STATIC_TEST_CONTENT, MailConstants.TEXT_PLAIN_UTF);
        this.setSubject(STATIC_TEST_CONTENT);
    }

    public void setBody(String body, HashMap parameters) throws Exception {
        // nothing to do
    }
}
