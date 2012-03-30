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
import info.magnolia.objectfactory.configuration.ComponentProviderConfigurationBuilder;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;

/**
 * Point of entry for Magnolia CMS, initializes the component providers, starts logging, triggers loading of
 * properties and finally delegates to {@link ConfigLoader} for completing initialization.
 *
 * <h3>Component providers</h3>
 * <p>
 * When Magnolia starts up the first thing that happens is the creation of the <i>platform</i> component provider. It
 * contains the essential singletons that constitutes the platform on which the rest of the system builds. These
 * components are defined in a file called <code>platform-components.xml</code>, it's on the classpath in package
 * /info/magnolia/init.
 * </p>
 * <p>
 * The location can be customized using a servlet context parameter called
 * <code>magnolia.platform.components.config.location</code>. It's specified as a list of comma-separated files on the
 * classpath. The files are loaded in the specified order allowing definitions to override definitions from earlier
 * files.
 * </p>
 * <pre>
 * &lt;context-param&gt;
 *   &lt;param-name&gt;magnolia.platform.components.config.location&lt;/param-name&gt;
 *   &lt;param-value&gt;/info/magnolia/init/platform-components.xml,/com/mycompany/custom-platform-components.xml&lt;/param-value&gt;
 * &lt;/context-param&gt;
 * </pre>
 * <p>
 * The platform components include the {@link ModuleManager} which is called by this listener to load the descriptors of
 * all the modules present. Modules define additional components that are loaded into a second component provider called
 * <i>system</i>.
 * </p>
 * <p>
 * When {@link ConfigLoader} takes over the initialization procedure it will create a third component provider called
 * <i>main</i> which contain components defined in modules as belonging to the main component provider.
 * </p>
 * <h3>Property loading</h3>
 * <p>
 * Properties are loaded by an implementation of {@link MagnoliaConfigurationProperties}. It's configured as a platform
 * component and is called by this class to do initialization. See {@link DefaultMagnoliaPropertiesResolver} and
 * {@link DefaultMagnoliaInitPaths} for details on how to customize the default behavior.
 * </p>
 *
 * @version $Id$
 * @see ModuleManager
 * @see MagnoliaInitPaths
 * @see MagnoliaPropertiesResolver
 * @see DefaultMagnoliaPropertiesResolver
 * @see DefaultMagnoliaConfigurationProperties
 * @see DefaultMagnoliaInitPaths
 * @see ConfigLoader
 * @see Log4jConfigurer
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
        contextInitialized(sce, true);
    }

    public void contextInitialized(final ServletContextEvent sce, boolean startServer) {
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
            log.info("Property sources loaded: {}", configurationProperties.describe());

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
            if (startServer) {
                startServer();
            }

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
            Components.setComponentProvider(system.getParent());
            system.destroy();
        }

        if (platform != null) {
            Components.setComponentProvider(platform.getParent());
            platform.destroy();
        }

        Log4jConfigurer.shutdownLogging();
    }

    protected ComponentProviderConfiguration getPlatformComponents() {
        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        List<String> resources = getPlatformComponentsResources();
        ComponentProviderConfiguration platformComponents = configurationBuilder.readConfiguration(resources, "platform");
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
        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        return configurationBuilder.getComponentsFromModules("system", platform.getComponent(ModuleRegistry.class).getModuleDefinitions());
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
