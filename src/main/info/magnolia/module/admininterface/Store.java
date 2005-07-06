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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.admininterface.dialogs.ParagraphEditDialog;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public class Store {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Store.class);

    /**
     * Map with repository name/handler class for admin tree. When this servlet will receive a call with a parameter
     * <code>repository</code>, the corresponding handler will be used top display the admin tree.
     */
    private final Map treeHandlers = new HashMap();

    private final Map dialogHandlers = new HashMap();

    private static Store store = new Store();

    private Content localStore;

    protected Store() {
    }

    public static Store getInstance() {
        return Store.store;
    }

    public void setStore(Content localStore) {
        this.localStore = localStore;
    }

    public Content getStore() {
        return this.localStore;
    }

    public void registerTreeHandler(String name, Class treeHandler) {
        log.info("Registering tree handler [" + name + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        treeHandlers.put(name, treeHandler);
    }

    /**
     * Register only if not yet an other handler is present
     * @param name
     * @param class
     */
    public void registerDefaultTreeHandler(String name, Class dialogHandler) {
        if (!this.treeHandlers.containsKey(name)) {
            registerTreeHandler(name, dialogHandler);
        }
    }

    public AdminTreeMVCHandler getTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {

        Class treeHandlerClass = (Class) treeHandlers.get(name);
        if (treeHandlerClass == null) {
            throw new InvalidDialogHandlerException(name);
        }

        try {
            Constructor constructor = treeHandlerClass.getConstructor(new Class[]{
                String.class,
                HttpServletRequest.class,
                HttpServletResponse.class});
            return (AdminTreeMVCHandler) constructor.newInstance(new Object[]{name, request, response});
        }
        catch (Exception e) {
            throw new InvalidTreeHandlerException(name, e);
        }
    }

    public void registerDialogHandler(String name, Class dialogHandler) {
        registerDialogHandler(name, dialogHandler, null);
    }

    public void registerDialogHandler(String name, Class dialogHandler, Content configNode) {
        log.info("Registering dialog handler [" + name + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        dialogHandlers.put(name, new Object[]{dialogHandler, configNode});
    }

    /**
     * register the dialogs from the config
     * @param store
     * @param path
     */
    public void registerDialogHandlers(Content defNode) {
        // read the dialog configuration
        try {
            Collection dialogs = defNode.getChildren(ItemType.CONTENT.getSystemName());
            dialogs.addAll(defNode.getChildren(ItemType.CONTENTNODE.getSystemName()));
            for (Iterator iter = dialogs.iterator(); iter.hasNext();) {
                Content dialog = (Content) iter.next();
                String name = dialog.getNodeData("name").getString(); //$NON-NLS-1$
                String className = dialog.getNodeData("class").getString(); //$NON-NLS-1$
                try {
                    registerDialogHandler(name, Class.forName(className), dialog);
                }
                catch (ClassNotFoundException e) {
                    log.warn("can't find dialog handler class " + className, e); //$NON-NLS-1$
                }
            }
        }
        catch (Exception e) {
            log.warn("can't find dialogs configuration", e); //$NON-NLS-1$
        }
    }

    public DialogMVCHandler getDialogHandler(String name, HttpServletRequest request, HttpServletResponse response) {

        Object[] handlerConfig = (Object[]) dialogHandlers.get(name);

        if (handlerConfig == null) {
            throw new InvalidDialogHandlerException(name);
        }

        try {

            Class dialogHandlerClass = (Class) handlerConfig[0];
            Content configNode = (Content) handlerConfig[1];
            if (configNode != null) {
                try {
                    Constructor constructor = dialogHandlerClass.getConstructor(new Class[]{
                        String.class,
                        HttpServletRequest.class,
                        HttpServletResponse.class,
                        Content.class});
                    return (DialogMVCHandler) constructor
                        .newInstance(new Object[]{name, request, response, configNode});
                }
                catch (NoSuchMethodException e) {
                    Constructor constructor = dialogHandlerClass.getConstructor(new Class[]{
                        String.class,
                        HttpServletRequest.class,
                        HttpServletResponse.class});
                    return (DialogMVCHandler) constructor.newInstance(new Object[]{name, request, response});
                }
            }

            Constructor constructor = dialogHandlerClass.getConstructor(new Class[]{
                String.class,
                HttpServletRequest.class,
                HttpServletResponse.class});
            return (DialogMVCHandler) constructor.newInstance(new Object[]{name, request, response});
        }
        catch (Exception e) {
            throw new InvalidDialogHandlerException(name, e);
        }

    }

    /**
     * This registers the dialog handler for a paragraph.
     */
    public void registerParagraphDialogHandler(String name, Content dialogContent) {
        try {
            Class handler = ParagraphEditDialog.class;

            String className = dialogContent.getNodeData("class").getString(); //$NON-NLS-1$
            if (StringUtils.isNotEmpty(className)) {
                try {
                    handler = Class.forName(className);
                }
                catch (ClassNotFoundException e) {
                    log.error("registering paragraph: class [" + className + "] not found", e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

            registerDialogHandler(name, handler, dialogContent);
        }
        catch (Exception e) {
            log.error("can't register handle for dialog [" + name + "]", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

}