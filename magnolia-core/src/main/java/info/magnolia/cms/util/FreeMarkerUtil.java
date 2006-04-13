/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.cms.beans.runtime.MgnlContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;


/**
 * This is a FreeMaker Util loading the templates from the classpath
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class FreeMarkerUtil {

    private static Configuration cfg;

    private static Logger log = LoggerFactory.getLogger(FreeMarkerUtil.class);

    static {
        cfg = new Configuration();
        cfg.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
        cfg.setClassForTemplateLoading(FreeMarkerUtil.class, "/");
    }

    public static String process(String name, Map data) {
        Writer writer = new StringWriter();
        process(name, data, writer);
        return writer.toString();
    }

    public static void process(String name, Map data, Writer writer) {
        try {
            Template tmpl = cfg.getTemplate(name);
            // add some usfull default data
            data.put("contextPath", MgnlContext.getContextPath());
            if(AlertUtil.isMessageSet()){
                data.put("message", AlertUtil.getMessage());
            }
            tmpl.process(data, writer);
        }
        catch (Exception e) {
            e.printStackTrace(new PrintWriter(writer));
            log.error("exception in template", e);
        }
    }

}
