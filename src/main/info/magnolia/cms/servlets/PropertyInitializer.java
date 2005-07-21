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
package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.logging.Log4jConfigurer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * <p>
 * Magnolia property initializer: reads initialization parameter from a properties file. The name of the file can be
 * defined as a context parameter in web.xml. Multiple path, comma separated, are supported (the first existing file in
 * the list will be used), and the following variables will be used:
 * </p>
 * <ul>
 * <li><code>${servername}</code>: name of the server where the webapp is running, lowercase</li>
 * <li><code>${webapp}</code>: the latest token in the webapp path (e.g. <code>magnoliaPublic</code> for a webapp
 * deployed ad <code>tomcat/webapps/magnoliaPublic</code>)</li>
 * </ul>
 * <p>
 * If no <code>magnolia.initialization.file</code> context parameter is set, the following default is assumed:
 * </p>
 *
 * <pre>
 * &lt;context-param>
 *   &lt;param-name>magnolia.initialization.file&lt;/param-name>
 *   &lt;param-value>
 *      WEB-INF/config/${servername}/${webapp}/magnolia.properties,
 *      WEB-INF/config/${servername}/magnolia.properties,
 *      WEB-INF/config/${webapp}/magnolia.properties,
 *      WEB-INF/config/default/magnolia.properties,
 *      WEB-INF/config/magnolia.properties
 *   &lt;/param-value>
 * &lt;/context-param>
 * </pre>
 *
 * The following parameters are needed in the properties file:
 * <dl>
 * <dt>magnolia.cache.startdir</dt>
 * <dd>directory used for cached pages</dd>
 * <dt>magnolia.upload.tmpdir</dt>
 * <dd>tmp directory for uploaded files</dd>
 * <dt>magnolia.exchange.history</dt>
 * <dd>history directory used for activation</dd>
 * <dt>magnolia.repositories.config</dt>
 * <dd>repositories configuration</dd>
 * <dt>log4j.config</dt>
 * <dd>Name of a log4j config file. Can be a .properties or .xml file. The value can be:
 * <ul>
 * <li>a full path</li>
 * <li>a path relative to the webapp root</li>
 * <li> a file name which will be loaded from the classpath</li>
 * </ul>
 * </dd>
 * <dt>magnolia.root.sysproperty</dt>
 * <dd>Name of a system variable which will be set to the webapp root. You can use this property in log4j configuration
 * files to handle relative paths, such as <code>${magnolia.root}logs/magnolia-debug.log</code>. <strong>Important</strong>:
 * if you drop multiple magnolia wars in a container which doesn't isolate system properties (e.g. tomcat) you will need
 * to change the name of the <code>magnolia.root.sysproperty</code> variable in web.xml and in log4j configuration
 * files.</dd>
 * <dt>magnolia.bootstrap.dir</dt>
 * <dd> Directory containing xml files for initialization of a blank magnolia instance. If no content is found in any of
 * the repository, they are initialized importing xml files found in this folder. If you don't want to let magnolia
 * automatically initialize repositories simply remove this parameter.</dd>
 * </dl>
 * @author Fabrizio Giustina
 * @version $Id: PropertyInitializer.java 1110 2005-07-06 15:30:44Z fgiust $
 */
public class PropertyInitializer implements ServletContextListener {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Initializer.class);

    /**
     * Context parameter name.
     */
    public static final String MAGNOLIA_INITIALIZATION_FILE = "magnolia.initialization.file"; //$NON-NLS-1$

    /**
     * Default value for the MAGNOLIA_INITIALIZATION_FILE parameter.
     */
    public static final String DEFAULT_INITIALIZATION_PARAMETER = //
    "WEB-INF/config/${servername}/${webapp}/magnolia.properties," //$NON-NLS-1$
        + "WEB-INF/config/${servername}/magnolia.properties," //$NON-NLS-1$
        + "WEB-INF/config/${webapp}/magnolia.properties," //$NON-NLS-1$
        + "WEB-INF/config/default/magnolia.properties," //$NON-NLS-1$
        + "WEB-INF/config/magnolia.properties"; //$NON-NLS-1$

    /**
     * Environment-specific properties.
     */
    private Properties envProperties = new Properties();

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce) {
        Log4jConfigurer.shutdownLogging(envProperties);
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        String propertiesLocationString = context.getInitParameter(MAGNOLIA_INITIALIZATION_FILE);
        if (StringUtils.isEmpty(propertiesLocationString)) {
            propertiesLocationString = DEFAULT_INITIALIZATION_PARAMETER;
        }

        String[] propertiesLocation = StringUtils.split(propertiesLocationString, ',');

        String servername = null;

        try {
            servername = StringUtils.lowerCase(InetAddress.getLocalHost().getHostName());
        }
        catch (UnknownHostException e) {
            log.error(e.getMessage());
        }

        String rootPath = context.getRealPath("/"); //$NON-NLS-1$
        String webapp = StringUtils.substringAfterLast(StringUtils.replace(rootPath, "\\", //$NON-NLS-1$
            "/"), "/"); //$NON-NLS-1$ //$NON-NLS-2$

        for (int j = 0; j < propertiesLocation.length; j++) {
            String location = StringUtils.trim(propertiesLocation[j]);
            location = StringUtils.replace(location, "${servername}", servername); //$NON-NLS-1$
            location = StringUtils.replace(location, "${webapp}", webapp); //$NON-NLS-1$

            File initFile = new File(rootPath, location);

            if (!initFile.exists() || initFile.isDirectory()) {
                if (log.isDebugEnabled()) {
                    log.debug(MessageFormat.format("Configuration file not found with path [{0}]", //$NON-NLS-1$
                        new Object[]{initFile.getAbsolutePath()}));
                }
                continue;
            }

            InputStream fileStream;
            try {
                fileStream = new FileInputStream(initFile);
            }
            catch (FileNotFoundException e1) {

                log.fatal(MessageFormat.format("Configuration file not found with path [{0}]", //$NON-NLS-1$
                    new Object[]{initFile.getAbsolutePath()}));
                return;
            }

            try {
                envProperties.load(fileStream);
                fileStream.close();

                log.info(MessageFormat.format("Loading configuration at {0}", new Object[]{initFile //$NON-NLS-1$
                    .getAbsolutePath()}));

                Log4jConfigurer.initLogging(context, envProperties);

                new ConfigLoader(context, envProperties);
            }
            catch (Exception e) {
                log.fatal(e.getMessage(), e);
            }
            return;

        }

        log
            .fatal(MessageFormat
                .format(
                    "No configuration found using location list {0}. ${servername} is [{1}], ${webapp} is [{2}] and base path is [{3}]", //$NON-NLS-1$
                    new Object[]{ArrayUtils.toString(propertiesLocation), servername, webapp, rootPath}));

    }
}