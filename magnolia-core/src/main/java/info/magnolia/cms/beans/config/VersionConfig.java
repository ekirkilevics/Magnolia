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

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Revision: 1871 $ ($Author: scharles $)
 */
public class VersionConfig {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(VersionConfig.class);

    /**
     * path to config node
     */
    private static final String CONFIG_PATH = "server/version";

    /**
     * maximum index to keep
     */
    public static final String MAX_VERSION_INDEX = "maxVersionIndex";

    /**
     * is versioning is active (at application level, JCR is always configured to version if implemented)
     */
    public static final String ACTIVE = "active";

    /**
     * boolean governing if versioning is actively used
     */
    private static boolean isActive;

    /**
     * maximum number of version index
     */
    private static long maxVersions = 0;

    /**
     * Initialize bean
     */
    protected static void init() {
        load();
        registerEventListener();
    }

    /**
     * load config from the repository
     */
    public static void load() {
        log.info("Config : loading version config");
        try {
            Content startPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getContent(CONFIG_PATH);
            isActive = startPage.getNodeData(ACTIVE).getBoolean();
            maxVersions = startPage.getNodeData(MAX_VERSION_INDEX).getLong();

        }
        catch (RepositoryException re) {
            log.error("Config: Failed to load version config or its not defined - " + re.getMessage());
            log.debug("Exception caught", re);
        }

    }

    /**
     * observe any changes in this config tree
     */
    public static void registerEventListener() {
        log.info("Registering event listener for version"); //$NON-NLS-1$

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
            log.error("Unable to add event listeners for version", e); //$NON-NLS-1$
        }
    }

    /**
     * Read config and reload bean
     */
    public static void reload() {
        load();
    }

    /**
     * Checks active flag in version config
     * @return true if versioning is active at application level
     */
    public static boolean isActive() {
        return isActive;
    }

    /**
     * Get maximum number of versions allowed in version history
     * @return max version index
     */
    public static long getMaxVersionIndex() {
        return maxVersions;
    }

}
