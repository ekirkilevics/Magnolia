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

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.ModuleLifecycleContextImpl;
import info.magnolia.module.ModuleManagerImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.reader.DependencyChecker;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.ComponentConfigurationPath;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfigurationBuilder;


/**
 * ModuleManager that creates a child Guice Injector in which it will load configuration from module definitions.
 *
 * @version $Id$
 */
@Singleton
public class GuiceModuleManager extends ModuleManagerImpl {

    private GuiceComponentProvider main;
    private ModuleRegistry moduleRegistry;
    private Map<String, ModuleInstanceProvider<?>> moduleProviders = new HashMap<String, ModuleInstanceProvider<?>>();

    @Inject
    public GuiceModuleManager(InstallContextImpl installContext, ModuleDefinitionReader moduleDefinitionReader, ModuleRegistry moduleRegistry, DependencyChecker dependencyChecker) {
        super(installContext, moduleDefinitionReader, moduleRegistry, dependencyChecker);
        this.moduleRegistry = moduleRegistry;
    }

    @Override
    public void startModules() {
        initialize();
        super.startModules();
    }

    protected void initialize() {
        if (main != null) {
            return;
        }

        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(getMainComponents());
        builder.withParent((GuiceComponentProvider) Components.getComponentProvider());
        builder.exposeGlobally();
        builder.addModule(new ModuleClassesModule());

        main = builder.build();
    }

    protected ComponentProviderConfiguration getMainComponents() {
        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        return configurationBuilder.getComponentsFromModules(moduleRegistry, "main");
    }

    @Override
    public void stopModules() {
        super.stopModules();
        // See comment in GuiceServletContextListener
        Components.setProvider(main.getParent());
        main.destroy();
    }

    @Override
    protected void startModule(Object moduleInstance, ModuleDefinition moduleDefinition, ModuleLifecycleContextImpl lifecycleContext) {

        super.startModule(moduleInstance, moduleDefinition, lifecycleContext);

        // Start observation now that the module has started
        moduleProviders.get(moduleDefinition.getName()).startObservation();
    }

    private Class<Object> getModuleClass(ModuleDefinition moduleDefinition) throws ClassNotFoundException {
        return Classes.getClassFactory().forName(moduleDefinition.getClassName());
    }

    /**
     * Guice configuration module that binds providers for module classes.
     *
     * @version $Id$
     */
    private class ModuleClassesModule extends AbstractModule {

        @Override
        protected void configure() {

            for (String moduleName : moduleRegistry.getModuleNames()) {
                final ModuleDefinition moduleDefinition = moduleRegistry.getDefinition(moduleName);
                if (moduleDefinition.getClassName() != null) {
                    final Class<Object> moduleClass;
                    try {
                        moduleClass = getModuleClass(moduleDefinition);
                    } catch (ClassNotFoundException e) {
                        throw new ProvisionException(e.getMessage(), e);
                    }
                    ModuleInstanceProvider provider = new ModuleInstanceProvider(moduleDefinition);
                    bind(moduleClass).toProvider(provider);
                    moduleProviders.put(moduleDefinition.getName(), provider);
                }
            }
        }
    }

    /**
     * Guice provider that creates the module class and populates it from repository configuration and updates it if
     * the configuration changes.
     *
     * @version $Id$
     */
    private class ModuleInstanceProvider<T> implements Provider<T> {

        private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModuleInstanceProvider.class);

        private static final int DEFAULT_MODULE_OBSERVATION_DELAY = 5000;
        private static final int DEFAULT_MODULE_OBSERVATION_MAX_DELAY = 30000;

        @Inject
        private Injector injector;
        @Inject
        private SystemContext systemContext;

        private ModuleDefinition moduleDefinition;
        private ComponentConfigurationPath path;
        private T instance;

        public ModuleInstanceProvider(ModuleDefinition moduleDefinition) {
            this.moduleDefinition = moduleDefinition;
            this.path = new ComponentConfigurationPath("/modules/" + moduleDefinition.getName() + "/config");
        }

        @Override
        public T get() {

            if (instance != null) {
                return instance;
            }

            Class<Object> moduleClass;
            try {
                moduleClass = getModuleClass(moduleDefinition);
            } catch (ClassNotFoundException e) {
                throw new ProvisionException(e.getMessage(), e);
            }

            ObjectManufacturer manufacturer = new ObjectManufacturer(injector);
            instance = (T) manufacturer.manufacture(moduleClass);

            populate();

            return instance;
        }

        public void startObservation() {
            ObservationUtil.registerDeferredChangeListener(path.getRepository(), path.getPath(), new EventListener() {
                @Override
                public void onEvent(EventIterator events) {
                    restartModule();
                }
            }, DEFAULT_MODULE_OBSERVATION_DELAY, DEFAULT_MODULE_OBSERVATION_MAX_DELAY);
        }

        private void restartModule() {
            // TODO we should keep only one instance of the lifecycle context
            final ModuleLifecycleContextImpl lifecycleContext = new ModuleLifecycleContextImpl();
            lifecycleContext.setPhase(ModuleLifecycleContext.PHASE_MODULE_RESTART);
            MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
                @Override
                public void doExec() {
                    stopModule(instance, moduleDefinition, lifecycleContext);
                    populate();
                    startModule(instance, moduleDefinition, lifecycleContext);
                }
            }, true);
        }

        public void populate() {
            final HierarchyManager hm = systemContext.getHierarchyManager(path.getRepository());
            if (hm.isExist(path.getPath())) {
                try {
                    final Content node = hm.getContent(path.getPath());
                    Content2BeanUtil.setProperties(instance, node, true);
                } catch (RepositoryException e) {
                    log.error("Can't read {}, can not populate {}.", new Object[]{path.getPath(), instance, e});
                } catch (Content2BeanException e) {
                    throw new RuntimeException(e); // TODO
                }
            }
        }
    }
}
