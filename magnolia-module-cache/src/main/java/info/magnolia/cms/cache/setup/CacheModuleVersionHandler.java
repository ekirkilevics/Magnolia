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
package info.magnolia.cms.cache.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.IsAuthorInstanceDelegateTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.WebXmlConditionsUtil;
import info.magnolia.voting.voters.AuthenticatedVoter;
import info.magnolia.voting.voters.ExtensionVoter;
import info.magnolia.voting.voters.OnAdminVoter;
import info.magnolia.voting.voters.RequestHasParametersVoter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        register(DeltaBuilder.update("3.5.0", "").addConditions(conditions));
    }

    protected List getExtraInstallTasks(InstallContext installContext) {
        final List tasks = super.getBasicInstallTasks(installContext);
        tasks.add(new FilterOrderingTask("cache", new String[]{"i18n"}));
        // activate cache on public instances
        tasks.add(new IsAuthorInstanceDelegateTask("Cache Activating", "Sets cache to active on public instances", null, new SetPropertyTask(ContentRepository.CONFIG,"/modules/cache/config","active", "true")));
        return tasks;
    }

    public List getStartupTasks(InstallContext installContext) {
        List tasks = new ArrayList();

        // standard voters that should be always available. They can be disabled by setting the enable flag to false,
        // but their presence will always be checked

        Map config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("trueValue", new Long(-1));
        tasks.add(new AddCacheVoterTask("notWithParametersVoter", RequestHasParametersVoter.class, config));

        config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("falseValue", new Long(-1));
        config.put("trueValue", new Long(0));
        config.put("allow", "html,css,js,jpg,gif,png");
        tasks.add(new AddCacheVoterTask("extensionVoter", ExtensionVoter.class, config));

        config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("trueValue", new Long(-1));
        tasks.add(new AddCacheVoterTask("notOnAdminVoter", OnAdminVoter.class, config));

        config = new HashMap();
        config.put("enabled", Boolean.FALSE);
        config.put("trueValue", new Long(-1));
        tasks.add(new AddCacheVoterTask("notAuthenticatedVoter", AuthenticatedVoter.class, config));

        return tasks;
    }

}
