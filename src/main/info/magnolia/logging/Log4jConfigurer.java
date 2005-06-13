package info.magnolia.logging;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;


/**
 * <p>
 * Log4j initializer. Loads the file specified using the <code>log4j.config</code> init parameter and optionally set a
 * system property containing the magnolia web application root directory with the name specified by the
 * <code>magnolia.root.sysproperty</code> init parameter.
 * </p>
 * <p>
 * If <code>magnolia.root.sysproperty</code> is empty no system variable will be set; if <code>log4j.config</code>
 * is empty no log4j initialization will be performed.
 * </p>
 * <p>
 * You can easily specify relative paths for log4j configuration files using the magnolia root system property, for
 * example using <code>${magnolia.root}logs/magnolia-debug.log</code>
 * </p>
 * <p>
 * Note: if you drop multiple magnolia wars in a container which doesn't isolate system properties (e.g. tomcat) you
 * could need to change the name of the <code>magnolia.root.sysproperty</code> variable in web.xml and in log4j
 * configuration files.
 * </p>
 * <p>
 * <em>Some ideas and snippets borrowed from the more complex Spring implementation http://www.springframework.org</em>
 * </p>
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public abstract class Log4jConfigurer {

    /**
     * Init parameter specifying the location of the Log4J config file
     */
    public static final String LOG4J_CONFIG = "log4j.config";

    /**
     * Web app root key parameter at the servlet context level (i.e. a context-param in web.xml): "webAppRootKey".
     */
    public static final String MAGNOLIA_ROOT_SYSPROPERTY = "magnolia.root.sysproperty";

    /**
     * Utility class, don't instantiate.
     */
    private Log4jConfigurer() {
        // unused
    }

    /**
     * Initialize Log4J, including setting the web app root system property.
     * @param parameters parameter map, containing the <code>MAGNOLIA_ROOT_SYSPROPERTY</code> and
     * <code>LOG4J_CONFIG</code> properties
     */
    public static void initLogging(ServletContext servletContext, Map parameters) {

        // can't use log4j yet
        log("Initializing Log4J");

        // system property initialization
        String magnoliaRootSysproperty = (String) parameters.get(MAGNOLIA_ROOT_SYSPROPERTY);
        if (StringUtils.isNotEmpty(magnoliaRootSysproperty)) {
            String root = servletContext.getRealPath("/");
            if (StringUtils.isNotEmpty(root)) {
                System.setProperty(magnoliaRootSysproperty, root);
                log("Setting the magnolia root system property: [" + magnoliaRootSysproperty + "] to [" + root + "]");
            }
        }

        String log4jFileName = (String) parameters.get(LOG4J_CONFIG);
        if (StringUtils.isNotEmpty(log4jFileName)) {
            boolean isXml = log4jFileName.toLowerCase().endsWith(".xml");

            log("Initializing Log4J from [" + log4jFileName + "]");
            File log4jFile = new File(servletContext.getRealPath(StringUtils.EMPTY), log4jFileName);
            if (log4jFile.exists()) {
                URL url;
                try {
                    url = new URL("file:" + log4jFile.getAbsolutePath());
                }
                catch (MalformedURLException e) {
                    log("Unable to initialize Log4J from ["
                        + log4jFileName
                        + "], got a MalformedURLException: "
                        + e.getMessage());
                    return;
                }

                if (isXml) {
                    DOMConfigurator.configure(url);
                }
                else {
                    PropertyConfigurator.configure(url);
                }
            }
            else {
                // classpath?
                if (isXml) {
                    DOMConfigurator.configure(log4jFileName);
                }
                else {
                    PropertyConfigurator.configure(log4jFileName);
                }
            }
        }
    }

    /**
     * Shut down Log4J, properly releasing all file locks and resetting the web app root system property.
     * @param parameters parameter map, containing the <code>LOG4J_CONFIG</code> property
     */
    public static void shutdownLogging(Map parameters) {
        log("Shutting down Log4J");
        try {
            LogManager.shutdown();
        }
        finally {
            // Remove the web app root system property.
            String param = (String) parameters.get(MAGNOLIA_ROOT_SYSPROPERTY);
            if (StringUtils.isNotEmpty(param)) {
                System.getProperties().remove(param);
            }
        }
    }

    /**
     * Handy System.out method to use when logging isn't configured yet.
     * @param message log message
     */
    private static void log(String message) {
        System.out.println(message);
    }

}
