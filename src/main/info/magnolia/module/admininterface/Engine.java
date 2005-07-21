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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.dialog.DialogManager;
import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.ModuleConfig;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.module.admininterface.trees.AdminTreeConfig;
import info.magnolia.module.admininterface.trees.AdminTreeRoles;
import info.magnolia.module.admininterface.trees.AdminTreeUsers;
import info.magnolia.module.admininterface.trees.AdminTreeWebsite;

import java.util.Collection;
import java.util.Iterator;
import java.util.jar.JarFile;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version 2.0
 */
public class Engine implements Module {

    /**
     * Logger.
     */
    protected static Logger log = Logger.getLogger(Engine.class);

    /**
     * @see info.magnolia.cms.module.Module#init(info.magnolia.cms.module.ModuleConfig)
     */
    public void init(ModuleConfig config) {
        // set local store to be accessed via admin interface classes or JSP
        Store store = Store.getInstance();
        store.setStore(config.getLocalStore());

        registerTrees(store);

        try {
            store.registerDialogHandlers(store.getStore().getContent("dialogs")); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error("can't register the admin interface dialogs", e); //$NON-NLS-1$
        }

        try {
            store.registerDialogPageHandlers(store.getStore().getContent("dialogpages")); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error("can't register the admin interface dialogpages", e); //$NON-NLS-1$
        }

        log.info("Init template"); //$NON-NLS-1$
        Template.init();

        log.info("Init dialog controls"); //$NON-NLS-1$
        DialogManager.init();

        registerEventListeners(store);

    }

    /**
     * @param store
     */
    protected static void registerTrees(Store store) {
        // read the tree configuration
        try {
            Collection trees = store.getStore().getContent("trees") //$NON-NLS-1$
                .getChildren(ItemType.CONTENTNODE.getSystemName());
            for (Iterator iter = trees.iterator(); iter.hasNext();) {
                Content tree = (Content) iter.next();
                String name = tree.getNodeData("name").getString(); //$NON-NLS-1$
                String className = tree.getNodeData("class").getString(); //$NON-NLS-1$
                store.registerTreeHandler(name, Class.forName(className));
            }
        }
        catch (Exception e) {
            log.warn("can't find trees configuration: will use defaults", e); //$NON-NLS-1$
        }
        // register defaults
        store.registerDefaultTreeHandler(ContentRepository.WEBSITE, AdminTreeWebsite.class);
        store.registerDefaultTreeHandler(ContentRepository.USERS, AdminTreeUsers.class);
        store.registerDefaultTreeHandler(ContentRepository.USER_ROLES, AdminTreeRoles.class);
        store.registerDefaultTreeHandler(ContentRepository.CONFIG, AdminTreeConfig.class);
    }

    /**
     * Register an event listener: reload cache configuration when something changes.
     */
    private static void registerEventListeners(final Store store) {

        ObservationManager observationManager = null;

        try {

            observationManager = ContentRepository
                .getHierarchyManager(ContentRepository.CONFIG)
                .getWorkspace()
                .getObservationManager();
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for admininterface module", e); //$NON-NLS-1$
            return;
        }
        log.info("Registering event listener for trees"); //$NON-NLS-1$
        try {
            observationManager.addEventListener(new EventListener() {

                public void onEvent(EventIterator iterator) {
                    // reload everything
                    registerTrees(store);
                }
            }, Event.NODE_ADDED
                | Event.NODE_REMOVED
                | Event.PROPERTY_ADDED
                | Event.PROPERTY_CHANGED
                | Event.PROPERTY_REMOVED, store.getStore().getHandle() + "/" + "trees" //$NON-NLS-1$  //$NON-NLS-2$
            , true, null, null, false);
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for trees", e); //$NON-NLS-1$
        }

        log.info("Registering event listener for dialogs"); //$NON-NLS-1$
        try {
            observationManager.addEventListener(new EventListener() {

                public void onEvent(EventIterator iterator) {
                    try {
                        store.registerDialogHandlers(store.getStore().getContent("dialogs")); //$NON-NLS-1$
                    }
                    catch (Exception e) {
                        log.error("Unable to reload the admin interface dialogs", e); //$NON-NLS-1$
                    }
                }
            }, Event.NODE_ADDED
                | Event.NODE_REMOVED
                | Event.PROPERTY_ADDED
                | Event.PROPERTY_CHANGED
                | Event.PROPERTY_REMOVED, store.getStore().getHandle() + "/" + "dialogs" //$NON-NLS-1$  //$NON-NLS-2$
            , true, null, null, false);
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for dialogs", e); //$NON-NLS-1$
        }

        log.info("Registering event listener for dialogpages"); //$NON-NLS-1$
        try {
            observationManager.addEventListener(new EventListener() {

                public void onEvent(EventIterator iterator) {

                    try {
                        store.registerDialogHandlers(store.getStore().getContent("dialogpages")); //$NON-NLS-1$
                    }
                    catch (Exception e) {
                        log.error("Unable to reload the admin interface dialogpages", e); //$NON-NLS-1$
                    }
                }
            }, Event.NODE_ADDED
                | Event.NODE_REMOVED
                | Event.PROPERTY_ADDED
                | Event.PROPERTY_CHANGED
                | Event.PROPERTY_REMOVED, store.getStore().getHandle() + "/" + "dialogpages" //$NON-NLS-1$  //$NON-NLS-2$
            , true, null, null, false);
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for dialogpages", e); //$NON-NLS-1$
        }

    }

    /**
     * @see info.magnolia.cms.module.Module#register(java.lang.String, java.lang.String, info.magnolia.cms.core.Content,
     * java.util.jar.JarFile, int)
     */
    public void register(String moduleName, String version, Content moduleNode, JarFile jar, int registerState)
        throws RegisterException {
        // nothing todo
    }

    /**
     * @see info.magnolia.cms.module.Module#destroy()
     */
    public void destroy() {
        // nothing to do
    }
}