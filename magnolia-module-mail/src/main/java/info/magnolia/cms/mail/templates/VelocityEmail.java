package info.magnolia.cms.mail.templates;

import info.magnolia.cms.mail.MailConstants;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import javax.mail.Session;
import java.io.StringWriter;
import java.util.HashMap;

/**
 * Date: Mar 30, 2006
 * Time: 1:13:33 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class VelocityEmail extends MgnlEmail {

    static {
        try {
            Velocity.init();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public VelocityEmail(Session _session) throws Exception {
        super(_session);
        this.setHeader(MailConstants.CONTENT_TYPE, MailConstants.TEXT_HTML_UTF);
    }

    public void setBody(String body, HashMap parameters) throws Exception {
        VelocityContext context = new VelocityContext(parameters);
        /* lets render a template */
        StringWriter w = new StringWriter();
        if (this.getTemplate() == null) {
            log.error("No template defined for this mail. Copying the text as is");
            this.setContent(body, MailConstants.TEXT_PLAIN_UTF);
        } else {
            Velocity.mergeTemplate(getClass().getResource(MailConstants.VELOCITY_MAIL_PATH + this.getTemplate() + ".vm").getFile(), context, w);
            this.setContent(w.toString(), MailConstants.TEXT_HTML_UTF);
        }
    }
}
