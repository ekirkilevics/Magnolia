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
package info.magnolia.module.cache.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BackupTask;
import info.magnolia.module.delta.BootstrapResourcesTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.IsAuthorInstanceDelegateTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.WebXmlConditionsUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class CacheModuleVersionHandler extends DefaultModuleVersionHandler {

    public CacheModuleVersionHandler() {
        super();
        final List conditions = new ArrayList();
        final WebXmlConditionsUtil u = new WebXmlConditionsUtil(conditions);
        u.servletIsRemoved("CacheServlet");
        u.servletIsRemoved("CacheGeneratorServlet");
        final DeltaBuilder deltaTo35 = DeltaBuilder.update("3.5.0", "");
        // bootstrap file removed - and not needed even if we're upgrading from 3.0
        // deltaTo35.addTask(new BootstrapSingleResource("Configured Observation", "Adds the repositories to be observed.", "/mgnl-bootstrap/cache/config.modules.cache.config.repositories.xml"));
        deltaTo35.addConditions(conditions);
        register(deltaTo35);

        register(DeltaBuilder.update("3.6", "New cache API and configuration.")
                .addTask(new BackupTask("config", "/modules/cache/config", true))
                .addTask(new BootstrapResourcesTask("New configuration", "Bootstraps new default cache configuration.") {
                    protected String[] getResourcesToBootstrap(final InstallContext installContext) {
                        return new String[]{
                                "/mgnl-bootstrap/cache/config.modules.cache.config.configurations.default.xml",
                                "/mgnl-bootstrap/cache/config.modules.cache.config.cacheFactory.xml"
                        };
                    }
                })
        );


    }

    protected List getExtraInstallTasks(InstallContext installContext) {
        final List tasks = new ArrayList();
        tasks.add(new FilterOrderingTask("cache", new String[]{"i18n"}));
        tasks.add(new FilterOrderingTask("gzip", new String[]{"cache"}));
        return tasks;
    }

    /* TODO : if we keep this, they should move to cacheStrategy now
    public List getStartupTasks(InstallContext installContext) {
        List tasks = new ArrayList();

        // standard voters that should be always available. They can be disabled by setting the enable flag to false,
        // but their presence will always be checked

        Map config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("not", Boolean.TRUE);
        tasks.add(new AddCacheVoterTask("notWithParametersVoter", RequestHasParametersVoter.class, config));

        // this was replaced by a simple bypass
        config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("falseValue", new Long(-1));
        config.put("trueValue", new Long(0));
        config.put("allow", "html,css,js,jpg,gif,png");
        tasks.add(new AddCacheVoterTask("extensionVoter", ExtensionVoter.class, config));

        config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("not", Boolean.TRUE);
        tasks.add(new AddCacheVoterTask("notOnAdminVoter", OnAdminVoter.class, config));

        config = new HashMap();
        config.put("enabled", Boolean.FALSE);
        config.put("not", Boolean.TRUE);
        tasks.add(new AddCacheVoterTask("notAuthenticatedVoter", AuthenticatedVoter.class, config));

        return tasks;
    }
    */

}
