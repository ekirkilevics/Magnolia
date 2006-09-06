package info.magnolia.logging;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.ConfigUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;


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
 * @version $Id$
 */
public abstract class Log4jConfigurer {

    /**
     * Init parameter specifying the location of the Log4J config file
     */
    public static final String LOG4J_CONFIG = "log4j.config"; //$NON-NLS-1$

    /**
     * Utility class, don't instantiate.
     */
    private Log4jConfigurer() {
        // unused
    }

    /**
     * Initialize Log4J, including setting the web app root system property.
     * @param servletContext ServletContext
     * @param parameters parameter map, containing the <code>MAGNOLIA_ROOT_SYSPROPERTY</code> and
     * <code>LOG4J_CONFIG</code> properties
     */
    public static void initLogging(ServletContext servletContext) {

        // can't use log4j yet
        log("Initializing Log4J"); //$NON-NLS-1$

        String log4jFileName = (String) SystemProperty.getProperty(LOG4J_CONFIG);
        if (StringUtils.isNotEmpty(log4jFileName)) {
            boolean isXml = log4jFileName.toLowerCase().endsWith(".xml"); //$NON-NLS-1$

            log("Initializing Log4J from [" + log4jFileName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            
            InputStream stream = ConfigUtil.getConfigFile(log4jFileName);
            
            String config;
            try {
                config = ConfigUtil.replaceTokens(stream);
            }
            catch (IOException e) {
                log("Unable to initialize Log4J from [" //$NON-NLS-1$
                    + log4jFileName
                    + "], got a IOException " //$NON-NLS-1$
                    + e.getMessage());
                return;
            }

            // classpath?
            if (isXml) {
                Document document;
                try {
                    Map dtds = new HashMap();
                    dtds.put("log4j.dtd", "/org/apache/log4j/xml/log4j.dtd");
                    document = ConfigUtil.string2DOM(config, dtds);
                }
                catch (Exception e) {
                    log("Unable to initialize Log4J from [" //$NON-NLS-1$
                        + log4jFileName
                        + "], got an Exception during reading the xml file " //$NON-NLS-1$
                        + e.getMessage());
                    return;
                }
                DOMConfigurator.configure(document.getDocumentElement());
            }
            else {
                Properties properties = new Properties();
                try {
                    properties.load(IOUtils.toInputStream(config));
                }
                catch (IOException e) {
                    log("Unable to initialize Log4J from [" //$NON-NLS-1$
                        + log4jFileName
                        + "], got an Exception during reading the properties file " //$NON-NLS-1$
                        + e.getMessage());
                    return;
                }
                PropertyConfigurator.configure(log4jFileName);
            }

        }
    }

    /**
     * Shut down Log4J, properly releasing all file locks and resetting the web app root system property.
     * @param parameters parameter map, containing the <code>LOG4J_CONFIG</code> property
     */
    public static void shutdownLogging(Map parameters) {
        log("Shutting down Log4J"); //$NON-NLS-1$
        try {
            LogManager.shutdown();
        }
        finally {
            // Remove the web app root system property.
            String param = (String) parameters.get(SystemProperty.MAGNOLIA_ROOT_SYSPROPERTY);
            if (StringUtils.isNotEmpty(param)) {
                System.getProperties().remove(param);
            }
        }
    }

    /**
     * Handy System.out method to use when logging isn't configured yet.
     * @param message log message
     */
    public static void log(String message) {
        System.out.println(message);
    }

}
