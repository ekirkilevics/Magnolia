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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import info.magnolia.cms.core.Content;


/**
 * Date: Oct 14, 2004 Time: 3:23:06 PM
 * @author Sameer Charles
 * @version 2.0
 */
public class Store {

    private static Logger log = Logger.getLogger(Store.class);

    /**
     * Map with repository name/handler class for admin tree. When this servlet will receive a call with a parameter
     * <code>repository</code>, the corresponding handler will be used top display the admin tree.
     */
    private final Map treeHandlers = new HashMap();

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
        treeHandlers.put(name, treeHandler);
    }

    public AdminTreeMVCHandler getTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        try {
            Class treeHandlerClass = (Class) treeHandlers.get(name);
            Constructor constructor = treeHandlerClass.getConstructor(new Class[]{String.class, HttpServletRequest.class, HttpServletResponse.class});
            return (AdminTreeMVCHandler) constructor.newInstance(new Object[]{name, request, response});
        }
        catch (Exception e) {
            log.error("can't instanciate the treehandler: " + name, e);
        }
        return null;
    }

}