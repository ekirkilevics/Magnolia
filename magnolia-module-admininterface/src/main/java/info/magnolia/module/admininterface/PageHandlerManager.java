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

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.FactoryUtil;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages the page handlers. A page is a very simple dialog without any configuration.
 * @author philipp
 */
public class PageHandlerManager extends ObservedManager {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(PageHandlerManager.class);

    /**
     * The current implementation of the ParagraphManager. Defeined in magnolia.properties.
     */
    private static PageHandlerManager instance = (PageHandlerManager) FactoryUtil
        .getSingleton(PageHandlerManager.class);

    /**
     * The handlers
     */
    private final Map dialogPageHandlers = new HashMap();

    /**
     * Find a handler by name
     * @param name
     * @param request
     * @param response
     * @returnn an instance of the handlers
     */
    public PageMVCHandler getPageHandler(String name, HttpServletRequest request,
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
            return (PageMVCHandler) constructor.newInstance(new Object[]{name, request, response});
        }
        catch (Exception e) {
            log.error("can't instantiate page ["+name+"]",  e);
            throw new InvalidDialogPageHandlerException(name, e);
        }
    }

    protected void registerPageHandler(String name, Class dialogPageHandler) {
        log.info("Registering page handler [" + name + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        dialogPageHandlers.put(name, dialogPageHandler);
    }

    /**
     * register the pages from the config
     * @param defNode
     */
    protected void onRegister(Content defNode) {
        // read the dialog configuration
        try {
            Collection pages = defNode.getChildren(ItemType.CONTENT.getSystemName());
            pages.addAll(defNode.getChildren(ItemType.CONTENTNODE.getSystemName()));
            for (Iterator iter = pages.iterator(); iter.hasNext();) {
                Content page = (Content) iter.next();
                String name = page.getNodeData("name").getString(); //$NON-NLS-1$
                String className = page.getNodeData("class").getString(); //$NON-NLS-1$
                try {
                    registerPageHandler(name, Class.forName(className));
                }
                catch (ClassNotFoundException e) {
                    log.warn("can't find dialogpage handler class " + className, e); //$NON-NLS-1$
                }
            }
        }
        catch (Exception e) {
            log.warn("can't find pages configuration", e); //$NON-NLS-1$
        }
    }

    /**
     * @return Returns the instance.
     */
    public static PageHandlerManager getInstance() {
        return instance;
    }

    protected void onClear() {
        this.dialogPageHandlers.clear();
    }

}
