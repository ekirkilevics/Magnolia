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
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
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
        "i18n.js",
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

        WebContext ctx = (WebContext) FactoryUtil.getInstance(WebContext.class);
        ctx.init(request);
        MgnlContext.setInstance(ctx);

        out.println("var contextPath = '" + contextPath + "';");

        for (int i = 0; i < includes.length; i++) {
            InputStream in = ClasspathResourcesUtil.getStream("/mgnl-resources/admin-js/" + includes[i]);
            IOUtils.copy(in, out);
        }

        out.println(MessagesUtil.generateJavaScript(MessagesManager.getMessages()));

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

            def.content = IOUtils.toString(ClasspathResourcesUtil.getStream(name));
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

    private class Definition {

        private boolean proceed = false;

        private String content;

        private String name;

        private List imports = new ArrayList();
    }

}
