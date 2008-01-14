/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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
