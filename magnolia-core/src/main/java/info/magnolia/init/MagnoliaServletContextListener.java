/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.init;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Stage;
import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.MgnlContext;
import info.magnolia.logging.Log4jConfigurer;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.configuration.ComponentConfigurationBuilder;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;


/**
 * <p>
 * Magnolia main listener: reads initialization parameter from a properties file. The name of the file can be defined as
 * a context parameter in web.xml. Multiple path, comma separated, are supported (the first existing file in the list
 * will be used), and the following variables will be used:
 * </p>
 * <ul>
 * <li><b><code>${servername}</code></b>: name of the server where the webapp is running, lowercase</li>
 * <li><b><code>${webapp}</code></b>: the latest token in the webapp path (e.g. <code>magnoliaPublic</code> for a webapp
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
 * <p>
 * The ${servername} variable will be resolved to the full name obtained by using
 * InetAddress.getLocalHost().getHostName(), which may also contain the server domain, depending on your server
 * configuration/operating system. You can set the optional context parameter "magnolia.unqualified.server.name" to true
 * if you prefer using the unqualified name (the server "server.domain.com" will be simply resolved as "server")
 * </p>
 *
 * <pre>
 * &lt;context-param>
 *   &lt;param-name>magnolia.unqualified.server.name&lt;/param-name>
 *   &lt;param-value>true&lt;/param-value>
 * &lt;/context-param>
 * </pre>
 *
 * The following parameters are needed in the properties file:
 * <ul>
 * <li><b>magnolia.cache.startdir</b>:<br/>
 * directory used for cached pages</li>
 * <li><b>magnolia.upload.tmpdir</b>:<br/>
 * tmp directory for uploaded files</li>
 * <li><b>magnolia.exchange.history</b>:<br/>
 * history directory used for activation</li>
 * <li><b>magnolia.repositories.config</b>:<br/>
 * repositories configuration</li>
 * <li><b>log4j.config</b>:<br/>
 * Name of a log4j config file. Can be a .properties or .xml file. The value can be:
 * <ul>
 * <li>a full path</li>
 * <li>a path relative to the webapp root</li>
 * <li>a file name which will be loaded from the classpath</li>
 * </ul>
 * </li>
 * <li><b>magnolia.root.sysproperty</b>:<br/>
 * Name of a system variable which will be set to the webapp root. You can use this property in log4j configuration
 * files to handle relative paths, such as <code>${magnolia.root}logs/magnolia-debug.log</code>.
 * <strong>Important</strong>: if you drop multiple magnolia wars in a container which doesn't isolate system properties
 * (e.g. tomcat) you will need to change the name of the <code>magnolia.root.sysproperty</code> variable in web.xml and
 * in log4j configuration files.</li>
 * <li><b>magnolia.bootstrap.dir</b>:<br/>
 * Directory containing xml files for initialization of a blank magnolia instance. If no content is found in any of
 * the repository, they are initialized importing xml files found in this folder. If you don't want to let magnolia
 * automatically initialize repositories simply remove this parameter.</li>
 * </ul>
 * <h3>Advance use: deployment service</h3>
 * <p>
 * Using the <code>${servername}</code> and <code>${webapp}</code> properties you can easily bundle in the same webapp
 * different set of configurations which are automatically applied dependending on the server name (useful for switching
 * between development, test and production instances where the repository configuration need to be different) or the
 * webapp name (useful to bundle both the public and admin log4j/jndi/bootstrap configuration in the same war). By
 * default the initializer will try to search for the file in different location with different combination of
 * <code>servername</code> and <code>webapp</code>: the <code>default</code> fallback directory will be used if no other
 * environment-specific directory has been added.
 * </p>
 * @author Fabrizio Giustina
 *
 * TODO : javadoc - update javadoc to reflect current code and point to references instead of duplicating.
 */
@Singleton
public class MagnoliaServletContextListener implements ServletContextListener {

    public static final String PLATFORM_COMPONENTS_CONFIG_LOCATION_NAME = "magnolia.platform.components.config.location";
    public static final String DEFAULT_PLATFORM_COMPONENTS_CONFIG_LOCATION = "/info/magnolia/init/platform-components.xml";

    private static final Logger log = LoggerFactory.getLogger(MagnoliaServletContextListener.class);

    private ServletContext servletContext;
    private GuiceComponentProvider platform;
    private GuiceComponentProvider system;
    private ModuleManager moduleManager;
    private ConfigLoader loader;

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        try {
            servletContext = sce.getServletContext();

            // Start 'platform' ComponentProvider
            GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
            builder.withConfiguration(getPlatformComponents());
            builder.inStage(Stage.PRODUCTION);
            builder.exposeGlobally();
            platform = builder.build();

            // Expose server name as a system property, so it can be used in log4j configurations
            // rootPath and webapp are not exposed since there can be different webapps running in the same jvm
            System.setProperty("server", platform.getComponent(MagnoliaInitPaths.class).getServerName());

            // Load module definitions
            moduleManager = platform.getComponent(ModuleManager.class);
            moduleManager.loadDefinitions();

            // Initialize MagnoliaConfigurationProperties
            MagnoliaConfigurationProperties configurationProperties = platform.getComponent(MagnoliaConfigurationProperties.class);
            configurationProperties.init();

            // Connect legacy properties to the MagnoliaConfigurationProperties object
            SystemProperty.setMagnoliaConfigurationProperties(configurationProperties);

            // Initialize logging now that properties are available
            Log4jConfigurer.initLogging();

            // Start 'system' ComponentProvider
            builder = new GuiceComponentProviderBuilder();
            builder.withConfiguration(getSystemComponents());
            builder.withParent(platform);
            builder.exposeGlobally();
            system = builder.build();

            // Delegate to ConfigLoader to complete initialization
            loader = system.getComponent(ConfigLoader.class);
            startServer();

        } catch (Throwable t) {
            log.error("Oops, Magnolia could not be started", t);
            t.printStackTrace();
            if (t instanceof Error) {
                throw (Error) t;
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {

        // avoid disturbing NPEs if the context has never been started (classpath problems, etc)
        if (moduleManager != null) {
            moduleManager.stopModules();
        }

        stopServer();

        // We set the global ComponentProvider to its parent here, then we destroy it, components in it that expects the
        // global ComponentProvider to be the one it lives in and the one that was there when the component was created
        // might fail because of this. Maybe we can solve it by using the ThreadLocal override we already have and call
        // scopes.

        if (system != null) {
            Components.setProvider(system.getParent());
            system.destroy();
        }

        if (platform != null) {
            Components.setProvider(platform.getParent());
            platform.destroy();
        }

        Log4jConfigurer.shutdownLogging();
    }

    protected ComponentProviderConfiguration getPlatformComponents() {
        ComponentConfigurationBuilder configurationBuilder = new ComponentConfigurationBuilder();
        List<String> resources = getPlatformComponentsResources();
        ComponentProviderConfiguration platformComponents = configurationBuilder.readConfiguration(resources);
        platformComponents.registerInstance(ServletContext.class, servletContext);
        // This is needed by DefaultMagnoliaInitPaths for backwards compatibility
        platformComponents.registerInstance(MagnoliaServletContextListener.class, this);
        return platformComponents;
    }

    /**
     * Returns a list of resources that contain platform components. Definitions for the same type will override giving
     * preference to the last read definition. Checks for an init parameter in web.xml for an overridden location
     * Subclasses can override this method to provide alternative strategies. The returned locations are used to find
     * the resource on the class path.
     */
    protected List<String> getPlatformComponentsResources() {
        String configLocation = servletContext.getInitParameter(PLATFORM_COMPONENTS_CONFIG_LOCATION_NAME);
        if (StringUtils.isNotBlank(configLocation)) {
            return Arrays.asList(StringUtils.split(configLocation, ", \n"));
        }
        return Collections.singletonList(DEFAULT_PLATFORM_COMPONENTS_CONFIG_LOCATION);
    }

    protected ComponentProviderConfiguration getSystemComponents() {
        ComponentConfigurationBuilder configurationBuilder = new ComponentConfigurationBuilder();
        return configurationBuilder.getComponentsFromModules(platform.getComponent(ModuleRegistry.class), "system");
    }

    protected void startServer() {
        MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
            @Override
            public void doExec() {
                loader.load();
            }
        }, true);
    }

    protected void stopServer() {
        if (loader != null) {
            MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
                @Override
                public void doExec() {
                    loader.unload();
                }
            }, true);
        }
    }

    /**
     * @deprecated since 4.5, use or subclass {@link MagnoliaInitPaths}.
     */
    protected String initWebappName(String rootPath) {
        return null;
    }

    /**
     * @deprecated since 4.5, use or subclass {@link MagnoliaInitPaths}.
     */
    protected String initRootPath(final ServletContext context) {
        return null;
    }

    /**
     * @deprecated since 4.5, use or subclass {@link MagnoliaInitPaths}.
     */
    protected String initServername(boolean unqualified) {
        return null;
    }

}
