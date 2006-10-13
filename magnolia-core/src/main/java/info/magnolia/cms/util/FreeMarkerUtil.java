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

import info.magnolia.context.MgnlContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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

    /**
     * The internal configuration used
     */
    private static Configuration cfg;

    private static Logger log = LoggerFactory.getLogger(FreeMarkerUtil.class);

    static {
        cfg = new Configuration();
        cfg.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
        cfg.setClassForTemplateLoading(FreeMarkerUtil.class, "/");
        cfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
        cfg.setDefaultEncoding("UTF8");
    }

    /**
     * Process this template with the passed data
     * @param name
     * @param data
     * @return the resuling string
     */
    public static String process(String name, Map data) {
        Writer writer = new StringWriter();
        process(name, data, writer);
        return writer.toString();
    }

    /**
     * Uses the class of the object to create the templates name and passes the object under the name 'this'
     * @param thisObj
     * @return the resuling string
     */
    public static String process(Object thisObj) {
        return process(thisObj.getClass(), thisObj);
    }

    /**
     * Default extension is html
     * @param klass
     * @param thisObj
     * @return
     */
    public static String process(Class klass, Object thisObj) {
        return process(klass, thisObj, "html");
    }

    /**
     * Uses the class to create the templates name and passes the object under the name 'this'
     * @param klass
     * @param thisObj
     * @param ext
     * @return
     */
    public static String process(Class klass, Object thisObj, String ext) {
        Map data = new HashMap();
        data.put("this", thisObj);
        return process(klass, data, ext);
    }

    /**
     * Uses the class to create the templates name.
     * @param klass
     * @param data
     * @param ext
     * @return
     */
    public static String process(Class klass, Map data, String ext) {
        return process(createTemplateName(klass, ext), data);
    }

    /**
     * Process the template with the data and writes the result to the writer.
     * @param name
     * @param data
     * @param writer
     */
    public static void process(String name, Map data, Writer writer) {
        try {
            Template tmpl = cfg.getTemplate(name);
            // add some usfull default data
            data.put("contextPath", MgnlContext.getContextPath());
            if (AlertUtil.isMessageSet()) {
                data.put("message", AlertUtil.getMessage());
            }
            tmpl.process(data, writer);
        }
        catch (Exception e) {
            e.printStackTrace(new PrintWriter(writer));
            log.error("exception in template", e);
        }
    }

    public static String createTemplateName(Class klass, String ext) {
        return "/" + StringUtils.replace(klass.getName(), ".", "/") + "." + ext;
    }

}
