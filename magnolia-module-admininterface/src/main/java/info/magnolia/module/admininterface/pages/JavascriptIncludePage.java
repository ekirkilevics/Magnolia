/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.module.admininterface.PageMVCHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class JavascriptIncludePage extends PageMVCHandler {

    private static boolean nocache = BooleanUtils.toBoolean(SystemProperty.getProperty("magnolia.develop"));

    private static String[] files;

    private static String[] includes = {
        "debug.js",
        "generic.js",
        "general.js",
        "controls.js",
        "tree.js",
        //"i18n.js", moved alone since it must be initialized first
        "contextmenu.js",
        "inline.js"};

    private static Pattern importPattern = Pattern.compile("importClass\\(\"(.*)\"\\);");

    private Map classDefinitions = new HashMap();

    /**
     * Required constructor.
     * @param name page name
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    public JavascriptIncludePage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    void process(String name, PrintWriter out) throws IOException {
        Definition def = (Definition) classDefinitions.get(name);
        if (!def.proceed) {
            def.proceed = true;
            for (Iterator iter = def.imports.iterator(); iter.hasNext();) {
                String importName = (String) iter.next();
                process(importName, out);
            }
            out.println(def.content);
        }
    }

    /**
     * @see info.magnolia.cms.servlets.MVCServletHandler#renderHtml(java.lang.String)
     */
    public void renderHtml(String view) throws IOException {
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        PrintWriter out = response.getWriter();
        String contextPath = request.getContextPath();

        out.println("var contextPath = '" + contextPath + "';");

        prepareI18n(out);

        for (int i = 0; i < includes.length; i++) {
            InputStream in = ClasspathResourcesUtil.getStream("/mgnl-resources/admin-js/" + includes[i]);
            IOUtils.copy(in, out);
            in.close();
        }


        // finding files in classpath is too expensive, just cache the list of paths!
        if (files == null || nocache) {
            files = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {

                public boolean accept(String name) {
                    return name.startsWith("/mgnl-resources/js-classes") && name.endsWith(".js");
                }
            });
        }

        // request.getRequestDispatcher("/.resources/js-libs/*.js").include(request, response);

        for (int j = 0; j < files.length; j++) {
            String name = files[j];
            Definition def = new Definition();
            def.name = StringUtils.replace(name, "\\", "/");
            def.name = StringUtils.substringAfterLast(def.name, "/js-classes/");
            def.name = StringUtils.removeEnd(def.name, ".js");
            def.name = StringUtils.replace(def.name, "/", ".");
            InputStream stream = ClasspathResourcesUtil.getStream(name);
            def.content = IOUtils.toString(stream);
            stream.close();
            Matcher matcher = importPattern.matcher(def.content);
            while (matcher.find()) {
                String importName = matcher.group(1);
                def.imports.add(importName);
            }
            classDefinitions.put(def.name, def);
        }

        // write first the runtime
        Definition runtime = (Definition) classDefinitions.get("mgnl.Runtime");
        out.println(runtime.content);
        runtime.proceed = true;

        out.println("MgnlRuntime.loadingOn=false;");

        for (Iterator iter = classDefinitions.keySet().iterator(); iter.hasNext();) {
            String className = (String) iter.next();
            process(className, out);
        }

        out.println("MgnlRuntime.loadingOn=true;");

    }

    private void prepareI18n(PrintWriter out) throws IOException {
        InputStream in = ClasspathResourcesUtil.getStream("/mgnl-resources/admin-js/i18n.js");
        IOUtils.copy(in, out);
        out.println(MessagesUtil.generateJavaScript(MessagesManager.getMessages()));
        in.close();
    }

    protected class Definition {

        protected boolean proceed = false;

        protected String content;

        protected String name;

        protected List imports = new ArrayList();
    }

}
