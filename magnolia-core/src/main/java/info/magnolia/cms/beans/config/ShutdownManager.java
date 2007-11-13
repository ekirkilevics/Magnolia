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
import info.magnolia.commands.MgnlRepositoryCatalog;
import info.magnolia.context.MgnlContext;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles shutdown coreTasks supplied by Providers. Can be used to cleanly shutdown repositories while stopping the web
 * application.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class ShutdownManager extends ObservedManager implements ServletContextListener {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ShutdownManager.class);

    /**
     * List of <code>ShutdownManager.ShutdownTask</code>s which will be executed when the web application is stopped.
     */
    private static List coreTasks = new ArrayList();

    /**
     * Other tasks that the shutdown manager will execute.
     */
    private static List customTasks = new ArrayList();

    private static ShutdownManager instance = new ShutdownManager();

    public static ShutdownManager getInstance() {
        return instance;
    }

    /**
     * Adds a new <code>ShutdownTask</code>. Most recently added task will be executed first
     * @param task ShutdownTask implementation
     */
    public static void addShutdownTask(ShutdownTask task) {
        coreTasks.add(0, task);
    }

    /**
     * List the shutdown task that the server will execute
     * @return <code>List</code> of
     */
    public static List listShutdownTasks() {
        List allTasks = new ArrayList();
        allTasks.addAll(coreTasks);
        allTasks.addAll(0, customTasks);
        return allTasks;
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     * @deprecated (does nothing on initialization)
     *
     */
    public void contextInitialized(ServletContextEvent sce) {
        log.warn("\n***********\nThe use of ShutdownManager as ServletContextListener is deprecated in Magnolia 3.1, "
            + "please update your web.xml and remove the listener but add a single listener with class name " 
            + "info.magnolia.cms.servlets.MgnlServletContextListener\n***********");

        // nothing to do
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     * @deprecated use {@link #execute()} instead;
     */
    public void contextDestroyed(ServletContextEvent sce) {
        execute();
    }

    /**
     * Executes the registers shutdown tasks
     */
    public void execute() {
        log.info("Executing shutdown tasks");

        for (Iterator iter = listShutdownTasks().iterator(); iter.hasNext();) {
            Command task = (Command) iter.next();
            Context c = MgnlContext.getSystemContext();
            try {
                task.execute(c);
            }
            catch (Throwable e) {
                log.warn(MessageFormat.format("Failed to execute shutdown task {0}: {1} {2}", new Object[]{
                    task,
                    e.getClass().getName(),
                    e.getMessage()}));
            }
        }
    }

    protected void onRegister(Content node) {
        Catalog mrc = new MgnlRepositoryCatalog(node);
        Iterator iter = mrc.getNames();
        while (iter.hasNext()) {
            Object element = iter.next();
            log.info("Adding shutdown task:" + element.toString());
            customTasks.add(0, mrc.getCommand((String) element)); // Last Registered First Executed
        }

    }

    protected void onClear() {
        customTasks.clear();
    }
}
