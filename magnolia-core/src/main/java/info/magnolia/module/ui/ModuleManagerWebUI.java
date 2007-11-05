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
package info.magnolia.module.ui;

import freemarker.template.TemplateException;
import info.magnolia.context.MgnlContext;
import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.freemarker.FreemarkerUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManager;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of ModuleManagerWebUI which is meant to be used through a web interface,
 * with human interaction.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleManagerWebUI implements ModuleManagerUI {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModuleManagerWebUI.class);

    public static final String INSTALLER_PATH = "/.magnolia/installer";

    private final ModuleManager moduleManager;

    public ModuleManagerWebUI(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void onStartup() {
        final ModuleManager.ModuleManagementState moduleMgtState = moduleManager.getStatus();
        if (moduleMgtState.needsUpdateOrInstall()) {
            log.info("\n" +
                    "*********************************************************************************************************\n" +
                    "*                                                                                                       *\n" +
                    "* Magnolia needs module updates or installs, point your browser to your Magnolia instance and confirm ! *\n" +
                    "*                                                                                                       *\n" +
                    "*********************************************************************************************************");
        } else {
            moduleManager.startModules();
        }
    }

    /**
     *
     */
    public boolean execute(Writer out, String command) throws ModuleManagementException {
        if (command == null) {
            render("listTasks", out);
            return false;
        } else if ("start".equals(command)) {
            //TODO : actually check for status before executing
            moduleManager.performInstallOrUpdate();
            // TODO : be more consistent : state.needsUpdateOrInstall() vs installContext().isRestartNeeded()
            if (moduleManager.getInstallContext().isInstallDone()) {
                render("installDone", out);
            } else {
                render("conditions-not-met", out);
            }
            return false;
        } else if ("finish".equals(command)) {
            final boolean restartNeeded = moduleManager.getInstallContext().isRestartNeeded();
            if (restartNeeded) {
                render("restartNeeded", out);
            } else {
                MgnlContext.doInSystemContext(new MgnlContext.SystemContextOperation() {
                    public void exec() {
                        //TODO : actually check for status before executing
                        moduleManager.startModules();
                        moduleManager.getStatus().done();
                    }
                });
                return true;
            }
        }
        throw new IllegalStateException("Unexpected state In ModuleManagerWebUI.");
    }

    public void renderTempPage(Writer out) throws ModuleManagementException {
        render("temp", out);
    }

    protected void render(String templateName, Writer out) throws ModuleManagementException {
        final FreemarkerHelper freemarkerHelper = FreemarkerHelper.getInstance();
        final String tmpl = FreemarkerUtil.createTemplateName(getClass(), templateName + ".html");
        final Map ctx = new HashMap();
        ctx.put("installerPath", INSTALLER_PATH);
        ctx.put("status", moduleManager.getStatus());
        ctx.put("context", moduleManager.getInstallContext());
        try {
            freemarkerHelper.render(tmpl, ctx, out);
        } catch (TemplateException e) {
            throw new ModuleManagementException("Couldn't render template: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ModuleManagementException("Couldn't render template: " + e.getMessage(), e);
        }
    }

}
