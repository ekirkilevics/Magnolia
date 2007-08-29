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
package info.magnolia.module;

import freemarker.template.TemplateException;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.freemarker.FreemarkerUtil;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleManagerUI {
    public static final String INSTALLER_PATH = "/.magnolia/installer";

    private final String contextPath;

    public ModuleManagerUI(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * TODO : have a specific Context - will avoid having to add contextPath to the freemarker ctx, for instance.
     *
     * @param params a Map<String, String[]>, as in HttpServletRequest.getParametersMap()
     * @return a boolean indicating if the request should go through or pause until next user action.
     *
     * TODO : exception handling
     */
    public boolean execute(ModuleManager moduleManager, Writer out, Map params) throws IOException, TemplateException, RepositoryException {
        final String ok = value(params, "ok");
        final String done = value(params, "done");
        if (ok == null && done == null) {
            render("start", moduleManager, out);
            return false;
        } else if ("ok".equals(ok)) {
            moduleManager.performInstallOrUpdate();
            render("done", moduleManager, out);
            return false;
        } else if ("done".equals(done)) {
            final boolean restartNeeded = moduleManager.getInstallContext().isRestartNeeded();
            if (restartNeeded) {
                render("restart", moduleManager, out);
            } else {
                Context ctx = null;
                if(MgnlContext.hasInstance()){
                    ctx = MgnlContext.getInstance();
                }
                try{
                    MgnlContext.setInstance(MgnlContext.getSystemContext());
                    moduleManager.startModules();
                    moduleManager.getStatus().done();
                }
                finally{
                    MgnlContext.setInstance(ctx);
                }
                return true;
            }
        }
        throw new IllegalStateException("Unexpected state In ModuleManagerUI.");
    }

    // TODO : exception handling
    public void renderTempPage(ModuleManager moduleManager, Writer out) throws IOException, TemplateException {
        render("temp", moduleManager, out);
    }

    protected void render(String templateName, ModuleManager moduleManager, Writer out) throws IOException, TemplateException {
        final FreemarkerHelper freemarkerHelper = FreemarkerHelper.getInstance();
        final String tmpl = FreemarkerUtil.createTemplateName(getClass(), templateName + ".html");
        final Map ctx = new HashMap();
        ctx.put("contextPath", contextPath);
        ctx.put("installerPath", INSTALLER_PATH);
        ctx.put("status", moduleManager.getStatus());
        ctx.put("context", moduleManager.getInstallContext());
        freemarkerHelper.render(tmpl, ctx, out);
    }

    private String value(Map parametersMap, String name) {
        final String[] arr = (String[]) parametersMap.get(name);
        if (arr == null) {
            return null;
        }
        return arr[0];
    }

}
