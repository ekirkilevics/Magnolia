package info.magnolia.cms.mail.templates.impl;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

import java.io.StringWriter;
import java.util.Map;

import javax.mail.Session;


/**
 * Date: Apr 5, 2006 Time: 8:59:18 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class FreemarkerEmail extends HtmlEmail {

    public FreemarkerEmail(Session _session) throws Exception {
        super(_session);
    }

    static Configuration cfg = new Configuration();

    static {
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        try {
            ClassTemplateLoader ctl = new ClassTemplateLoader(FreemarkerEmail.class, "/");
            TemplateLoader[] loaders = new TemplateLoader[]{ctl};
            MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
            cfg.setTemplateLoader(mtl);
            cfg.setDefaultEncoding("UTF8");
        }
        catch (Exception e) {
            log.error("Error while loading freemarker configuration", e);
        }
    }

    public void setBodyFromResourceFile(String resourceFile, Map _map) throws Exception {
        Template late = cfg.getTemplate(resourceFile);
        setBodyFromTemplate(late, _map);
    }

    public void setBodyFromTemplate(Template template, Map _map) throws Exception {
        StringWriter writer = new StringWriter();
        template.process(_map, writer);
        writer.flush();
        super.setBody(writer.toString(), _map);
    }
}
