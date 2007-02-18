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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.SecureURI;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * $Id$
 */
public final class Server {

    public static final String CONFIG_PAGE = "server"; //$NON-NLS-1$

    /**
     * server properties
     * */
    private static final String PROPERTY_SERVER_ID = "magnolia.server.id";

    /**
     * Logger.
     */
    protected static Logger log = LoggerFactory.getLogger(Server.class);

    private static Map cachedContent = new Hashtable();

    private static Map cachedMailSettings = new Hashtable();

    private static Map loginSettings = new Hashtable();

    private static long uptime = System.currentTimeMillis();

    private static Server instance;

    private Server() {
        // only used internally
    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
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
        Server.cachedMailSettings.clear();
        Server.loginSettings.clear();
        try {
            log.info("Config : loading Server"); //$NON-NLS-1$
            Content startPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getContent(CONFIG_PAGE);
            cacheServerConfiguration(startPage);
            cacheSecureURIList(startPage);
            cacheMailSettings(startPage);
            cacheLoginSettings(startPage);
            log.info("Config : Server config loaded"); //$NON-NLS-1$
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load Server config: " + re.getMessage(), re); //$NON-NLS-1$
            throw new ConfigurationException("Config : Failed to load Server config: " + re.getMessage(), re); //$NON-NLS-1$
        }
    }

    private static void cacheMailSettings(Content page) {
        childrenToMap(page.getChildByName("mail"), cachedMailSettings);
    }

    private static void cacheLoginSettings(Content page) {
        childrenToMap(page.getChildByName("login"), loginSettings);
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

    }

    /**
     * Cache server content from the config repository.
     */
    private static void cacheSecureURIList(Content page) {
        SecureURI.init();
        try {
            addToSecureList(page.getContent("secureURIList")); //$NON-NLS-1$
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }

        try {
            if (page.hasContent("unsecureURIList")) {
                addToUnsecureList(page.getContent("unsecureURIList")); //$NON-NLS-1$
            }
        }
        catch (javax.jcr.PathNotFoundException pn) {
            log.info("No unsecure uri found");
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
    }

    private static void addToUnsecureList(Content node) {
        if (node == null) {
            return;
        }
        Iterator childIterator = node.getChildren(ItemType.CONTENTNODE).iterator();
        while (childIterator.hasNext()) {
            Content sub = (Content) childIterator.next();
            String uri = sub.getNodeData("URI").getString(); //$NON-NLS-1$
            log.info("Adding new unsecure uri: {}", uri);
            SecureURI.addUnsecure(uri);
        }
    }

    /**
     * Register an event listener: reload server configuration when something changes. todo split reloading of base
     * server configuration and secure URI list
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
                false,
                null,
                null,
                false);
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for server", e); //$NON-NLS-1$
        }

        // secure URI list
        addEventListener(new EventListener() {

            public void onEvent(EventIterator iterator) {
                try {
                    reload();
                }
                catch (ConfigurationException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }, "/secureURIList");

        // unsecure URI list
        addEventListener(new EventListener() {

            public void onEvent(EventIterator iterator) {
                try {
                    reload();
                }
                catch (ConfigurationException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }, "/unsecureURIList");

        // login
        addEventListener(new EventListener() {

            public void onEvent(EventIterator iterator) {
                try {
                    reload();
                }
                catch (ConfigurationException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }, "/login");

    }

    /**
     * @param eventListener
     * @param path
     */
    private static void addEventListener(EventListener eventListener, String path) {
        try {
            ObservationManager observationManager = ContentRepository
                .getHierarchyManager(ContentRepository.CONFIG)
                .getWorkspace()
                .getObservationManager();

            observationManager.addEventListener(eventListener, Event.NODE_ADDED
                | Event.NODE_REMOVED
                | Event.PROPERTY_ADDED
                | Event.PROPERTY_CHANGED
                | Event.PROPERTY_REMOVED, "/" + CONFIG_PAGE + path, true, null, null, false); //$NON-NLS-1$
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for server", e); //$NON-NLS-1$
        }
    }

    private static void addToSecureList(Content node) {

        if (node == null) {
            return;
        }
        Iterator childIterator = node.getChildren(ItemType.CONTENTNODE).iterator();
        while (childIterator.hasNext()) {
            Content sub = (Content) childIterator.next();
            String uri = sub.getNodeData("URI").getString(); //$NON-NLS-1$
            SecureURI.addSecure(uri);
        }
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
        String mailServer = (String) Server.cachedMailSettings.get("smtpServer");
        return mailServer != null ? mailServer : StringUtils.EMPTY;
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
        return Boolean.TRUE.equals(Server.cachedContent.get("admin")); //$NON-NLS-1$
    }

    /**
     * Time in ms since the server was started
     */
    public static long getUptime() {
        return System.currentTimeMillis() - uptime;
    }

    public Map getLoginConfig() {
        return loginSettings;
    }

    /**
     * get server ID
     * @return server id as configured in magnolia.properties
     * */
    public static String getServerId() {
        return SystemProperty.getProperty(PROPERTY_SERVER_ID);
    }

    /**
     * @param content
     * @param configMap
     */
    private static void childrenToMap(Content content, Map configMap) {
        if (content == null) {
            return;
        }

        Iterator iter = content.getNodeDataCollection().iterator();
        while (iter.hasNext()) {
            NodeData data = (NodeData) iter.next();
            configMap.put(data.getName(), data.getString());
        }
    }

}
