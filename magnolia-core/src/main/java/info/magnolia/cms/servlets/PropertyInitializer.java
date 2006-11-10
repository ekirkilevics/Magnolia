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
package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.ModuleRegistration;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.module.ModuleDefinition;
import info.magnolia.cms.module.PropertyDefinition;
import info.magnolia.logging.Log4jConfigurer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.OrderedMapIterator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
 * <h3>Advance use: deployment service</h3>
 * <p>
 * Using the <code>${servername}</code> and <code>${webapp}</code> properties you can easily bundle in the same
 * webapp different set of configurations which are automatically applied dependending on the server name (useful for
 * switching between development, test and production instances where the repository configuration need to be different)
 * or the webapp name (useful to bundle both the public and admin log4j/jndi/bootstrap configuration in the same war).
 * By default the initializer will try to search for the file in different location with different combination of
 * <code>servername</code> and <code>webapp</code>: the <code>default</code> fallback directory will be used if
 * no other environment-specific directory has been added.
 * </p>
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class PropertyInitializer implements ServletContextListener {
    
    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(PropertyInitializer.class);

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
     * The properties file containing the bean default implementations
     */
    private static final String MGNL_BEANS_PROPERTIES = "/mgnl-beans.properties";
    
    /**
     * Environment-specific properties.
     */
    protected Properties envProperties = new Properties();

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
        final ServletContext context = sce.getServletContext();
        
        loadBeanProperties();
        
        loadModuleProperties();

        String propertiesLocationString = context.getInitParameter(MAGNOLIA_INITIALIZATION_FILE);

        if (log.isDebugEnabled()) {
            log.debug("{} value in web.xml is :[{}]", MAGNOLIA_INITIALIZATION_FILE, propertiesLocationString); //$NON-NLS-1$
        }
        if (StringUtils.isEmpty(propertiesLocationString)) {
            propertiesLocationString = DEFAULT_INITIALIZATION_PARAMETER;
        }

        String[] propertiesLocation = StringUtils.split(propertiesLocationString, ',');

        String servername = initServername();

        String rootPath = initRootPath(context);
        
        String webapp = initWebappName(rootPath);

        if (log.isDebugEnabled()) {
            log.debug("rootPath is {}, webapp is {}", rootPath, webapp); //$NON-NLS-1$ 
        }

        createApplicationDirectories(webapp);

        boolean found = false;

        found = loadPropertiesFiles(propertiesLocation, servername, rootPath, webapp, found);
        
        overloadWithSystemProperties();
        
        if(!found){
            log
            .error(MessageFormat
                .format(
                    "No configuration found using location list {0}. [servername] is [{1}], [webapp] is [{2}] and base path is [{3}]", //$NON-NLS-1$
                    new Object[]{ArrayUtils.toString(propertiesLocation), servername, webapp, rootPath}));
            return;
        }
        
        Log4jConfigurer.initLogging(context);

        new ConfigLoader(context);

    }

    /**
     * Load the properties defined in the module descriptors. They can get overridden later in the properties files in WEB-INF
     */
    protected void loadModuleProperties() {
        OrderedMap defs = ModuleRegistration.getInstance().getModuleDefinitions();
        for (OrderedMapIterator iter = defs.orderedMapIterator(); iter.hasNext();) {
            iter.next();
            ModuleDefinition def = (ModuleDefinition) iter.getValue();
            for (Iterator iter2 = def.getProperties().iterator(); iter2.hasNext();) {
                PropertyDefinition property = (PropertyDefinition) iter2.next();
                SystemProperty.setProperty(property.getName(), property.getValue());
            }
        }
    }

    protected boolean loadPropertiesFiles(String[] propertiesLocation, String servername, String rootPath, String webapp, boolean found) {
        for (int j = propertiesLocation.length-1; j >= 0; j--) {
            String location = StringUtils.trim(propertiesLocation[j]);
            location = StringUtils.replace(location, "${servername}", servername); //$NON-NLS-1$
            location = StringUtils.replace(location, "${webapp}", webapp); //$NON-NLS-1$

            if(loadPropertiesFile(rootPath, location)){
                found = true;
            }
        }
        return found;
    }

    private String initWebappName(String rootPath) {
        String webapp = StringUtils.substringAfterLast(rootPath, "/"); //$NON-NLS-1$
        envProperties.put(SystemProperty.MAGNOLIA_WEBAPP, webapp);
        return webapp;
    }

    protected String initRootPath(final ServletContext context) {
        String rootPath = StringUtils.replace(context.getRealPath(StringUtils.EMPTY), "\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        envProperties.put(SystemProperty.MAGNOLIA_APP_ROOTDIR, rootPath);
        
        // system property initialization
        String magnoliaRootSysproperty = (String) envProperties.get(SystemProperty.MAGNOLIA_ROOT_SYSPROPERTY);
        if (StringUtils.isNotEmpty(magnoliaRootSysproperty)) {
            System.setProperty(magnoliaRootSysproperty, rootPath);
            log.info("Setting the magnolia root system property: [" + magnoliaRootSysproperty + "] to [" + rootPath + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        return rootPath;
    }

    protected String initServername() {
        String servername = null;

        try {
            servername = StringUtils.lowerCase(InetAddress.getLocalHost().getHostName());
            envProperties.put(SystemProperty.MAGNOLIA_SERVERNAME, servername);
        }
        catch (UnknownHostException e) {
            log.error(e.getMessage());
        }
        return servername;
    }

    protected void loadBeanProperties() {
        // load mgnl-beans.properties first
        InputStream mgnlbeansStream = getClass().getResourceAsStream(MGNL_BEANS_PROPERTIES);
        
        if (mgnlbeansStream != null) {
            Properties mgnlbeans = new Properties();
            try {
                mgnlbeans.load(mgnlbeansStream);
            }
            catch (IOException e) {
                log.error("Unable to load {} due to an IOException: {}", MGNL_BEANS_PROPERTIES, e.getMessage());
            }
            finally {
                IOUtils.closeQuietly(mgnlbeansStream);
            }

            for (Iterator iter = mgnlbeans.keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                SystemProperty.setProperty(key, mgnlbeans.getProperty(key));
            }

        }
        else {
            log
                .warn(
                    "{} not found in the classpath. Check that all the needed implementation classes are defined in your custom magnolia.properties file.",
                    MGNL_BEANS_PROPERTIES);
        }
    }

    /**
     * Crate the tmp and log directory
     */
    private void createApplicationDirectories(String webapp) {
        File logs = new File(webapp + File.separator + "logs");
        File tmp = new File(webapp + File.separator + "tmp");
        if (!logs.exists()) {
            logs.mkdir();
            log.debug("Creating " + logs.getAbsoluteFile() + " folder");
        }

        if (!tmp.exists()) {
            tmp.mkdir();
            log.debug("Creating " + tmp.getAbsoluteFile() + " folder");
        }
    }

    /**
     * Try to load a magnolia.properties file
     * @param rootPath
     * @param location
     * @return
     */
    protected boolean loadPropertiesFile(String rootPath, String location) {
        File initFile = new File(rootPath, location);

        if (!initFile.exists() || initFile.isDirectory()) {
            if (log.isDebugEnabled()) {
                log.debug("Configuration file not found with path [{}]", //$NON-NLS-1$
                    initFile.getAbsolutePath());
            }
            return false;
        }

        InputStream fileStream=null;
        try {
            fileStream = new FileInputStream(initFile);
        }
        catch (FileNotFoundException e1) {
            log.debug("Configuration file not found with path [{}]", //$NON-NLS-1$
                initFile.getAbsolutePath());
            return false;
        }

        try {
            envProperties.load(fileStream);
            log.info("Loading configuration at {}", initFile.getAbsolutePath());//$NON-NLS-1$
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        finally {
            IOUtils.closeQuietly(fileStream);
        }
        return true;
    }

    /**
     * Overload the properties with set system properties
     */
    protected void overloadWithSystemProperties() {
        Iterator it = envProperties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry param = (Map.Entry) it.next();
            String value = (String)param.getValue();
            if(System.getProperties().containsKey(param.getKey())){
                log.info("system property found: {}", param.getKey());
                value = (String) System.getProperty((String)param.getKey());
            }
            SystemProperty.setProperty((String) param.getKey(), value);
        }
    }
}