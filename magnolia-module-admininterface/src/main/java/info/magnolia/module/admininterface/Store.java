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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.admininterface.dialogs.ParagraphEditDialog;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public class Store {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Store.class);

    private static Store store = new Store();

    public static Store getInstance() {
        return Store.store;
    }

    private final Map dialogHandlers = new HashMap();

    private final Map dialogPageHandlers = new HashMap();

    private Content localStore;

    /**
     * Map with repository name/handler class for admin tree. When this servlet will receive a call with a parameter
     * <code>repository</code>, the corresponding handler will be used top display the admin tree.
     */
    private final Map treeHandlers = new HashMap();

    protected Store() {
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

    public void registerDialogHandler(String name, Class dialogHandler) {
        registerDialogHandler(name, dialogHandler, null);
    }

    public void registerDialogHandler(String name, Class dialogHandler, Content configNode) {
        log.info("Registering dialog handler [" + name + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        dialogHandlers.put(name, new Object[]{dialogHandler, configNode});
    }

    public DialogMVCHandler getDialogHandler(String name, HttpServletRequest request, HttpServletResponse response) {

        Object[] handlerConfig = (Object[]) dialogHandlers.get(name);

        if (handlerConfig == null) {
            // @todo FIXME!!!
            // the parameter "name" should be the dialog path, not the paragraph name
            // This is a quick patch to get the dialog name from the paragraph name, but we should modify the
            // tags to look for the paragraph description and generate the javascript with the dialog name
            Paragraph par = Paragraph.getInfo(name);
            if (par != null) {
                if (par.getDialogPath() != null) {
                    log
                        .warn("Looking for a dialog using the paragraph name instead of the dialog name. Dialog path changed to "
                            + par.getDialogPath()
                            + ". Please fix me!");
                    handlerConfig = (Object[]) dialogHandlers.get(par.getDialogPath());

                }
            }
        }

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

    public DialogPageMVCHandler getDialogPageHandler(String name, HttpServletRequest request,
        HttpServletResponse response) {

        Class dialogPageHandlerClass = (Class) dialogPageHandlers.get(name);
        if (dialogPageHandlerClass == null) {
            throw new InvalidDialogPageHandlerException(name);
        }

        try {
            Constructor constructor = dialogPageHandlerClass.getConstructor(new Class[]{
                String.class,
                HttpServletRequest.class,
                HttpServletResponse.class});
            return (DialogPageMVCHandler) constructor.newInstance(new Object[]{name, request, response});
        }
        catch (Exception e) {
            throw new InvalidDialogPageHandlerException(name, e);
        }
    }

    public Content getStore() {
        return this.localStore;
    }

    public AdminTreeMVCHandler getTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {

        Class treeHandlerClass = (Class) treeHandlers.get(name);
        if (treeHandlerClass == null) {
            throw new InvalidTreeHandlerException(name);
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

    /**
     * Register only if not yet an other handler is present
     * @param name
     * @param dialogHandler
     */
    public void registerDefaultTreeHandler(String name, Class dialogHandler) {
        if (!this.treeHandlers.containsKey(name)) {
            registerTreeHandler(name, dialogHandler);
        }
    }

    public void registerParagraphDialogHandlers(Content modulesTemplatingDialogsRoot) {
        List dialogs = modulesTemplatingDialogsRoot.collectAllChildren();
        for (Iterator iter = dialogs.iterator(); iter.hasNext();) {
            Content dialog = (Content) iter.next();
            String name = StringUtils.substringAfter(dialog.getHandle(), "/modules/templating/dialogs/");

            registerParagraphDialogHandler(name, dialog);
        }
    }

    /**
     * register the dialogs from the config
     * @param defNode
     */
    public void registerDialogHandlers(Content defNode) {
        // read the dialog configuration

        log.info("registerDialogHandlers in " + defNode.getHandle());
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

    public void registerDialogPageHandler(String name, Class dialogPageHandler) {
        log.info("Registering dialogpage handler [" + name + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        dialogPageHandlers.put(name, dialogPageHandler);
    }

    /**
     * register the pages from the config
     * @param defNode
     */
    public void registerDialogPageHandlers(Content defNode) {
        // read the dialog configuration
        try {
            Collection pages = defNode.getChildren(ItemType.CONTENT.getSystemName());
            pages.addAll(defNode.getChildren(ItemType.CONTENTNODE.getSystemName()));
            for (Iterator iter = pages.iterator(); iter.hasNext();) {
                Content page = (Content) iter.next();
                String name = page.getNodeData("name").getString(); //$NON-NLS-1$
                String className = page.getNodeData("class").getString(); //$NON-NLS-1$
                try {
                    registerDialogPageHandler(name, Class.forName(className));
                }
                catch (ClassNotFoundException e) {
                    log.warn("can't find dialogpage handler class " + className, e); //$NON-NLS-1$
                }
            }
        }
        catch (Exception e) {
            log.warn("can't find dialogpages configuration", e); //$NON-NLS-1$
        }
    }

    public void registerTreeHandler(String name, Class treeHandler) {
        log.info("Registering tree handler [" + name + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        treeHandlers.put(name, treeHandler);
    }

    public void setStore(Content localStore) {
        this.localStore = localStore;
    }

    public void registerTreeHandlers(Content defNode) throws ClassNotFoundException {
        Collection trees = defNode.getChildren(ItemType.CONTENTNODE.getSystemName());
        for (Iterator iter = trees.iterator(); iter.hasNext();) {
            Content tree = (Content) iter.next();
            String name = tree.getNodeData("name").getString(); //$NON-NLS-1$
            String className = tree.getNodeData("class").getString(); //$NON-NLS-1$
            this.registerTreeHandler(name, Class.forName(className));
        }
    }

}