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
package info.magnolia.setup.for3_1;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.BootstrapResourcesTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class UpdateI18nConfiguration extends NodeExistsDelegateTask {
    private static final String I18N_NODEPATH = "/server/i18n";
    private static final String I18N_SYSTEM_NODEPATH = I18N_NODEPATH + "/system";

    public UpdateI18nConfiguration() {
        super("I18N configuration", "The I18N configuration has changed considerably in Magnolia 3.1. Will update the existing configuration or bootstrap a new one.",
                "config", I18N_NODEPATH, new UpdateFrom30(), new BootstrapI18nConfig());
    }

    final static class BootstrapI18nConfig extends BootstrapResourcesTask {
        BootstrapI18nConfig() {
            super(null, null);
        }

        protected String[] getResourcesToBootstrap(final InstallContext installContext) {
            return new String[]{
                    "/mgnl-bootstrap/core/config.server.i18n.content.xml",
                    "/mgnl-bootstrap/core/config.server.i18n.system.xml"
            };
        }
    }

    static class UpdateFrom30 extends AbstractRepositoryTask {
        UpdateFrom30() {
            super(null, null);
        }

        protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
            final HierarchyManager configHM = ctx.getConfigHierarchyManager();

            // get existing values and remove node
            final Content i18nNode = configHM.getContent(I18N_NODEPATH);
            final String defaultLangIn30 = i18nNode.getNodeData("language").getString();
            final Content availLangsNode = i18nNode.getContent("availableLanguages");
            final Collection availLangsIn30;
            try {
                availLangsIn30 = Content2BeanUtil.toMap(availLangsNode, false).values();
            } catch (Content2BeanException e) {
                throw new TaskExecutionException("Couldn't read availableLanguages", e);
            }
            i18nNode.delete();

            // bootstrap new defaults
            doBootstrap(ctx);

            // adapt with previously existing values
            final Content newI18nSystemNode = configHM.getContent(I18N_SYSTEM_NODEPATH);
            final NodeData sysFallbackLang = newI18nSystemNode.getNodeData("fallbackLanguage");
            sysFallbackLang.setValue(defaultLangIn30);

            final Collection languages = newI18nSystemNode.getContent("languages").getChildren();
            final Iterator it = languages.iterator();
            while (it.hasNext()) {
                final Content langNode = (Content) it.next();
                if (!availLangsIn30.contains(langNode.getName())) {
                    final NodeData enabledProperty = NodeDataUtil.getOrCreate(langNode, "enabled");
                    enabledProperty.setValue(false);
                }
            }
        }

        protected void doBootstrap(InstallContext ctx) throws TaskExecutionException {
            new BootstrapI18nConfig().execute(ctx);
        }
    }
}
