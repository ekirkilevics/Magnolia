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
package info.magnolia.objectfactory.pico;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.ModuleLifecycleContextImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.ComponentConfigurationPath;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.behaviors.AbstractBehaviorFactory;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.lang.reflect.Type;
import java.util.Properties;

/**
 * A specific {@link org.picocontainer.ComponentFactory} for Magnolia module classes.
 * Manages observation of the module node as well as content2bean population.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleAdapterFactory extends AbstractBehaviorFactory {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModuleAdapterFactory.class);

    public static Properties makeModuleProperties(String moduleName) {
        final Properties p = new Properties();
        p.put(ModuleAdapterFactory.class.getName(), moduleName);
        return p;
    }

    @Override
    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy, Properties componentProperties, Object componentKey, Class<T> componentImplementation, Parameter... parameters) throws PicoCompositionException {
        final String moduleName = removeModuleProperty(componentProperties);
        final ComponentAdapter<T> delegate = super.createComponentAdapter(componentMonitor, lifecycleStrategy, componentProperties, componentKey, componentImplementation, parameters);
        if (moduleName == null) {
            return delegate;
        } else {
            return componentMonitor.newBehavior(new ModuleAdapter<T>(delegate, moduleName));
        }
    }

    @Override
    public <T> ComponentAdapter<T> addComponentAdapter(ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy, Properties componentProperties, ComponentAdapter<T> adapter) {
        final String path = removeModuleProperty(componentProperties);
        final ComponentAdapter<T> delegate = super.addComponentAdapter(componentMonitor, lifecycleStrategy, componentProperties, adapter);
        if (path == null) {
            return delegate;
        } else {
            // this doesn't seem to ever be called
            return componentMonitor.newBehavior(new ModuleAdapter<T>(delegate, path));
        }
    }

    private String removeModuleProperty(Properties componentProperties) {
        return (String) componentProperties.remove(ModuleAdapterFactory.class.getName());
    }

    // TODO - other option is only have a lifecycle - call populate() in start()

    private static class ModuleAdapter<T> extends AbstractBehavior<T> implements LifecycleStrategy {
        private static final int DEFAULT_MODULE_OBSERVATION_DELAY = 5000;
        private static final int DEFAULT_MODULE_OBSERVATION_MAX_DELAY = 30000;
        private final String moduleName;
        private final ComponentConfigurationPath path;
        private ModuleDefinition moduleDefinition;

        public ModuleAdapter(ComponentAdapter<T> delegate, String moduleName) {
            super(delegate);
            this.moduleName = moduleName;
            final String moduleNodePath = "/modules/" + moduleName + "/config"; // TODO : a more elegant solution to this ?
            this.path = new ComponentConfigurationPath(moduleNodePath);
        }

        @Override
        public T getComponentInstance(final PicoContainer container, Type into) throws PicoCompositionException {
            final T instance = super.getComponentInstance(container, into);
            if (moduleDefinition == null) {
                moduleDefinition = container.getComponent(ModuleRegistry.class).getDefinition(moduleName);
            }
            // This populates on every get() not only when new !! -- so if we make sure the caching behaviour is in front of this one, we'll only decorate once ...
            populate(instance);
            return instance;
        }

        // --- we don't want to delegate lifecycle to the delegate ! take it in our own hands !!
        @Override
        public boolean hasLifecycle(Class<?> type) {
            return true;
        }

        @Override
        public void start(final Object component) {
            startObservation((T) component);
            super.start(component);
        }

        @Override
        public void stop(Object component) {
            // TODO stop observation ?
            super.stop(component);
        }

        @Override
        public void dispose(Object component) {
            super.dispose(component);
        }

        private void startObservation(final T component) {
            ObservationUtil.registerDeferredChangeListener(path.getRepository(), path.getPath(), new EventListener() {
                @Override
                public void onEvent(EventIterator events) {
                    // TODO we should keep only one instance of the lifecycle context
                    final ModuleLifecycleContextImpl lifecycleContext = new ModuleLifecycleContextImpl();
                    lifecycleContext.setPhase(ModuleLifecycleContext.PHASE_MODULE_RESTART);
                    MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
                        @Override
                        public void doExec() {
                            stopModule(component, moduleDefinition, lifecycleContext);
                            populate(component);
                            startModule(component, moduleDefinition, lifecycleContext);
                        }
                    }, true);

                }
            }, DEFAULT_MODULE_OBSERVATION_DELAY, DEFAULT_MODULE_OBSERVATION_MAX_DELAY);
        }

        // TODO - copied from info.magnolia.module.ModuleManagerImpl#startModule
        protected void startModule(Object moduleInstance, final ModuleDefinition moduleDefinition, final ModuleLifecycleContextImpl lifecycleContext) {
            if (moduleInstance instanceof ModuleLifecycle) {
                lifecycleContext.setCurrentModuleDefinition(moduleDefinition);
                log.info("Starting module {}", moduleDefinition.getName());
                ((ModuleLifecycle) moduleInstance).start(lifecycleContext);
            }
        }

        // TODO - copied from info.magnolia.module.ModuleManagerImpl#stopModule
        protected void stopModule(Object moduleInstance, final ModuleDefinition moduleDefinition, final ModuleLifecycleContextImpl lifecycleContext) {
            if (moduleInstance instanceof ModuleLifecycle) {
                lifecycleContext.setCurrentModuleDefinition(moduleDefinition);
                log.info("Stopping module {}", moduleDefinition.getName());
                ((ModuleLifecycle) moduleInstance).stop(lifecycleContext);
            }
        }

        public void populate(T instance) {
            final HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(path.getRepository());
            if (hm.isExist(path.getPath())) {
                try {
                    final Content node = hm.getContent(path.getPath());
                    Content2BeanUtil.setProperties(instance, node, true);
                } catch (RepositoryException e) {
                    log.error("Can't read {}, can not populate {}.", new Object[]{path.getPath(), instance, e});
                } catch (Content2BeanException e) {
                    throw new RuntimeException(e); // TODO
                }
            } else {
                log.warn("{} does not exist, can not populate {}.", path.getPath(), instance);
            }
        }

        @Override
        public String getDescriptor() {
            return "Module";
        }
    }
}
