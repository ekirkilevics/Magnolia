package info.magnolia.cms.mail.templates.impl;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

import javax.mail.Session;
import java.io.StringWriter;
import java.util.HashMap;

/**
 * Date: Apr 5, 2006
 * Time: 8:59:18 PM
 *
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
            //FileTemplateLoader ftl1 = new FileTemplateLoader(new File("/tmp/templates"));
            //FileTemplateLoader ftl2 = new FileTemplateLoader(new File("/usr/data/templates"));
            ClassTemplateLoader ctl = new ClassTemplateLoader(FreemarkerEmail.class, "/");
            //TemplateLoader[] loaders = new TemplateLoader[]{ftl1, ftl2, ctl};
            TemplateLoader[] loaders = new TemplateLoader[]{ctl};
            MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
            cfg.setTemplateLoader(mtl);
        } catch (Exception e) {
            log.error("Error while loading freemarker configuration", e);
        }
    }

    public void setBodyFromResourceFile(String resourceFile, HashMap _map) throws Exception {
        Template late = cfg.getTemplate(resourceFile);
        StringWriter writer = new StringWriter();
        late.process(_map, writer);
        writer.flush();
        log.info(writer.toString());
        super.setBody(writer.toString(), _map);
    }
}
