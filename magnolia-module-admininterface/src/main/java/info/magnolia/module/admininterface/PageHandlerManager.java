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
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
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
    public PageMVCHandler getPageHandler(String name, HttpServletRequest request, HttpServletResponse response) {

        PageDefinition pageDefinition = (PageDefinition) dialogPageHandlers.get(name);

        if (pageDefinition == null) {
            log.warn("Page definition not found: \"{}\"", name);
            return null;
        }

        Class dialogPageHandlerClass = pageDefinition.getHandlerClass();
        if (dialogPageHandlerClass == null) {
            throw new InvalidDialogPageHandlerException(name);
        }

        try {
            Constructor constructor = dialogPageHandlerClass.getConstructor(new Class[]{
                String.class,
                HttpServletRequest.class,
                HttpServletResponse.class});
            PageMVCHandler page = (PageMVCHandler) constructor.newInstance(new Object[]{name, request, response});
            BeanUtils.populate(page, pageDefinition.getDefaultProperties());
            return page;
        }
        catch (Exception e) {
            log.error("can't instantiate page [" + name + "]", e);
            throw new InvalidDialogPageHandlerException(name, e);
        }
    }

    /**
     * register the pages from the config
     * @param defNode
     */
    protected void onRegister(Content defNode) {
        // read the dialog configuration

        for (Iterator iter = ContentUtil.getAllChildren(defNode).iterator(); iter.hasNext();) {
            Content pageNode = (Content) iter.next();

            try {
                Map properties = Content2BeanUtil.toMap(pageNode, true);
                String handlerClassName = (String) properties.get("class");
                Class handlerClass = ClassUtil.classForName(handlerClassName);
                String name = StringUtils.defaultIfEmpty((String) properties.get("name"), pageNode.getName());
                properties.remove("class");
                properties.remove("name");
                PageDefinition pd = new PageDefinition(name, handlerClass);
                pd.setDefaultProperties(properties);
                dialogPageHandlers.put(name, pd);
            }
            catch (Content2BeanException e) {
                log.error("can't read page properties [" + pageNode.getHandle() + "]", e);
            }
            catch (ClassNotFoundException e) {
                log.error("can't find class for the page [" + pageNode.getHandle() + "]", e);
            }
        }

    }

    /**
     * @return Returns the instance.
     */
    public static PageHandlerManager getInstance() {
        return (PageHandlerManager) FactoryUtil.getSingleton(PageHandlerManager.class);
    }

    protected void onClear() {
        this.dialogPageHandlers.clear();
    }

    protected static class PageDefinition {

        private Map defaultProperties = new HashMap();

        private Class handlerClass;

        private String name;

        public PageDefinition(String name, Class handlerClass) {
            this.name = name;
            this.handlerClass = handlerClass;
        }

        public Map getDefaultProperties() {
            return this.defaultProperties;
        }

        public void setDefaultProperties(Map defaultProperties) {
            this.defaultProperties = defaultProperties;
        }

        public Class getHandlerClass() {
            return this.handlerClass;
        }

        public void setHandlerClass(Class handlerClass) {
            this.handlerClass = handlerClass;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
