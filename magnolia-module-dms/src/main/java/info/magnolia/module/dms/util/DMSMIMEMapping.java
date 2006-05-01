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
package info.magnolia.module.dms.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Philipp Bracher
 */
public final class DMSMIMEMapping {

    private static final String DEFAULT_ICON = "/.resources/fileIcons/general.gif";

    private static final String DEFAULT_MIMETYPE = "application/octet-stream";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DMSMIMEMapping.class);

    private static final String START_NODE = "server"; //$NON-NLS-1$

    private static Map cachedContent = new Hashtable();

    /**
     * Utility class, don't instantiate.
     */
    private DMSMIMEMapping() {
        // unused
    }

    /**
     * Reads all configured mime mapping (config/server/MIMEMapping).
     */
    public static void init() {
        load();
        registerEventListener();
    }

    /**
     * Reads all configured mime mapping (config/server/MIMEMapping).
     */
    public static void load() {
        DMSMIMEMapping.cachedContent.clear();
        try {
            log.info("Config : loading MIMEMapping"); //$NON-NLS-1$
            Content startPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getContent(START_NODE);
            Collection mimeList = startPage.getContent("MIMEMapping").getChildren(); //$NON-NLS-1$
            DMSMIMEMapping.cacheContent(mimeList);
            log.info("Config : MIMEMapping loaded"); //$NON-NLS-1$
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load MIMEMapping"); //$NON-NLS-1$
            log.error(re.getMessage(), re);
        }
    }

    public static void reload() {
        log.info("Config : re-loading MIMEMapping"); //$NON-NLS-1$
        DMSMIMEMapping.load();
    }

    /**
     * Register an event listener: reload cache configuration when something changes.
     */
    private static void registerEventListener() {

        log.info("Registering event listener for MIMEMapping"); //$NON-NLS-1$

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
                | Event.PROPERTY_REMOVED, "/" + START_NODE + "/" + "MIMEMapping", true, null, null, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for MIMEMapping", e); //$NON-NLS-1$
        }
    }

    /**
     * Cache all MIME types configured.
     */
    private static void cacheContent(Collection mimeList) {
        Iterator iterator = mimeList.iterator();
        while (iterator.hasNext()) {
            Content c = (Content) iterator.next();
            try {
                DMSMIMEMapping.cachedContent.put(c.getNodeData("extension").getString(), c); //$NON-NLS-1$
            }
            catch (Exception e) {
                log.error("Failed to cache MIMEMapping"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Get MIME type String.
     * @param key extension for which MIME type is requested
     * @return MIME type
     */
    public static String getMIMEType(String ext) {
        if (StringUtils.isEmpty(ext)) {
            return DEFAULT_MIMETYPE;
        }

        try {
            return ((Content) DMSMIMEMapping.cachedContent.get(ext.toLowerCase())).getNodeData("mime-type").getString();
        }
        catch (Exception e) {
            log.info("Cannot find MIME type for extension \"" + ext + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            return DEFAULT_MIMETYPE;
        }
    }

    /**
     * Get MIME type String.
     * @param key extension for which MIME type is requested
     * @return MIME type
     */
    public static String getMIMETypeIcon(String ext) {
        if (StringUtils.isEmpty(ext)) {
            return StringUtils.EMPTY;
        }

        String icon = null;
        try {
            Content node = (Content) DMSMIMEMapping.cachedContent.get(ext.toLowerCase());
            if (node != null) {
                icon = node.getNodeData("icon").getString();
            }
        }
        catch (Exception e) {
            log.info("Cannot find MIME type icon for extension \"" + ext + "\"", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (StringUtils.isEmpty(icon)) {
            icon = DEFAULT_ICON;
        }
        return icon;
    }

}
