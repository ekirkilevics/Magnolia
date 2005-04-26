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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 1.1
 */
public final class Listener {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Listener.class);

    private static final String CONFIG_PAGE = "server";

    private static Iterator ipList;

    private static Map cachedContent = new Hashtable();

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
        try {
            log.info("Config : loading Listener info");
            Content startPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getContent(CONFIG_PAGE);
            Listener.ipList = startPage.getContent("IPConfig").getChildren().iterator();
            Listener.cachedContent.clear();
            Listener.cacheContent();
            log.info("Config : Listener info loaded");
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load Listener info");
            log.error(re.getMessage(), re);
        }
    }

    public static void reload() {
        log.info("Config : re-loading Listener info");
        Listener.init();
    }

    /**
     * Cache listener content from the config repository.
     */
    private static void cacheContent() {
        while (Listener.ipList.hasNext()) {
            Content c = (Content) Listener.ipList.next();
            try {
                Map types = new Hashtable();
                Listener.cachedContent.put(c.getNodeData("IP").getString(), types);
                Iterator it = c.getContent("Access").getChildren().iterator();
                while (it.hasNext()) {
                    Content type = (Content) it.next();
                    types.put(type.getNodeData("Method").getString().toLowerCase(), "");
                }
            }
            catch (RepositoryException re) {
            }
        }
        Listener.ipList = null;
    }

    /**
     * Get access info of the requested IP.
     * @param key IP tp be checked
     * @return Hashtable containing Access info
     * @throws Exception
     */
    public static Map getInfo(String key) throws Exception {
        return (Hashtable) Listener.cachedContent.get(key);
    }
}
