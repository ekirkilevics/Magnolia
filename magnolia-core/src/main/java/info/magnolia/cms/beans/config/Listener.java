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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.util.DeprecationUtil;

import java.util.Map;


/**
 * @author Sameer Charles
 * @version 1.1
 *
 * @deprecated since 3.5 - now use {@link info.magnolia.cms.security.IPSecurityManager}
 * @see info.magnolia.cms.security.IPSecurityManager
 */
public final class Listener {
    /**
     * Utility class, don't instantiate.
     */
    private Listener() {
        // unused
    }

    /**
     * Reads listener config from the config repository and caches its content in to the hash table.
     */
    public static void init() {
        DeprecationUtil.isDeprecated("Please use info.magnolia.cms.security.IPSecurityManager");
        DeprecationUtil.isDeprecated("Please use info.magnolia.cms.security.IPSecurityManager");
        /* @deprecated
        load();
        registerEventListener();
        */
    }

    /**
     * Reads listener config from the config repository and caches its content in to the hash table.
     */
    public static void load() {
        DeprecationUtil.isDeprecated("Please use info.magnolia.cms.security.IPSecurityManager");
        /* @deprecated
        log.info("Config : loading Listener info"); //$NON-NLS-1$

        Collection children = Collections.EMPTY_LIST;

        try {
            final HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
            final Content configNode = hm.getContent(CONFIG_PATH);
            children = configNode.getChildren(ItemType.CONTENTNODE);
        } catch (PathNotFoundException e) {
            log.warn("Config : no Listener info configured at " + CONFIG_PATH); //$NON-NLS-1$
            return;
        } catch (RepositoryException re) {
            log.error("Config : Failed to load Listener info"); //$NON-NLS-1$
            log.error(re.getMessage(), re);
        }

        cachedContent.clear();
        cacheContent(children);
        log.info("Config : Listener info loaded"); //$NON-NLS-1$
        */
    }

    public static void reload() {
        DeprecationUtil.isDeprecated("Please use info.magnolia.cms.security.IPSecurityManager");
        /* @deprecated
        log.info("Config : re-loading Listener info"); //$NON-NLS-1$
        load();
        */
    }

    /**
     * Get access info of the requested IP.
     * @param key IP tp be checked
     * @return Hashtable containing Access info
     * @throws Exception
     */
    public static Map getInfo(String key) throws Exception {
        DeprecationUtil.isDeprecated("Please use info.magnolia.cms.security.IPSecurityManager");
        throw new IllegalStateException("Please use info.magnolia.cms.security.IPSecurityManager");
        // return (Hashtable) cachedContent.get(key);
    }
}
