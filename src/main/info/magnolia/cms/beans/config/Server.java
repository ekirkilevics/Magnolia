/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */




package info.magnolia.cms.beans.config;


import info.magnolia.cms.core.*;
import info.magnolia.cms.beans.runtime.SecureURI;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;



/**
 * User: sameercharles
 * Date: Jun 2, 2003
 * Time: 3:06:43 PM
 * @author Sameer Charles
 * @version 1.1
 */


public class Server {



    private static Logger log = Logger.getLogger(Server.class);
    protected static final String CONFIG_PAGE = "server";

    private static Hashtable cachedContent = new Hashtable();
    private static Hashtable cachedURImapping = new Hashtable();
    private static Hashtable cachedCacheableURIMapping = new Hashtable();



    /**
     * constructor
     */
    public Server() {

    }




    /**
     *
     */
    public static void init() {
        Server.cachedContent.clear();
        Server.cachedURImapping.clear();
        Server.cachedCacheableURIMapping.clear();
        try {
            log.info("Config : loading Server");
            Content startPage
                    = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getPage(CONFIG_PAGE);
            Server.cacheContent(startPage);
            log.info("Config : Server config loaded");
        } catch (RepositoryException re) {
            log.fatal("Config : Failed to load Server config");
            log.fatal(re.getMessage(), re);
        }
    }



    public static void reload() {
        log.info("Config : re-loading Server config");
        Server.init();
    }



    /**
     * <p>Cache server content from the config repository</p>
     *
     */
    private static void cacheContent(Content page) {
        try {
            addToSecureList(page.getContentNode("secureURIList"));
        } catch (RepositoryException re) {}
        try {
            boolean isAdmin = page.getNodeData("admin").getValue().getBoolean();
            Server.cachedContent.put("admin",new Boolean(isAdmin));
        } catch (RepositoryException re) {
            Server.cachedContent.put("admin",new Boolean(false));
        }
        try {
            String ext = page.getNodeData("defaultExtension").getValue().getString();
            Server.cachedContent.put("defaultExtension",ext);
        } catch (RepositoryException re) {
            Server.cachedContent.put("defaultExtension","");
        }
        try {
            String basicRealm = page.getNodeData("basicRealm").getValue().getString();
            Server.cachedContent.put("basicRealm",basicRealm);
        } catch (RepositoryException re) {
            Server.cachedContent.put("basicRealm","");
        }
        try {
            String mailServer = page.getNodeData("defaultMailServer").getString();
            Server.cachedContent.put("defaultMailServer",mailServer);
        } catch (Exception e) {
            Server.cachedContent.put("defaultMailServer","");
        }
        Server.cachedContent.put("404URI",page.getNodeData("ResourceNotAvailableURIMapping").getString());
        try {
            boolean visibleToObinary = page.getNodeData("visibleToObinary").getBoolean();
            Server.cachedContent.put("visibleToObinary",new Boolean(visibleToObinary));
        } catch (Exception e) {
            Server.cachedContent.put("visibleToObinary",new Boolean(false));
        }
    }




    private static void addToSecureList(ContentNode node) {
        if (node == null)
            return;
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
        String URI = (String)Server.cachedContent.get("404URI");
        if (URI.equals(""))
            return "/";
        return URI;
    }



    /**
     * @return default URL extension as configured
     */
    public static String getDefaultExtension() {
        String defaultExtension = (String)Server.cachedContent.get("defaultExtension");
        if (defaultExtension == null)
            return "";
        return defaultExtension;
    }


    /**
     * @return default mail server
     * */
    public static String getDefaultMailServer() {
        return (String)Server.cachedContent.get("defaultMailServer");
    }


    /**
     * @return basic realm string
     * */
    public static String getBasicRealm() {
        return (String)Server.cachedContent.get("basicRealm");
    }


    /**
     * @return true if the instance is Admin
     */
    public static boolean isAdmin() {
        return ((Boolean)Server.cachedContent.get("admin")).booleanValue();
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
     * */
    public static boolean isVisibleToObinary() {
        return ((Boolean)Server.cachedContent.get("visibleToObinary")).booleanValue();
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
