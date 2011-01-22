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
package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.PropertiesInitializer;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.DeprecationUtil;
import info.magnolia.context.ContextFactory;
import info.magnolia.context.MgnlContext;
import info.magnolia.logging.Log4jConfigurer;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.reader.DependencyChecker;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.ComponentConfigurationPath;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.DefaultComponentProvider;
import info.magnolia.objectfactory.pico.ComponentFactoryProviderAdapter;
import info.magnolia.objectfactory.pico.ObservedComponentAdapter;
import info.magnolia.objectfactory.pico.PicoComponentProvider;
import info.magnolia.objectfactory.pico.PicoLifecycleStrategy;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.gems.monitors.Slf4jComponentMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;


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
public class MgnlServletContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(MgnlServletContextListener.class);

    /**
     * Context parameter name.
     */
    public static final String MAGNOLIA_INITIALIZATION_FILE = "magnolia.initialization.file";

    /**
     * Context parameter name. If set to true in web.xml the server name resolved by magnolia will never contain the
     * domain (the server "server.domain.com" will be simply resolved as "server").
     */
    public static final String MAGNOLIA_UNQUALIFIED_SERVER_NAME = "magnolia.unqualified.server.name";

    private ConfigLoader loader;

    public void contextDestroyed(final ServletContextEvent sce) {

        // TODO : pico.stop() instead !!

        // avoid disturbing NPEs if the context has never been started (classpath problems, etc)
        ModuleManager mm = ModuleManager.Factory.getInstance();
        if (mm != null) {
            mm.stopModules();
        }

        if (loader != null) {
            MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
                public void doExec() {
                    loader.unload(sce.getServletContext());
                }
            }, true);
        }

        Log4jConfigurer.shutdownLogging();
    }

    /**
     * TODO : javadoc
     */
    public void contextInitialized(final ServletContextEvent sce) {
        try {
            final ServletContext context = sce.getServletContext();

            // the container used for startup - should not be referenced further
            // TODO : maybe we don't even need to store the root container ?
            final MutablePicoContainer root = makeContainer(null, "Magnolia-Root");
            populateRootContainer(root, context);
            storeRootContainer(context, root);

            // TODO - currently unused : installContainer
            // final MutablePicoContainer installContainer = makeContainer(root);
            // storeInstallContainer(context, installContainer);

            // TODO : extract ApplicationPaths, register it in the root container - then use it where needed as a regular dependency
            final ApplicationPaths appPaths = determineApplicationPaths(context);

            // TODO: these were previously set in the various determine* methods (which in turn were previously called init*).
            // TODO: -> find usage point, and replace by a dependency on ApplicationPaths
            SystemProperty.setProperty(SystemProperty.MAGNOLIA_WEBAPP, appPaths.getWebappFolderName());
            SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, appPaths.getRootPath());
            SystemProperty.setProperty(SystemProperty.MAGNOLIA_SERVERNAME, appPaths.getServerName());

            final String propertiesFilesString = getPropertiesFilesString(context, appPaths);

            // Get ModuleManager and initialize properties from the root container
            final ModuleManager moduleManager = root.getComponent(ModuleManager.class);
            moduleManager.loadDefinitions();

            // We need the properties to setup the container, i.e before we fire up ConfigLoader.
            // TODO - perhaps ConfigLoader could depend on PropertiesInitializer, to be checked.
            final PropertiesInitializer propertiesInitializer = root.getComponent(PropertiesInitializer.class);
            propertiesInitializer.loadAllProperties(propertiesFilesString, appPaths.getRootPath());

            // TODO - isn't the below completely bogus, since we already replace tokens when loading the log4j file ?
            // expose server name as a system property, so it can be used in log4j configurations
            // rootPath and webapp are not exposed since there can be different webapps running in the same jvm
            System.setProperty("server", appPaths.getServerName());

            final MutablePicoContainer mainContainer = makeContainer(root, "Magnolia-Main");
            // TODO extract population to a ContainerComposer interface - and get the composers out of pico ? (ordering problem? how about request-scope?)
            // TODO - extract container composers (one for properties, one for modules, etc)
            populateMainContainer(mainContainer, root.getComponent(ModuleRegistry.class), SystemProperty.getProperties());
            storeMainContainer(context, mainContainer);

            // Finally, we fire up the container! Root container will start its components, then start its children containers too.
            root.start();


            // TODO : de-uglify this ? Also: get rid of DefaultComponentProvider here.
            Components.setProvider(new PicoComponentProvider(mainContainer, new DefaultComponentProvider(SystemProperty.getProperties())));
            // TODO - perhaps PicoComponentProvider can be constructed by pico itself

            // Start from the main container (ConfigLoader needs LicenceFileExtractor for example, whose impl is set via a property)
            this.loader = mainContainer.getComponent(ConfigLoader.class); // new ConfigLoader(context);
            startServer();
        } catch (Throwable t) {
            log.error("Oops, Magnolia could not be started", t);
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t);
            }
        }
    }

    protected void populateRootContainer(MutablePicoContainer pico, ServletContext context) {
        // it doesn't matter whether we register an implementation with or without a key - but the lookup is slightly faster if we register it using the class it's going to be looked up by as its key
        pico.addComponent(ServletContext.class, context);
        pico.addComponent(InstallContextImpl.class); // this is looked up as InstallContextImpl, at least by ModuleManagerImpl, let's see...
        pico.addComponent(ModuleManager.class, info.magnolia.module.ModuleManagerImpl.class);
        pico.addComponent(ModuleRegistry.class, info.magnolia.module.ModuleRegistryImpl.class);
        pico.addComponent(ModuleDefinitionReader.class, info.magnolia.module.model.reader.BetwixtModuleDefinitionReader.class);
        pico.addComponent(DependencyChecker.class, info.magnolia.module.model.reader.DependencyCheckerImpl.class);
        pico.addComponent(PropertiesInitializer.class);
        // set via a property in the main container pico.addComponent(ConfigLoader.class);

        pico.addComponent(ContextFactory.class);

        pico.addComponent(Log4jConfigurer.class);

        // we'll register the whole c2b she-bang here for now
        pico.addComponent(info.magnolia.content2bean.Content2BeanProcessor.class, info.magnolia.content2bean.impl.Content2BeanProcessorImpl.class);
        pico.addComponent(info.magnolia.content2bean.Content2BeanTransformer.class, info.magnolia.content2bean.impl.Content2BeanTransformerImpl.class);
        pico.addComponent(info.magnolia.content2bean.TransformationState.class, info.magnolia.content2bean.impl.TransformationStateImpl.class);
        pico.addComponent(info.magnolia.content2bean.TypeMapping.class, info.magnolia.content2bean.impl.TypeMappingImpl.class);
    }

    protected void populateMainContainer(MutablePicoContainer pico, ModuleRegistry moduleRegistry, Properties mappings) {
        // Ideally, the dependency should be on SystemProperty or other relevant object, instead of this java.util.Properties instance
        // Hopefully, we'll de-staticize SystemProperty soon.

        // TODO : we have a dependency on ClassFactory, but we can't inject it here,
        // since it might get swapped later

        for (String moduleName : moduleRegistry.getModuleNames()) {
            final ModuleDefinition module = moduleRegistry.getDefinition(moduleName);
            if (module.getClassName() != null) {
                pico.addComponent(classForName(module.getClassName()));
            }
        }

        for (Map.Entry<Object, Object> e : mappings.entrySet()) {
            String key = (String) e.getKey();
            String value = (String) e.getValue();
            final Class<Object> keyType = classForName(key);
            if (keyType == null) {
                log.debug("{} does not seem to resolve to a class. (property value: {})", key, value);
                continue;
            }
            if (ComponentConfigurationPath.isComponentConfigurationPath(value)) {
                final ComponentConfigurationPath path = new ComponentConfigurationPath(value);
                // TODO - wrap in a caching adapter if that's not already the case ??
                pico.addAdapter(new ObservedComponentAdapter(path, keyType, keyType));
            } else {
                final Class<?> valueType = classForName(value);
                if (valueType == null) {
                    log.debug("{} does not seem to resolve a class or a configuration path. (property key: {})",  value, key);
                } else {
                    if (ComponentFactory.class.isAssignableFrom(valueType)) {
                        // TODO - maybe use a FactoryInjector instead ? not clear. Also not sure if we want factories to be IoC'd.
                        pico.addAdapter(new ComponentFactoryProviderAdapter(keyType, (Class<ComponentFactory>) valueType));
                    } else {
                        pico.addComponent(keyType, valueType);
                    }
                }
            }
        }
    }

    // TODO use ClassloadingPicoContainer instead ?
    private Class<Object> classForName(String value) {
        try {
            return Classes.getClassFactory().forName(value);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Our LifecycleStrategy is by default lazy, i.e components are not started before they're needed.
     * Changing this now would mean all known components get started at startup before the repo is loaded.
     * We could have an annotation to drive this behaviour, see {@link LifecycleStrategy#isLazy(org.picocontainer.ComponentAdapter)}
     *

     * @param parent
     * @return
     */
    protected MutablePicoContainer makeContainer(PicoContainer parent, String name) {
        final ComponentMonitor componentMonitor = makeComponentMonitor();
        final LifecycleStrategy lifecycleStrategy = makeLifecycleStrategy(componentMonitor);

        // TODO : perhaps we'll need to use DefaultClassLoadingPicoContainer - able to load classes based on names
        //      : and/or somehow hookup a info.magnolia.objectfactory.ClassFactory, so we can use groovy components !
        //      : DefaultClassLoadingPicoContainer.loadClass() would need to be protected - or we use ClassLoadingPicoContainer.getComponentClassLoader instead
        final PicoBuilder picoBuilder = new PicoBuilder(parent)
                // order of injection matters, so ConstructorInjection must be first. Yes, we could add more types of injection if needed.
                .withConstructorInjection()
                .withAnnotatedFieldInjection() // TODO: once PICO-380 is fixed, we can use javax.inject.@Inject instead of org.picocontainer.annotations.@Inject
                .withCaching()
                .withMonitor(componentMonitor)
                .withLifecycle(lifecycleStrategy);
        if (parent != null) {
            picoBuilder.addChildToParent(); // we add child containers to their parent so that lifecycle is propagated downwards. This also allows visiting, and propagation of monitor changes.
        }
        final MutablePicoContainer pico = picoBuilder.build();
        pico.setName(name);
        return pico;
    }

    protected Slf4jComponentMonitor makeComponentMonitor() {
        // TODO - different monitor(s) ? LifecycleComponentMonitor might be interesting to "summarize" all failures ?
        // (the PrefuseDependencyGraph might be interesting too)
        return new Slf4jComponentMonitor(LoggerFactory.getLogger(Slf4jComponentMonitor.class));
    }

    protected PicoLifecycleStrategy makeLifecycleStrategy(ComponentMonitor componentMonitor) {
        return new PicoLifecycleStrategy(componentMonitor);
    }

    protected void storeRootContainer(ServletContext context, PicoContainer pico) {
        context.setAttribute("pico-root", pico);
    }

    protected void storeMainContainer(ServletContext context, PicoContainer pico) {
        context.setAttribute("pico-main", pico);
    }

    protected void storeInstallContainer(ServletContext context, PicoContainer pico) {
        context.setAttribute("pico-install", pico);
    }

    /**
     * @deprecated since 5.0, use {@link #startServer()}, dependencies are injected.
     */
    protected void startServer(final ServletContext context) {
        startServer();
    }

    protected void startServer() {
        MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
            public void doExec() {
                loader.load();
            }
        }, true);
    }

    /**
     * @deprecated since 5.0, use {@link #getPropertiesFilesString(javax.servlet.ServletContext, info.magnolia.cms.servlets.MgnlServletContextListener.ApplicationPaths)}
     */
    protected String getPropertiesFilesString(ServletContext context, String servername, String webapp) {
        DeprecationUtil.isDeprecated();
        return getPropertiesFilesString(context, new ApplicationPaths(servername, null, webapp, null));
    }

    protected String getPropertiesFilesString(ServletContext context, ApplicationPaths appPaths) {
        String propertiesFilesString = context.getInitParameter(MAGNOLIA_INITIALIZATION_FILE);
        if (StringUtils.isEmpty(propertiesFilesString)) {
            log.debug("{} value in web.xml is undefined, falling back to default: {}", MgnlServletContextListener.MAGNOLIA_INITIALIZATION_FILE, PropertiesInitializer.DEFAULT_INITIALIZATION_PARAMETER);
            propertiesFilesString = PropertiesInitializer.DEFAULT_INITIALIZATION_PARAMETER;
        } else {
            log.debug("{} value in web.xml is :'{}'", MgnlServletContextListener.MAGNOLIA_INITIALIZATION_FILE, propertiesFilesString);
        }
        // TODO refactor PropertiesInitializer. It could simply depend on ApplicationPaths.
        return PropertiesInitializer.processPropertyFilesString(context, appPaths.getServerName(), appPaths.getWebappFolderName(), propertiesFilesString);
    }

    /**
     * Override this method if you need to provide different startup/configuration paths.
     * One can even think about return a subclass with more properties if needed.
     */
    protected ApplicationPaths determineApplicationPaths(ServletContext context) {
        final String servername = determineServerName(context);
        final String rootPath = determineRootPath(context);
        final String webapp = determineWebappFolderName(rootPath, context);
        final String contextPath = determineContextPath(context);
        log.debug("servername is {}, rootPath is {}, webapp is {}, contextPath is {}", new Object[]{servername, rootPath, webapp, contextPath});
        return new ApplicationPaths(servername, rootPath, webapp, contextPath);
    }

    /**
     * Figures out the local host name, makes sure it's lowercased, and use its unqualified name if the {@value #MAGNOLIA_UNQUALIFIED_SERVER_NAME} init parameter is set to true.
     */
    protected String determineServerName(ServletContext context) {
        final boolean unqualifiedServerName = BooleanUtils.toBoolean(context.getInitParameter(MAGNOLIA_UNQUALIFIED_SERVER_NAME));
        final String retroCompatMethodCall = initServername(unqualifiedServerName);
        if (retroCompatMethodCall != null) {
            DeprecationUtil.isDeprecated("You should update your code and override determineServerName(ServletContext) instead of initServername(String)");
            return retroCompatMethodCall;
        }

        try {
            String serverName = StringUtils.lowerCase(InetAddress.getLocalHost().getHostName());

            if (unqualifiedServerName && StringUtils.contains(serverName, ".")) {
                serverName = StringUtils.substringBefore(serverName, ".");
            }
            return serverName;
        } catch (UnknownHostException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * Figures out the root path where the webapp is deployed.
     */
    protected String determineRootPath(ServletContext context) {
        final String retroCompatMethodCall = initRootPath(context);
        if (retroCompatMethodCall != null) {
            DeprecationUtil.isDeprecated("You should update your code and override determineRootPath(ServletContext) instead of initRootPath(ServletContext)");
            return retroCompatMethodCall;
        }

        String realPath = StringUtils.replace(context.getRealPath(StringUtils.EMPTY), "\\", "/");
        realPath = StringUtils.removeEnd(realPath, "/");
        if (realPath == null) {
            // don't use new java.io.File("x").getParentFile().getAbsolutePath() to find out real directory, could throw
            // a NPE for unexpanded war
            throw new RuntimeException("Magnolia is not configured properly and therefore unable to start: real path can't be obtained [ctx real path:" + context.getRealPath(StringUtils.EMPTY) + "]. Please refer to the Magnolia documentation for installation instructions specific to your environment.");
        }
        return realPath;
    }

    protected String determineWebappFolderName(String determinedRootPath, ServletContext context) {
        final String retroCompatMethodCall = initWebappName(determinedRootPath);
        if (retroCompatMethodCall != null) {
            DeprecationUtil.isDeprecated("You should update your code and override determineWebappFolderName(String, ServletContext) instead of initWebappName(String)");
            return retroCompatMethodCall;
        }

        return StringUtils.substringAfterLast(determinedRootPath, "/");
    }

    protected String determineContextPath(ServletContext context) {
        // Getting the contextPath via reflection, until we can depend on servlet 2.5 : See MAGNOLIA-3094
        try {
            final Method getContextPath = context.getClass().getMethod("getContextPath", null);
            return (String) getContextPath.invoke(context);
        } catch (NoSuchMethodException e) {
            log.info("Magnolia appears to be running on a server using a Servlet API version older than 2.5, so we can not know the contextPath at startup.");
            return null;
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @deprecated since 5.0, the method is {@link #determineServerName(javax.servlet.ServletContext)}.
     */
    protected String initServername(boolean unqualified) {
        return null;
    }

    /**
     * @deprecated since 5.0, the method is {@link #determineServerName(javax.servlet.ServletContext)}.
     */
    protected String initRootPath(final ServletContext context) {
        return null;
    }

    /**
     * @deprecated since 5.0, the method is {@link #determineWebappFolderName(String, javax.servlet.ServletContext)}.
     */
    protected String initWebappName(String rootPath) {
        return null;
    }

    public class ApplicationPaths {
        private final String serverName;
        private final String rootPath;
        private final String webappFolderName;
        private final String contextPath;

        public ApplicationPaths(String serverName, String rootPath, String webapp, String contextPath) {
            this.serverName = serverName;
            this.rootPath = rootPath;
            this.webappFolderName = webapp;
            this.contextPath = contextPath;
        }

        public String getServerName() {
            return serverName;
        }

        public String getRootPath() {
            return rootPath;
        }

        public String getWebappFolderName() {
            return webappFolderName;
        }

        public String getContextPath() {
            return contextPath;
        }

    }
}
