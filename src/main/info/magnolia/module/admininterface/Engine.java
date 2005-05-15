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
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.ModuleConfig;
import info.magnolia.module.admininterface.trees.AdminTreeConfig;
import info.magnolia.module.admininterface.trees.AdminTreeRoles;
import info.magnolia.module.admininterface.trees.AdminTreeUsers;
import info.magnolia.module.admininterface.trees.AdminTreeWebsite;

import java.util.Collection;
import java.util.Iterator;

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
    private static Logger log = Logger.getLogger(Engine.class);

    /**
     * @see info.magnolia.cms.module.Module#init(info.magnolia.cms.module.ModuleConfig)
     */
    public void init(ModuleConfig config) {
        // set local store to be accessed via admin interface classes or JSP
        Store store = Store.getInstance();
        store.setStore(config.getLocalStore());

        registerTrees(store);
        registerDialogs(store, "dialogs");

    }

    /**
     * register the dialogs from the config
     * @param store
     * @param path
     */
    private void registerDialogs(Store store, String path) {
        // read the dialog configuration
        try {
            Collection dialogs = store.getStore().getContent(path).getChildren(ItemType.CONTENTNODE.getSystemName());
            for (Iterator iter = dialogs.iterator(); iter.hasNext();) {
                Content dialog = (Content) iter.next();
                String name = dialog.getNodeData("name").getString();
                String className = dialog.getNodeData("class").getString();
                try {
                    store.registerDialogHandler(name, Class.forName(className), dialog);
                }
                catch (ClassNotFoundException e) {
                    log.warn("can't find dialog handler class " + className, e);
                }
            }
        }
        catch (Exception e) {
            log.warn("can't find dialogs configuration", e);
        }
    }

    /**
     * @param store
     */
    private void registerTrees(Store store) {
        // read the tree configuration
        try {
            Collection trees = store.getStore().getContent("trees").getChildren(ItemType.CONTENTNODE.getSystemName());
            for (Iterator iter = trees.iterator(); iter.hasNext();) {
                Content tree = (Content) iter.next();
                String name = tree.getNodeData("name").getString();
                String className = tree.getNodeData("class").getString();
                store.registerTreeHandler(name, Class.forName(className));
            }
        }
        catch (Exception e) {
            log.warn("can't find trees configuration: will use defaults", e);
        }
        // register defaults
        store.registerDefaultTreeHandler(ContentRepository.WEBSITE, AdminTreeWebsite.class);
        store.registerDefaultTreeHandler(ContentRepository.USERS, AdminTreeUsers.class);
        store.registerDefaultTreeHandler(ContentRepository.USER_ROLES, AdminTreeRoles.class);
        store.registerDefaultTreeHandler(ContentRepository.CONFIG, AdminTreeConfig.class);
    }

    /**
     * @see info.magnolia.cms.module.Module#register(info.magnolia.cms.core.Content)
     */
    public void register(Content moduleNode) {
        // nothing to do
    }

    /**
     * @see info.magnolia.cms.module.Module#destroy()
     */
    public void destroy() {
        // nothing to do
    }
}