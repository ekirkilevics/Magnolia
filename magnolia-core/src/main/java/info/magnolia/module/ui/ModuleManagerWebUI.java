/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.ui;

import freemarker.template.TemplateException;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.freemarker.FreemarkerUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.InstallStatus;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManager;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
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

    public boolean execute(Writer out, String command) throws ModuleManagementException {
        if (command == null) {
            render("listTasks", out);
            return false;
        } else {
            final InstallContext installCtx = moduleManager.getInstallContext();
            final InstallStatus status = installCtx.getStatus();
            if ("status".equals(command) || "start".equals(command)) {
                if (status == null) {
                    performInstallOrUpdate();
                    render("inProgress", out);
                } else {
                    // template names match statuses
                    render(status.getName(), out);
                }
                return false;
            } else if ("finish".equals(command) && status.equals(InstallStatus.installDone)) {
                MgnlContext.doInSystemContext(new MgnlContext.SystemContextOperation() {
                    public void exec() {
                        //TODO : actually check for status before executing
                        moduleManager.startModules();
                        //moduleManager.getStatus().done();
                    }
                }, false);
                return true;
            }
        }
        throw new IllegalStateException("Unexpected state In ModuleManagerWebUI.");
    }

    public void renderTempPage(Writer out) throws ModuleManagementException {
        render("temp", out);
    }

    protected void performInstallOrUpdate() {
        final Runnable runnable = new Runnable() {
            public void run() {
                try {
                    moduleManager.performInstallOrUpdate();
                } catch (Throwable e) {
                    log.error("Could not perform installation: " + e.getMessage(), e);
                    moduleManager.getInstallContext().error("Could not perform installation: " + e.getMessage(), e);
                    // TODO set status ? here the status page continues on reloading itself ...
                }
            }
        };
        new Thread(runnable).start();
    }

    protected void render(String templateName, Writer out) throws ModuleManagementException {
        // a special instance of FreemarkerHelper which does not use Magnolia components
        final FreemarkerHelper freemarkerHelper = new FreemarkerHelper() {
            protected void addDefaultData(Map data, Locale locale, String i18nBasename) {
                final WebContext webCtx = (WebContext) MgnlContext.getInstance();
                // @deprecated (-> update all templates)
                data.put("contextPath", webCtx.getContextPath());
                data.put("ctx", MgnlContext.getInstance());
            }
        };
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
