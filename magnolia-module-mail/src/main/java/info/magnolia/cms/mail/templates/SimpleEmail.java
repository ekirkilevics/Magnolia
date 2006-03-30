package info.magnolia.cms.mail.templates;

import info.magnolia.cms.mail.MailConstants;

import javax.mail.Session;
import java.util.HashMap;

/**
 * Date: Mar 30, 2006
 * Time: 1:01:01 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class SimpleEmail extends MgnlEmail {

    public SimpleEmail(Session session) throws Exception {
        super(session);
        this.setHeader(MailConstants.CONTENT_TYPE, MailConstants.TEXT_PLAIN_UTF);
    }

    public void setBody(String body, HashMap parameters) throws Exception {
        this.setContent(body, MailConstants.TEXT_PLAIN_UTF);
    }
}
