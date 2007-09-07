package info.magnolia.cms.mail.templates.impl;

import info.magnolia.freemarker.FreemarkerHelper;

import javax.mail.Session;
import java.io.StringWriter;
import java.util.Map;

/**
 * Date: Apr 5, 2006 Time: 8:59:18 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class FreemarkerEmail extends HtmlEmail {

    public FreemarkerEmail(Session _session) throws Exception {
        super(_session);
    }

    public void setBodyFromResourceFile(String resourceFile, Map _map) throws Exception {
        final StringWriter writer = new StringWriter();
        FreemarkerHelper.getInstance().render(resourceFile, _map, writer);
        super.setBody(writer.toString(), _map);
    }

}
