/*
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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentHandler;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;

import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Revision: $ ($Author: $)
 */
public final class DialogManager {

    /**
     * Logger.
     */
    protected static Logger log = Logger.getLogger(DialogManager.class);

    /**
     * Config node name: "controls".
     */
    private static String DIALOGCONTROLS_CONFIG_NAME = "controls"; //$NON-NLS-1$

    /**
     * "/modules/adminInterface/Config".
     */
    private static String ADMIN_CONFIG_NODE_NAME = "/modules/adminInterface/Config"; //$NON-NLS-1$

    /**
     * Node data name for control class.
     */
    private static String DATA_CONTROL_CLASS = "class"; //$NON-NLS-1$

    /**
     * Node data name for control name.
     */
    private static String DATA_CONTROL_NAME = "name"; //$NON-NLS-1$

    /**
     * don't instantiate.
     */
    private DialogManager() {
        // unused
    }

    /**
     * Called through the initialization process
     */
    public static void init() {
        load();
        registerEventListener();
    }

    /**
     * Register an event listener: reload configuration when something changes.
     */
    private static void registerEventListener() {

        log.info("Registering event listener for Controls"); //$NON-NLS-1$

        try {
            ObservationManager observationManager = ContentRepository
                .getHierarchyManager(ContentRepository.CONFIG)
                .getWorkspace()
                .getObservationManager();

            observationManager.addEventListener(
                new EventListener() {

                    public void onEvent(EventIterator iterator) {
                        // reload everything
                        reload();
                    }
                },
                Event.NODE_ADDED
                    | Event.NODE_REMOVED
                    | Event.PROPERTY_ADDED
                    | Event.PROPERTY_CHANGED
                    | Event.PROPERTY_REMOVED,
                ADMIN_CONFIG_NODE_NAME + "/" + DIALOGCONTROLS_CONFIG_NAME, true, null, null, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for Controls", e); //$NON-NLS-1$
        }
    }

    /**
     * Called through the initialization process
     */
    public static void reload() {
        load();
    }

    /**
     * Loads and caches dialog controls.
     */
    public static void load() {
        log.info("Config : loading dialog controls configuration"); //$NON-NLS-1$

        // reading the configuration from the repository
        HierarchyManager configHierarchyManager = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);

        try {
            Content serverNode = configHierarchyManager.getContent(ADMIN_CONFIG_NODE_NAME);

            Content configNode;
            try {
                configNode = serverNode.getContent(DIALOGCONTROLS_CONFIG_NAME);
            }
            catch (javax.jcr.PathNotFoundException e) {

                log.info("Initialize default configuration for dialog controls"); //$NON-NLS-1$

                configNode = serverNode.createContent(DIALOGCONTROLS_CONFIG_NAME, ItemType.CONTENT);

                ValueFactory valueFactory = configHierarchyManager.getWorkspace().getSession().getValueFactory();

                // sample fckedit dialog
                Content fckedit = configNode.createContent("fckEdit", ItemType.CONTENTNODE); //$NON-NLS-1$
                fckedit.createNodeData(DATA_CONTROL_NAME, valueFactory.createValue("fckEdit")); //$NON-NLS-1$
                fckedit.createNodeData(DATA_CONTROL_CLASS, valueFactory.createValue(DialogFckEdit.class.getName()));
                configHierarchyManager.save();
            }

            if (configNode == null) {
                return;
            }

            Iterator iterator = configNode
                .getChildren(ItemType.CONTENTNODE, ContentHandler.SORT_BY_SEQUENCE)
                .iterator();

            while (iterator.hasNext()) {
                Content controlNode = (Content) iterator.next();

                if (log.isDebugEnabled()) {
                    log.debug("Initializing control [" + controlNode + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                }

                String classNodeData = controlNode.getNodeData(DATA_CONTROL_CLASS).getString();
                String nameNodeData = controlNode.getNodeData(DATA_CONTROL_NAME).getString();

                if (StringUtils.isEmpty(classNodeData) || StringUtils.isEmpty(nameNodeData)) {
                    log.warn("Config : Can't add custom control with name [" //$NON-NLS-1$
                        + nameNodeData + "] and class [" //$NON-NLS-1$
                        + classNodeData + "] specified in node [" //$NON-NLS-1$
                        + controlNode.getName() + "]"); //$NON-NLS-1$

                    continue;
                }
                Class controlClass = null;

                try {
                    controlClass = Class.forName(classNodeData);
                }
                catch (ClassNotFoundException e) {
                    log.error("Config : Failed to load dialog control with class [" + classNodeData, e); //$NON-NLS-1$
                    continue;
                }

                if (!DialogInterface.class.isAssignableFrom(controlClass)) {
                    log.error("Config : Invalid class specified for control [" //$NON-NLS-1$
                        + nameNodeData + "]: does not implement DialogInterface"); //$NON-NLS-1$
                    continue;
                }

                DialogFactory.registerDialog(nameNodeData, controlClass);

            }

        }
        catch (RepositoryException e) {
            log.error("Config : Failed to load dialog controls configuration - " //$NON-NLS-1$
                + ADMIN_CONFIG_NODE_NAME + "/" //$NON-NLS-1$
                + DIALOGCONTROLS_CONFIG_NAME, e);
        }

    }

}
