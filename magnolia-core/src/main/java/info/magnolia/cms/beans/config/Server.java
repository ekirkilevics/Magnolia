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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.SecureURI;

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
 * @version 1.1
 */
public final class Server {

    public static final String CONFIG_PAGE = "server"; //$NON-NLS-1$

    /**
     * Logger.
     */
    protected static Logger log = Logger.getLogger(Server.class);

    private static Map cachedContent = new Hashtable();

    private static Map cachedURImapping = new Hashtable();

    private static Map cachedCacheableURIMapping = new Hashtable();

    /**
     * Utility class, don't instantiate.
     */
    private Server() {
        // unused
    }

    /**
     * @throws ConfigurationException if basic config nodes are missing
     */
    public static void init() throws ConfigurationException {
        load();
        registerEventListener();
    }

    /**
     * Load the server configuration.
     * @throws ConfigurationException
     */
    public static void load() throws ConfigurationException {
        Server.cachedContent.clear();
        Server.cachedURImapping.clear();
        Server.cachedCacheableURIMapping.clear();
        try {
            log.info("Config : loading Server"); //$NON-NLS-1$
            Content startPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getContent(CONFIG_PAGE);
            cacheServerConfiguration(startPage);
            cacheSecureURIList(startPage);
            log.info("Config : Server config loaded"); //$NON-NLS-1$
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load Server config: " + re.getMessage(), re); //$NON-NLS-1$
            throw new ConfigurationException("Config : Failed to load Server config: " + re.getMessage(), re); //$NON-NLS-1$
        }
    }

    /**
     * Reload the server configuration: simply calls load().
     * @throws ConfigurationException
     */
    public static void reload() throws ConfigurationException {
        log.info("Config : re-loading Server config"); //$NON-NLS-1$
        Server.load();
    }

    /**
     * Cache server content from the config repository.
     */
    private static void cacheServerConfiguration(Content page) {

        boolean isAdmin = page.getNodeData("admin").getBoolean(); //$NON-NLS-1$
        Server.cachedContent.put("admin", BooleanUtils.toBooleanObject(isAdmin)); //$NON-NLS-1$

        String ext = page.getNodeData("defaultExtension").getString(); //$NON-NLS-1$
        Server.cachedContent.put("defaultExtension", ext); //$NON-NLS-1$

        String basicRealm = page.getNodeData("basicRealm").getString(); //$NON-NLS-1$
        Server.cachedContent.put("basicRealm", basicRealm); //$NON-NLS-1$

        try {
            String mailServer = page.getNodeData("defaultMailServer").getString(); //$NON-NLS-1$
            Server.cachedContent.put("defaultMailServer", mailServer); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error(e.getMessage());
            Server.cachedContent.put("defaultMailServer", StringUtils.EMPTY); //$NON-NLS-1$
        }

        Server.cachedContent.put("404URI", page.getNodeData("ResourceNotAvailableURIMapping").getString()); //$NON-NLS-1$ //$NON-NLS-2$

        boolean visibleToObinary = page.getNodeData("visibleToObinary").getBoolean(); //$NON-NLS-1$
        Server.cachedContent.put("visibleToObinary", BooleanUtils.toBooleanObject(visibleToObinary)); //$NON-NLS-1$

    }

    /**
     * Cache server content from the config repository.
     */
    private static void cacheSecureURIList(Content page) {
        try {
            addToSecureList(page.getContent("secureURIList")); //$NON-NLS-1$
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }

    }

    /**
     * Register an event listener: reload server configuration when something changes.
     * todo split reloading of base server configuration and secure URI list
     */
    private static void registerEventListener() {

        log.info("Registering event listener for server"); //$NON-NLS-1$

        // server properties, only on the root server node
        try {
            ObservationManager observationManager = ContentRepository
                .getHierarchyManager(ContentRepository.CONFIG)
                .getWorkspace()
                .getObservationManager();

            observationManager.addEventListener(new EventListener() {

                public void onEvent(EventIterator iterator) {
                    // reload everything
                    try {
                        reload();
                    }
                    catch (ConfigurationException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }, Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED, "/" + CONFIG_PAGE, //$NON-NLS-1$
                false, null, null, false);
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for server", e); //$NON-NLS-1$
        }

        // secure URI list
        try {
            ObservationManager observationManager = ContentRepository
                .getHierarchyManager(ContentRepository.CONFIG)
                .getWorkspace()
                .getObservationManager();

            observationManager.addEventListener(new EventListener() {

                public void onEvent(EventIterator iterator) {
                    // reload everything
                    try {
                        reload();
                    }
                    catch (ConfigurationException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }, Event.NODE_ADDED
                | Event.NODE_REMOVED
                | Event.PROPERTY_ADDED
                | Event.PROPERTY_CHANGED
                | Event.PROPERTY_REMOVED, "/" + CONFIG_PAGE + "/secureURIList", true, null, null, false); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for server", e); //$NON-NLS-1$
        }
    }

    private static void addToSecureList(Content node) {

        if (node == null) {
            return;
        }
        Iterator childIterator = node.getChildren().iterator();
        while (childIterator.hasNext()) {
            Content sub = (Content) childIterator.next();
            String uri = sub.getNodeData("URI").getString(); //$NON-NLS-1$
            SecureURI.add(uri);
        }
    }

    /**
     * @return resource not available URI mapping as specifies in serverInfo, else /
     */
    public static String get404URI() {
        String uri = (String) Server.cachedContent.get("404URI"); //$NON-NLS-1$
        if (StringUtils.isEmpty(uri)) {
            return "/"; //$NON-NLS-1$
        }
        if (log.isDebugEnabled()) {
            log.debug("404URI is \"" + uri + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return uri;
    }

    /**
     * @return default URL extension as configured
     */
    public static String getDefaultExtension() {
        String defaultExtension = (String) Server.cachedContent.get("defaultExtension"); //$NON-NLS-1$
        if (defaultExtension == null) {
            return StringUtils.EMPTY;
        }
        return defaultExtension;
    }

    /**
     * @return default mail server
     */
    public static String getDefaultMailServer() {
        return (String) Server.cachedContent.get("defaultMailServer"); //$NON-NLS-1$
    }

    /**
     * @return basic realm string
     */
    public static String getBasicRealm() {
        return (String) Server.cachedContent.get("basicRealm"); //$NON-NLS-1$
    }

    /**
     * @return true if the instance is configured as an admin server
     */
    public static boolean isAdmin() {
        return ((Boolean) Server.cachedContent.get("admin")).booleanValue(); //$NON-NLS-1$
    }

    /**
     *
     */
    public static boolean isVisibleToObinary() {
        return ((Boolean) Server.cachedContent.get("visibleToObinary")).booleanValue(); //$NON-NLS-1$
    }

    /**
     * @see Cache#isCacheable()
     * @return true if the pages could be cached
     * @deprecated
     */
    public static boolean isCacheable() {
        return Cache.isCacheable();
    }

    /**
     * @param request HttpServletRequest
     * @return true if the requested URI can be added to cache
     * @deprecated
     * @see Cache#isCacheable(javax.servlet.http.HttpServletRequest)
     */
    public static boolean isCacheable(HttpServletRequest request) {
        return Cache.isCacheable(request);
    }
}
