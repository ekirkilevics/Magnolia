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
package info.magnolia.cms.gui.control;

import freemarker.template.TemplateException;
import info.magnolia.freemarker.FreemarkerHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for a generic control renderered by freemarker.
 * @author Manuel Molaschi
 * @version $Id: $
 */
public class FreemarkerControl extends ControlImpl {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(FreemarkerControl.class);

    /**
     * Constructor
     * @param valueType ControlImpl.VALUETYPE_MULTIPLE or ControlImpl.VALUETYPE_SINGLE
     */
    public FreemarkerControl(int valueType) {
        this.setValueType(valueType);
    }

    /**
     * Draw control on writer
     * @param out writer to write on
     * @param path path of freemarker template. The template will be automatically searched in classpath or in
     * filesystem
     * @param parameters map of parameters to be used in template
     * @throws IOException exception in template loading
     * @throws TemplateException exception in template rendering
     */
    public void drawHtml(Writer out, String path, Map parameters) throws IOException, TemplateException {

        if (path.startsWith("classpath:")) {
            path = StringUtils.substringAfter(path, "classpath:");
        }

        // render
        FreemarkerHelper.getInstance().render(path, parameters, out);

        // write generic html save info
        ((PrintWriter) out).print(this.getHtmlSaveInfo());
    }
}