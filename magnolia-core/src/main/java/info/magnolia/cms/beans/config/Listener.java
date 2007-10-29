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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.util.DeprecationUtil;

import java.util.Map;


/**
 * @author Sameer Charles
 * @version 1.1
 *
 * @deprecated since 3.1 - now use {@link info.magnolia.cms.security.IPSecurityManager}
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
