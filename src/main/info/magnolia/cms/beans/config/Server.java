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
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.security.SecureURI;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 1.1
 */
public final class Server {

    public static final String CONFIG_PAGE = "server";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Server.class);

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
        Server.cachedContent.clear();
        Server.cachedURImapping.clear();
        Server.cachedCacheableURIMapping.clear();
        try {
            log.info("Config : loading Server");
            Content startPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getPage(CONFIG_PAGE);
            Server.cacheContent(startPage);
            log.info("Config : Server config loaded");
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load Server config: " + re.getMessage(), re);
            throw new ConfigurationException("Config : Failed to load Server config: " + re.getMessage(), re);
        }
    }

    public static void reload() throws ConfigurationException {
        log.info("Config : re-loading Server config");
        Server.init();
    }

    /**
     * Cache server content from the config repository.
     */
    private static void cacheContent(Content page) {
        try {
            addToSecureList(page.getContentNode("secureURIList"));
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }

        boolean isAdmin = page.getNodeData("admin").getBoolean();
        Server.cachedContent.put("admin", BooleanUtils.toBooleanObject(isAdmin));

        String ext = page.getNodeData("defaultExtension").getString();
        Server.cachedContent.put("defaultExtension", ext);

        String basicRealm = page.getNodeData("basicRealm").getString();
        Server.cachedContent.put("basicRealm", basicRealm);

        try {
            String mailServer = page.getNodeData("defaultMailServer").getString();
            Server.cachedContent.put("defaultMailServer", mailServer);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            Server.cachedContent.put("defaultMailServer", "");
        }

        Server.cachedContent.put("404URI", page.getNodeData("ResourceNotAvailableURIMapping").getString());

        boolean visibleToObinary = page.getNodeData("visibleToObinary").getBoolean();
        Server.cachedContent.put("visibleToObinary", BooleanUtils.toBooleanObject(visibleToObinary));

    }

    private static void addToSecureList(ContentNode node) {
        if (node == null) {
            return;
        }
        Iterator childIterator = node.getChildren().iterator();
        while (childIterator.hasNext()) {
            ContentNode sub = (ContentNode) childIterator.next();
            String uri = sub.getNodeData("URI").getString();
            SecureURI.add(uri);
        }
    }

    /**
     * @return resource not available URI mapping as specifies in serverInfo, else /
     */
    public static String get404URI() {
        String uri = (String) Server.cachedContent.get("404URI");
        if (uri.equals("")) {
            return "/";
        }
        if (log.isDebugEnabled()) {
            log.debug("404URI is \"" + uri + "\"");
        }
        return uri;
    }

    /**
     * @return default URL extension as configured
     */
    public static String getDefaultExtension() {
        String defaultExtension = (String) Server.cachedContent.get("defaultExtension");
        if (defaultExtension == null) {
            return "";
        }
        return defaultExtension;
    }

    /**
     * @return default mail server
     */
    public static String getDefaultMailServer() {
        return (String) Server.cachedContent.get("defaultMailServer");
    }

    /**
     * @return basic realm string
     */
    public static String getBasicRealm() {
        return (String) Server.cachedContent.get("basicRealm");
    }

    /**
     * @return true if the instance is Admin
     */
    public static boolean isAdmin() {
        return ((Boolean) Server.cachedContent.get("admin")).booleanValue();
    }

    /**
     * @deprecated
     * @see Cache#isCacheable()
     * @return true if the pages could be cached
     */
    public static boolean isCacheable() {
        return Cache.isCacheable();
    }

    /**
     *
     */
    public static boolean isVisibleToObinary() {
        return ((Boolean) Server.cachedContent.get("visibleToObinary")).booleanValue();
    }

    /**
     * @deprecated
     * @see Cache#isCacheable(javax.servlet.http.HttpServletRequest)
     * @return true if the requested URI can be added to cache
     */
    public static boolean isCacheable(HttpServletRequest request) {
        return Cache.isCacheable(request);
    }
}
