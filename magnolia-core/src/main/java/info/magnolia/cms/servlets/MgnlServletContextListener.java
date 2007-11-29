/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.beans.config.ShutdownManager;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.module.PropertyDefinition;
import info.magnolia.logging.Log4jConfigurer;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.model.ModuleDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Magnolia main listener: reads initialization parameter from a properties file. The name of the file can be
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
 *
 */
public class MgnlServletContextListener implements ServletContextListener {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MgnlServletContextListener.class);

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
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce) {
        ModuleManager.Factory.getInstance().stopModules();

        // TODO currently only used for shutting down the repository
        ShutdownManager.getInstance().execute();

        Log4jConfigurer.shutdownLogging(SystemProperty.getProperties());
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce) {
        final ServletContext context = sce.getServletContext();

        String servername = initServername();

        String rootPath = initRootPath(context);

        String webapp = initWebappName(rootPath);

        log.debug("rootPath is {}, webapp is {}", rootPath, webapp); //$NON-NLS-1$

        // load mgnl-beans.properties first
        loadBeanProperties();

        // complete or override with modules' properties
        final ModuleManager moduleManager = ModuleManager.Factory.getInstance();
        try {
            final List moduleDefinitions = moduleManager.loadDefinitions();
            loadModuleProperties(moduleDefinitions);
        } catch (ModuleManagementException e) {
            throw new RuntimeException(e); // TODO
        }

        // complete or override with WEB-INF properties files
        loadPropertiesFiles(context, servername, rootPath, webapp);

        // system property initialization
        String magnoliaRootSysproperty = SystemProperty.getProperty(SystemProperty.MAGNOLIA_ROOT_SYSPROPERTY);
        if (StringUtils.isNotEmpty(magnoliaRootSysproperty)) {
            System.setProperty(magnoliaRootSysproperty, rootPath);
            log.info("Setting the magnolia root system property: {} to {}", magnoliaRootSysproperty, rootPath); //$NON-NLS-1$
        }

        // complete or override with JVM system properties
        overloadWithSystemProperties();

        Log4jConfigurer.initLogging(context);

        new ConfigLoader(context);

    }

    /**
     * Load the properties defined in the module descriptors. They can get overridden later in the properties files in
     * WEB-INF
     */
    protected void loadModuleProperties(List moduleDefinitions) {
        final Iterator it = moduleDefinitions.iterator();
        while (it.hasNext()) {
            final ModuleDefinition module = (ModuleDefinition) it.next();
            final Iterator propsIt = module.getProperties().iterator();
            while (propsIt.hasNext()) {
                final PropertyDefinition property = (PropertyDefinition) propsIt.next();
                SystemProperty.setProperty(property.getName(), property.getValue());
            }
        }
    }

    protected void loadPropertiesFiles(ServletContext context, String servername, String rootPath, String webapp) {
        String propertiesLocationString = context.getInitParameter(MAGNOLIA_INITIALIZATION_FILE);

        if (StringUtils.isEmpty(propertiesLocationString)) {
            log.debug("{} value in web.xml is undefined, falling back to default: {}", MAGNOLIA_INITIALIZATION_FILE, DEFAULT_INITIALIZATION_PARAMETER);
            propertiesLocationString = DEFAULT_INITIALIZATION_PARAMETER;
        }
        else {
            log.debug("{} value in web.xml is :'{}'", MAGNOLIA_INITIALIZATION_FILE, propertiesLocationString); //$NON-NLS-1$
        }

        String[] propertiesLocation = StringUtils.split(propertiesLocationString, ',');

        // we should use the server name without the domain, sometimes it's hard to foresee if
        // getLocalHost().getHostName() will return a qualified or unqualified server name
        String fqServerName = null;
        if (StringUtils.contains(servername, '.')) {
            fqServerName = servername;
            servername = StringUtils.substringBefore(servername, ".");
        }

        boolean found = false;
        for (int j = propertiesLocation.length - 1; j >= 0; j--) {
            String location = StringUtils.trim(propertiesLocation[j]);

            location = StringUtils.replace(location, "${servername}", servername); //$NON-NLS-1$
            location = StringUtils.replace(location, "${webapp}", webapp); //$NON-NLS-1$

            if (loadPropertiesFile(rootPath, location)) {
                found = true;
            }

            // compatibility with old version, in case a fully qualified server name was used
            if (fqServerName != null) {
                location = StringUtils.trim(propertiesLocation[j]);
                if (StringUtils.contains(location, "${servername}")) {
                    location = StringUtils.replace(location, "${servername}", fqServerName); //$NON-NLS-1$
                    location = StringUtils.replace(location, "${webapp}", webapp); //$NON-NLS-1$

                    if (loadPropertiesFile(rootPath, location)) {
                        found = true;
                        log.warn("Deprecated: found a configuration file using server name {}, you should use {} instead.", fqServerName, servername);
                    }
                }
            }
        }

        if (!found) {
            String msg = MessageFormat.format("No configuration found using location list {0}. [servername] is [{1}], [webapp] is [{2}] and base path is [{3}]", new Object[]{ArrayUtils.toString(propertiesLocation), servername, webapp, rootPath}); //$NON-NLS-1$
            log.error(msg);
            throw new ConfigurationException(msg);
        }
    }

    protected String initWebappName(String rootPath) {
        String webapp = StringUtils.substringAfterLast(rootPath, "/"); //$NON-NLS-1$
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_WEBAPP, webapp);
        return webapp;
    }

    protected String initRootPath(final ServletContext context) {
        String rootPath = StringUtils.replace(context.getRealPath(StringUtils.EMPTY), "\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        rootPath = StringUtils.removeEnd(rootPath, "/");
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, rootPath);

        return rootPath;
    }

    protected String initServername() {
        String servername = null;

        try {
            servername = StringUtils.lowerCase(InetAddress.getLocalHost().getHostName());
            SystemProperty.setProperty(SystemProperty.MAGNOLIA_SERVERNAME, servername);
        }
        catch (UnknownHostException e) {
            log.error(e.getMessage());
        }
        return servername;
    }

    protected void loadBeanProperties() {
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
            log.warn("{} not found in the classpath. Check that all the needed implementation classes are defined in your custom magnolia.properties file.", MGNL_BEANS_PROPERTIES);
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
            log.debug("Configuration file not found with path [{}]", initFile.getAbsolutePath()); //$NON-NLS-1$
            return false;
        }

        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(initFile);
        }
        catch (FileNotFoundException e1) {
            log.debug("Configuration file not found with path [{}]", initFile.getAbsolutePath());
            return false;
        }

        try {
            SystemProperty.getProperties().load(fileStream);
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
        Iterator it = SystemProperty.getProperties().keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (System.getProperties().containsKey(key)) {
                log.info("system property found: {}", key);
                String value = System.getProperty(key);
                SystemProperty.setProperty(key, value);
            }
        }
    }
}
