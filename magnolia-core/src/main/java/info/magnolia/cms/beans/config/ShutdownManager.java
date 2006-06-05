package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.commands.MgnlRepositoryCatalog;
import info.magnolia.context.MgnlContext;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
     * Adds a new <code>ShutdownTask</code>.
     * Most recently added task will be executed first
     * @param task ShutdownTask implementation
     */
    public static void addShutdownTask(ShutdownTask task) {
        coreTasks.add(0,task);
    }

    /**
     * List the shutdown task that the server will execute
     * @return <code>List</code> of
     */
    public static List listShutdownTasks() {
        List allTasks = coreTasks;
        allTasks.addAll(0,customTasks);
        return allTasks;
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce) {
        // nothing to do
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce) {

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
        while(iter.hasNext()) {
            Object element = iter.next();
            log.info("Adding shutdown task:"+element.toString());
            customTasks.add(0,mrc.getCommand((String)element)); // Last Registered First Executed
        }

    }

    protected void onClear() {
        customTasks.clear();
    }
}
