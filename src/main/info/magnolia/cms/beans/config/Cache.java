/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.CacheHandler;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public final class Cache {

    private static final String CONFIG_PATH = "server/cache/level1"; //$NON-NLS-1$

    private static final String CACHE_MAPPING_NODE = "URI"; //$NON-NLS-1$

    private static final String COMPRESSION_LIST_NODE = "compression"; //$NON-NLS-1$

    private static final String ALLOW_LIST = "allow"; //$NON-NLS-1$

    private static final String DENY_LIST = "deny"; //$NON-NLS-1$

    private static final String ACTIVE = "active"; //$NON-NLS-1$

    private static final String DOMAIN = "domain"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Cache.class);

    private static Map cachedCacheableURIMapping = new HashMap();

    /**
     * Compression wont work for these pre compressed formats.
     */
    private static final Map COMPRESSION_LIST = new Hashtable();

    private static boolean active;

    private static String domain;

    /**
     * Utility class, don't instantiate.
     */
    private Cache() {
        // unused
    }

    protected static void init() {
        load();
        registerEventListener();
    }

    public static void load() {
        cachedCacheableURIMapping.clear();
        COMPRESSION_LIST.clear();

        log.info("Config : loading cache mapping"); //$NON-NLS-1$
        try {
            Content startPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getContent(CONFIG_PATH);
            active = startPage.getNodeData(ACTIVE).getBoolean();
            if (active) {
                domain = startPage.getNodeData(DOMAIN).getString();
                Content contentNode = startPage.getContent(CACHE_MAPPING_NODE + "/" + ALLOW_LIST); //$NON-NLS-1$
                cacheCacheableURIMappings(contentNode, true);
                contentNode = startPage.getContent(CACHE_MAPPING_NODE + "/" + DENY_LIST); //$NON-NLS-1$
                cacheCacheableURIMappings(contentNode, false);
                Content compressionListNode = startPage.getContent(COMPRESSION_LIST_NODE);
                updateCompressionList(compressionListNode);
                // @todo sort ascending so there wont be too much work on comparing
            }
            log.info("Config: cache mapping loaded"); //$NON-NLS-1$
        }
        catch (RepositoryException re) {
            log.error("Config: Failed to load cache mapping or no mapping defined - " + re.getMessage(), re); //$NON-NLS-1$
        }
    }

    public static void reload() {
        log.info("Config : reloading cache mapping"); //$NON-NLS-1$

        // @todo this should probably not be here, but it's important to remove cached entries when the configuration is
        // reloaded
        info.magnolia.cms.beans.runtime.Cache.clearCachedURIList();
        CacheHandler.flushCache();

        load();
    }

    /**
     * Register an event listener: reload cache configuration when something changes.
     */
    private static void registerEventListener() {

        log.info("Registering event listener for cache"); //$NON-NLS-1$

        try {
            ObservationManager observationManager = ContentRepository
                .getHierarchyManager(ContentRepository.CONFIG)
                .getWorkspace()
                .getObservationManager();

            observationManager.addEventListener(new EventListener() {

                public void onEvent(EventIterator iterator) {
                    // reload everything
                    reload();
                }
            }, Event.NODE_ADDED
                | Event.NODE_REMOVED
                | Event.PROPERTY_ADDED
                | Event.PROPERTY_CHANGED
                | Event.PROPERTY_REMOVED, "/" + CONFIG_PATH, true, null, null, false); //$NON-NLS-1$
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for cache", e); //$NON-NLS-1$
        }
    }

    /**
     * @param nodeList to be added in cache
     */
    private static void cacheCacheableURIMappings(Content nodeList, boolean allow) {
        if (nodeList == null) {
            return;
        }
        Iterator it = nodeList.getChildren().iterator();
        while (it.hasNext()) {
            Content container = (Content) it.next();
            NodeData uri = container.getNodeData(CACHE_MAPPING_NODE);
            UrlPattern p = new SimpleUrlPattern(uri.getString());
            cachedCacheableURIMapping.put(p, BooleanUtils.toBooleanObject(allow));
        }
        try {
            CacheHandler.validatePath(CacheHandler.CACHE_DIRECTORY);
        }
        catch (Exception e) {
            log.error("Failed to validate cache directory location: " + e.getMessage(), e); //$NON-NLS-1$
        }
    }

    private static void updateCompressionList(Content list) {
        if (list == null) {
            return;
        }
        Iterator it = list.getChildren().iterator();
        while (it.hasNext()) {
            Content node = (Content) it.next();
            COMPRESSION_LIST.put(node.getNodeData("extension").getString(), node.getNodeData("type").getString()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static boolean applyCompression(String key) {
        return COMPRESSION_LIST.containsKey(key.trim().toLowerCase());
    }

    /**
     * If cache enabled?.
     * @return <code>true</code> if cache is enabled
     * @deprecated use Cache.isActive()
     * @see Cache#isActive()
     */
    public static boolean isCacheable() {
        return active;
    }

    /**
     * If cache enabled?.
     * @return <code>true</code> if cache is enabled
     */
    public static boolean isActive() {
        return active;
    }

    public static String getDomain() {
        return domain;
    }

    /**
     * @return true if the requested URI can be added to cache
     */
    public static boolean isCacheable(HttpServletRequest request) {

        // is cache enabled?
        if (!isActive() || Server.isAdmin()) {
            return false;
        }

        // don't cache POSTs or requests with parameters
        if ("POST".equals(request.getMethod()) || request.getParameterMap().size() > 0) { //$NON-NLS-1$
            return false;
        }

        // first check for MIMEMappings, extension must exist otherwise its a fake request
        if (StringUtils.isEmpty(MIMEMapping.getMIMEType(Path.getExtension(request)))) {
            return false;
        }

        Iterator listEnum = cachedCacheableURIMapping.keySet().iterator();

        String uri = Path.getURI(request);
        boolean isAllowed = false;
        int lastMatchedPatternlength = 0;

        while (listEnum.hasNext()) {
            UrlPattern p = (UrlPattern) listEnum.next();
            if (p.match(uri)) {
                int patternLength = p.getLength();
                if (lastMatchedPatternlength < patternLength) {
                    lastMatchedPatternlength = patternLength;
                    isAllowed = ((Boolean) cachedCacheableURIMapping.get(p)).booleanValue();
                }
            }
        }
        return isAllowed;
    }

}
