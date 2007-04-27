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
package info.magnolia.module.admininterface;

import info.magnolia.freemarker.FreemarkerUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * A default page (hander) using a freemarker template to render. The default templat name is following the class name.
 * You can overwrite the getTemplateName() method wich can return a template per view.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class TemplatedMVCHandler extends PageMVCHandler {

    /**
     * @param name
     * @param request
     * @param response
     */
    public TemplatedMVCHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Returns the template name for the rendering. The standard implementation used the classname to build the path.
     * @param viewName the view name to render
     * @return the template name (including path)
     */
    protected String getTemplateName(String viewName) {
        String template = FreemarkerUtil.createTemplateName(this.getClass(), "html");
        if(StringUtils.isNotEmpty(viewName) && !viewName.equals(VIEW_SHOW)){
            String subtemplate = StringUtils.replace(template, ".html", StringUtils.capitalize(viewName + ".html"));
            // add postfix only in case the resource exists
            if(this.getClass().getResource(subtemplate) != null){
                template = subtemplate;
            }
        }
        return template;
    }

    /**
     * Renders the template. The handlers is passed with the name 'this'.
     */
    public void renderHtml(String view) throws IOException {
        // no rendering if view is null
        if(StringUtils.isEmpty(view)){
            return;
        }
        
        String template = this.getTemplateName(view);
        if (template != null) {

            Map data = new HashMap();
            data.put("this", this);
            data.put("view", view);

            PrintWriter writer;

            try {
                writer = getResponse().getWriter();
            }
            catch (IllegalStateException e) {
                // getResponse().getOutputStream() has already been called
                writer = new PrintWriter(getResponse().getOutputStream());
            }
            FreemarkerUtil.process(template, data, writer);
        }
    }

}
