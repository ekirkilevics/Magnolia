package info.magnolia.cms.beans.config;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles shutdown tasks supplied by Providers. Can be used to cleanly shutdown repositories while stopping the web
 * application.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class ShutdownManager implements ServletContextListener {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ShutdownManager.class);

    /**
     * List of <code>ShutdownManager.ShutdownTask</code>s which will be executed when the web application is stopped.
     */
    private static List tasks = new ArrayList();

    /**
     * Adds a new <code>ShutdownTask</code>.
     * Most recently added task will be executed first
     * @param task ShutdownTask implementation
     */
    public static void addShutdownTask(ShutdownTask task) {
        tasks.add(0,task);
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
        
        for (Iterator iter = tasks.iterator(); iter.hasNext();) {
            ShutdownTask task = (ShutdownTask) iter.next();
            try {
                task.execute(sce);
            }
            catch (Throwable e) {
                log.warn(MessageFormat.format("Failed to execute shutdown task {0}: {1} {2}", new Object[]{
                    task,
                    e.getClass().getName(),
                    e.getMessage()}));
            }

        }
    }

    /**
     * Simple interface that tasks should implement.
     */
    public interface ShutdownTask {

        /**
         * This method will be called during shutdown.
         * @param sce ServletContextEvent
         */
        void execute(ServletContextEvent sce);
    }

}
