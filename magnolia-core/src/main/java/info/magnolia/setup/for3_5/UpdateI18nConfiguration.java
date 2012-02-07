/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.setup.for3_5;

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
 * Updates pre 3.5 internationalization configuration to the format used since 3.5.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class UpdateI18nConfiguration extends NodeExistsDelegateTask {
    private static final String I18N_NODEPATH = "/server/i18n";
    private static final String I18N_SYSTEM_NODEPATH = I18N_NODEPATH + "/system";

    public UpdateI18nConfiguration() {
        super("I18N configuration", "The I18N configuration has changed considerably in Magnolia 3.5. Will update the existing configuration or bootstrap a new one.",
                "config", I18N_NODEPATH, new UpdateFrom30(), new BootstrapI18nConfig());
    }

    /**
     * Bootstrap task for loading new i18n configuration.
     */
    final static class BootstrapI18nConfig extends BootstrapResourcesTask {
        BootstrapI18nConfig() {
            super(null, null);
        }

        @Override
        protected String[] getResourcesToBootstrap(final InstallContext installContext) {
            return new String[]{
                    "/mgnl-bootstrap/core/config.server.i18n.content.xml",
                    "/mgnl-bootstrap/core/config.server.i18n.system.xml"
            };
        }
    }

    /**
     * Update task for bootstrapping new configuration and then merging it with existing configuration in the upgraded instance.
     */
    static class UpdateFrom30 extends AbstractRepositoryTask {
        UpdateFrom30() {
            super(null, null);
        }

        @Override
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
