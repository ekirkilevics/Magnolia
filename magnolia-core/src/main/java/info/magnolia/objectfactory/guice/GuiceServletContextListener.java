/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.objectfactory.guice;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.inject.Stage;
import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.VersionConfig;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.filters.FilterManager;
import info.magnolia.cms.filters.FilterManagerImpl;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.license.LicenseFileExtractor;
import info.magnolia.cms.util.WorkspaceAccessUtil;
import info.magnolia.context.ContextFactory;
import info.magnolia.context.JCRSessionPerThreadSystemContext;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.init.DefaultMagnoliaConfigurationProperties;
import info.magnolia.init.DefaultMagnoliaInitPaths;
import info.magnolia.init.DefaultMagnoliaPropertiesResolver;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.init.MagnoliaInitPaths;
import info.magnolia.init.MagnoliaPropertiesResolver;
import info.magnolia.init.MagnoliaServletContextListener;
import info.magnolia.logging.Log4jConfigurer;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.reader.DependencyChecker;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;


/**
 * ServletContextListener that configures and starts Magnolia using Guice.
 *
 * @version $Id$
 */
public class GuiceServletContextListener implements ServletContextListener {

    private ServletContext servletContext;
    private GuiceComponentProvider platform;
    private GuiceComponentProvider system;
    private ModuleManager moduleManager;
    private ConfigLoader loader;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        servletContext = sce.getServletContext();

        try {
            GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
            builder.withConfiguration(getPlatformConfiguration());
            builder.inStage(Stage.PRODUCTION);
            builder.exposeGlobally();
            platform = builder.build();

            System.setProperty("server", platform.getComponent(MagnoliaInitPaths.class).getServerName());
            moduleManager = platform.getComponent(ModuleManager.class);
            moduleManager.loadDefinitions();
            final MagnoliaConfigurationProperties configurationProperties = platform.getComponent(MagnoliaConfigurationProperties.class);
            ((DefaultMagnoliaConfigurationProperties) configurationProperties).init();
            SystemProperty.setMagnoliaConfigurationProperties(configurationProperties);

            builder = new GuiceComponentProviderBuilder();
            builder.withConfiguration(getSystemConfiguration());
            builder.withParent(platform);
            builder.exposeGlobally();
            system = builder.build();

            system.getComponent(Log4jConfigurer.class).start();

            loader = system.getComponent(ConfigLoader.class);

            startServer();

        } catch (Throwable e) {
            e.printStackTrace();
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
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
    }

    protected void startServer() {
        MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
            @Override
            public void doExec() {
                loader.load();
            }
        }, true);
    }

    private void stopServer() {
        if (loader != null) {
            MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
                @Override
                public void doExec() {
                    loader.unload(servletContext);
                }
            }, true);
        }
    }

    public ComponentProviderConfiguration getPlatformConfiguration() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.registerInstance(ServletContext.class, servletContext);
        configuration.registerImplementation(InstallContextImpl.class);
        configuration.registerImplementation(ModuleManager.class, GuiceModuleManager.class);
        configuration.registerImplementation(ModuleRegistry.class, info.magnolia.module.ModuleRegistryImpl.class);
        configuration.registerImplementation(ModuleDefinitionReader.class, info.magnolia.module.model.reader.BetwixtModuleDefinitionReader.class);
        configuration.registerImplementation(DependencyChecker.class, info.magnolia.module.model.reader.DependencyCheckerImpl.class);
        configuration.registerImplementation(MagnoliaInitPaths.class, DefaultMagnoliaInitPaths.class);
        configuration.registerImplementation(MagnoliaConfigurationProperties.class, DefaultMagnoliaConfigurationProperties.class);
        configuration.registerImplementation(MagnoliaPropertiesResolver.class, DefaultMagnoliaPropertiesResolver.class);
        configuration.registerImplementation(ContextFactory.class);
        configuration.registerImplementation(MagnoliaServletContextListener.class); // This is needed by DefaultMagnoliaInitPaths
        return configuration;
    }

    private ComponentProviderConfiguration getSystemConfiguration() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();

        configuration.registerImplementation(Log4jConfigurer.class);

        // we'll register the whole c2b she-bang here for now
        configuration.registerImplementation(info.magnolia.content2bean.Content2BeanProcessor.class, info.magnolia.content2bean.impl.Content2BeanProcessorImpl.class);
        configuration.registerImplementation(info.magnolia.content2bean.Content2BeanTransformer.class, info.magnolia.content2bean.impl.Content2BeanTransformerImpl.class);
        configuration.registerImplementation(info.magnolia.content2bean.TransformationState.class, info.magnolia.content2bean.impl.TransformationStateImpl.class);
        configuration.registerImplementation(info.magnolia.content2bean.TypeMapping.class, info.magnolia.content2bean.impl.TypeMappingImpl.class);

        configuration.registerImplementation(MessagesManager.class, DefaultMessagesManager.class);
        configuration.registerImplementation(LicenseFileExtractor.class);
        configuration.registerImplementation(VersionConfig.class);

        configuration.registerImplementation(SystemContext.class, JCRSessionPerThreadSystemContext.class);
        configuration.registerImplementation(WorkspaceAccessUtil.class);
        configuration.registerImplementation(ConfigLoader.class);

        configuration.registerImplementation(info.magnolia.cms.util.UnicodeNormalizer.Normalizer.class, info.magnolia.cms.util.UnicodeNormalizer.AutoDetectNormalizer.class);

        configuration.registerImplementation(FilterManager.class, FilterManagerImpl.class);

        return configuration;
    }
}
