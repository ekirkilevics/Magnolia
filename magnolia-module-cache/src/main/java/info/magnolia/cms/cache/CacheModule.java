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
package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.cache.setup.AddCacheVoterTask;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.voting.voters.RequestHasParametersVoter;
import info.magnolia.voting.voters.OnAdminVoter;
import info.magnolia.voting.voters.ExtensionVoter;
import info.magnolia.voting.voters.AuthenticatedVoter;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class CacheModule implements ModuleLifecycle {

    private static final Logger log = LoggerFactory.getLogger(CacheModule.class);

    private final CacheManager cacheManager = CacheManagerFactory.getCacheManager();

    /**
     * {@inheritDoc}
     */
    public void start(ModuleLifecycleContext moduleLifecycleContext) {

        // @todo should be refactored, it's a step in removing the old module configuration
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        Content configNode;
        try {
            configNode = hm.getContent("/modules/cache/config");
            Content voters = ContentUtil.getOrCreateContent(configNode, "voters", ItemType.CONTENT, true);
            this.cacheManager.init(configNode);

            // check for standard voters
            // standard voters that should be always available. They can be disabled by setting the enable flag to false,
            // but their presence will always be checked
            // TODO : they should also be created on install.

            Map config = new HashMap();
            config.put("enabled", Boolean.TRUE);
            config.put("trueValue", new Long(-1));
            AddCacheVoterTask.addVoter(voters, "notWithParametersVoter", RequestHasParametersVoter.class, config);

            config = new HashMap();
            config.put("enabled", Boolean.TRUE);
            config.put("falseValue", new Long(-1));
            config.put("trueValue", new Long(0));
            config.put("allow", "html,css,js,jpg,gif,png");
            AddCacheVoterTask.addVoter(voters, "extensionVoter", ExtensionVoter.class, config);

            config = new HashMap();
            config.put("enabled", Boolean.TRUE);
            config.put("trueValue", new Long(-1));
            AddCacheVoterTask.addVoter(voters, "notOnAdminVoter", OnAdminVoter.class, config);

            config = new HashMap();
            config.put("enabled", Boolean.FALSE);
            config.put("trueValue", new Long(-1));
            AddCacheVoterTask.addVoter(voters, "notAuthenticatedVoter", AuthenticatedVoter.class, config);


        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        if (this.cacheManager.isRunning()) {
            this.cacheManager.stop();
        }
    }

}
