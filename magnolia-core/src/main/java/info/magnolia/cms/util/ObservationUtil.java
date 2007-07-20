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
package info.magnolia.cms.util;

import info.magnolia.context.MgnlContext;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ObservationUtil {
    private final static Logger log = LoggerFactory.getLogger(ObservationUtil.class);

    /**
     * Registers an EventListener for any node type.
     * @see #registerChangeListener(String,String,boolean,String[],javax.jcr.observation.EventListener)
     */
    public static void registerChangeListener(String repository, String observationPath, EventListener listener) {
        registerChangeListener(repository, observationPath, true, listener);
    }

    /**
     * Registers an EventListener for any node type.
     * @see #registerChangeListener(String,String,boolean,String[],javax.jcr.observation.EventListener)
     */
    public static void registerChangeListener(String repository, String observationPath, boolean includeSubnodes, EventListener listener) {
        registerChangeListener(repository, observationPath, includeSubnodes, (String[]) null, listener);
    }

    /**
     * Registers an EventListener for a specific node type.
     * @see #registerChangeListener(String,String,boolean,String[],javax.jcr.observation.EventListener)
     */
    public static void registerChangeListener(String repository, String observationPath, boolean includeSubnodes, String nodeType, EventListener listener) {
        registerChangeListener(repository, observationPath, includeSubnodes, new String[]{nodeType}, listener);
    }

    /**
     * Register a single event listener, bound to the given path.
     * Be careful that if you observe "/", events are going to be generated for jcr:system, which is "shared" accross all workspaces.
     *
     * @param repository
     * @param observationPath repository path
     * @param includeSubnodes the isDeep parameter of ObservationManager.addEventListener()
     * @param nodeTypes the node types to filter events for
     * @param listener event listener
     * @see ObservationManager#addEventListener
     */
    public static void registerChangeListener(String repository, String observationPath, boolean includeSubnodes, String[] nodeTypes, EventListener listener) {
        log.debug("Registering event listener for path [{}]", observationPath); //$NON-NLS-1$

        try {

            ObservationManager observationManager = MgnlContext.getSystemContext()
                    .getHierarchyManager(repository)
                    .getWorkspace()
                    .getObservationManager();

            observationManager.addEventListener(listener, Event.NODE_ADDED
                    | Event.NODE_REMOVED
                    | Event.PROPERTY_ADDED
                    | Event.PROPERTY_CHANGED
                    | Event.PROPERTY_REMOVED, observationPath, includeSubnodes, null, nodeTypes, false);
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for " + observationPath, e); //$NON-NLS-1$
        }
    }
}
