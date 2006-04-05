package info.magnolia.cms.mail.templates.impl;

import info.magnolia.cms.mail.MailConstants;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import javax.mail.Session;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;

/**
 * Date: Mar 30, 2006
 * Time: 1:13:33 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class VelocityEmail extends HtmlEmail {

    static {
        try {
            Velocity.init();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public VelocityEmail(Session _session) throws Exception {
        super(_session);
    }

    public void setBody(String body, HashMap parameters) throws Exception {
        VelocityContext context = new VelocityContext(parameters);
        StringWriter w = new StringWriter();
        Velocity.mergeTemplate(getClass().getResource(MailConstants.VELOCITY_MAIL_PATH + body).getFile(), "UTF-8", context, w);
        super.setBody(w.toString(), parameters);
    }

    public void setBodyFromResourceFile(String resourceFile, HashMap _map) throws Exception {
        VelocityContext context = new VelocityContext(_map);
        URL url = this.getClass().getResource("/" + resourceFile);
        log.info("This is the url:" + url);
        BufferedReader br = new BufferedReader(new FileReader(url.getFile()));
        StringWriter w = new StringWriter();
        Velocity.evaluate(context, w, "email", br);
        super.setBody(w.toString(), _map);
    }
}
