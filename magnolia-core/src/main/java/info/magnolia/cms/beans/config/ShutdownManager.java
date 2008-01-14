/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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
        log.warn("\n***********\nThe use of ShutdownManager as ServletContextListener is deprecated in Magnolia 3.5, "
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
