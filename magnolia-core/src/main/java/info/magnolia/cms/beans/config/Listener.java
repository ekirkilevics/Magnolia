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

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version 1.1
 *
 * @deprecated rename+merge with info.magnolia.cms.security.Listener
 */
public final class Listener {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Listener.class);

    private static final String CONFIG_PAGE = "server"; //$NON-NLS-1$

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
        load();
        registerEventListener();
    }

    /**
     * Reads listener config from the config repository and caches its content in to the hash table.
     */
    public static void load() {

        log.info("Config : loading Listener info"); //$NON-NLS-1$

        Collection children = Collections.EMPTY_LIST;

        try {

            Content startPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getContent(CONFIG_PAGE);
            Content configNode = startPage.getContent("IPConfig");
            children = configNode.getChildren(ItemType.CONTENTNODE);
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load Listener info"); //$NON-NLS-1$
            log.error(re.getMessage(), re);
        }

        Listener.cachedContent.clear();
        Listener.cacheContent(children);
        log.info("Config : Listener info loaded"); //$NON-NLS-1$
    }

    public static void reload() {
        log.info("Config : re-loading Listener info"); //$NON-NLS-1$
        Listener.load();
    }

    /**
     * Register an event listener: reload cache configuration when something changes.
     */
    private static void registerEventListener() {

        log.info("Registering event listener for Listeners"); //$NON-NLS-1$

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
                | Event.PROPERTY_REMOVED, "/" + CONFIG_PAGE + "/" + "IPConfig", true, null, null, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for Listeners", e); //$NON-NLS-1$
        }
    }

    /**
     * Cache listener content from the config repository.
     */
    private static void cacheContent(Collection listeners) {

        Iterator ipList = listeners.iterator();
        while (ipList.hasNext()) {
            Content c = (Content) ipList.next();
            try {
                Map types = new Hashtable();
                Listener.cachedContent.put(c.getNodeData("IP").getString(), types); //$NON-NLS-1$
                Iterator it = c.getContent("Access").getChildren().iterator(); //$NON-NLS-1$
                while (it.hasNext()) {
                    Content type = (Content) it.next();
                    types.put(type.getNodeData("Method").getString().toLowerCase(), StringUtils.EMPTY); //$NON-NLS-1$
                }
            }
            catch (RepositoryException re) {
                log.error("RepositoryException caught while loading listener configuration: " + re.getMessage(), re); //$NON-NLS-1$
            }
        }
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
