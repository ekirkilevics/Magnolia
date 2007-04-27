/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.freemarker;

import freemarker.template.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.HashMap;
import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;

import info.magnolia.context.MgnlContext;
import info.magnolia.cms.util.AlertUtil;

/**
 * A bunch of utility methods to render freemarker templates into Strings.
 *
 * @see info.magnolia.freemarker.FreemarkerHelper
 * 
 * @author Philipp Bracher
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FreemarkerUtil {
    private static final Logger log = LoggerFactory.getLogger(FreemarkerUtil.class);

    /**
     * Uses the class of the object to create the templates name, passes the object under the name 'this'
     * and returns the result in a String.
     */
    public static String process(Object thisObj) {
        return process(thisObj.getClass(), thisObj);
    }

    /**
     * Default extension is html
     */
    public static String process(Class klass, Object thisObj) {
        return process(klass, thisObj, "html");
    }

    /**
     * Uses the class to create the templates name and passes the object under the name 'this'
     *
     * @deprecated not used (only by this class)
     */
    public static String process(Class klass, Object thisObj, String ext) {
        final Map data = new HashMap();
        data.put("this", thisObj);
        return process(klass, data, ext);
    }

    /**
     * Uses the class to create the templates name.
     *
     * @deprecated not used (only by this class)
     */
    public static String process(Class klass, Map data, String ext) {
        return process(createTemplateName(klass, ext), data);
    }

    /**
     * Process this template with the passed data and returns the result in a String.
     */
    public static String process(String name, Map data) {
        final Writer writer = new StringWriter();
        process(name, data, writer);
        return writer.toString();
    }

    /**
     * Process the template with the data and writes the result to the writer.
     * TODO : move this to FreemarkerHelper
     */
    public static void process(String name, Map data, Writer writer) {
        try {
            // add some usfull default data
            data.put("contextPath", MgnlContext.getContextPath());
            if (AlertUtil.isMessageSet()) {
                data.put("message", AlertUtil.getMessage());
            }
            FreemarkerHelper.getInstance().render(name, data, writer);
        }
        catch (Exception e) {
            e.printStackTrace(new PrintWriter(writer));
            log.error("exception in template", e);
        }
    }

    public static String createTemplateName(Class klass, String ext) {
        return "/" + StringUtils.replace(klass.getName(), ".", "/") + "." + ext;
    }

    /**
     * @return get default static freemarker configuration
     * @deprecated don't mess around, just use FreemarkerHelper
     */
    public static Configuration getDefaultConfiguration() {
        return FreemarkerHelper.getInstance().getConfiguration();
    }
}
