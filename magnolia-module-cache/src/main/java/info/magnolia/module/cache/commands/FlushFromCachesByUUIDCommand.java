/**
 * This file Copyright (c) 2010-2010 Magnolia International
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
package info.magnolia.module.cache.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheConfiguration;
import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicy;

/**
 * Command to completely flush all entries related to given uuid from all available caches.
 * @author had
 * @version $Id:$
 */
public class FlushFromCachesByUUIDCommand extends MgnlCommand {

    public static final String REPOSITORY = "repository";
    public static final String UUID = "uuid";

    private final CacheModule cacheModule;

    public FlushFromCachesByUUIDCommand() {
        cacheModule = ModuleRegistry.Factory.getInstance().getModuleInstance(CacheModule.class);
    }

    @Override
    public boolean execute(Context ctx) throws Exception {
        final String uuid = (String) ctx.get(UUID);
        final String repository = (String) ctx.get(REPOSITORY);
        final Set<Object> keySet = new HashSet<Object>();
        Collection<CacheConfiguration> cacheConfigs = cacheModule.getConfigurations().values();
        for (CacheConfiguration config : cacheConfigs) {
            CachePolicy policy = config.getCachePolicy();
            if (policy == null) {
                continue;
            }
            keySet.addAll(Arrays.asList(policy.removeCacheKeys(uuid, repository)));
        }

        CacheFactory factory = cacheModule.getCacheFactory();
        // flush each key from all caches
        for (Object key : keySet) {
            for (CacheConfiguration config : cacheConfigs) {
                final Cache cache = factory.getCache(config.getName());
                cache.remove(key);
            }
        }
        return true;
    }

}
