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

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.VersionConfig;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.license.LicenseFileExtractor;
import info.magnolia.cms.util.WorkspaceAccessUtil;
import info.magnolia.context.SystemContext;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.configuration.ComponentFactoryConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ConfiguredComponentConfiguration;
import info.magnolia.objectfactory.configuration.ImplementationConfiguration;
import info.magnolia.objectfactory.configuration.InstanceConfiguration;


/**
 * Guice configuration module that uses a ComponentProviderConfiguration to create and bind a ComponentProvider.
 *
 * @version $Id$
 */
public class GuiceComponentProviderModule extends AbstractModule {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private GuiceComponentProvider parentComponentProvider;
    private ComponentProviderConfiguration configuration;
    private boolean exposeGlobally = false;

    public GuiceComponentProviderModule(ComponentProviderConfiguration configuration, boolean exposeGlobally, GuiceComponentProvider parentComponentProvider) {
        this.parentComponentProvider = parentComponentProvider;
        this.configuration = configuration;
        this.exposeGlobally = exposeGlobally;
    }

    @Override
    protected void configure() {

        GuiceComponentProvider componentProvider = new GuiceComponentProvider(null, configuration, parentComponentProvider);
        if (exposeGlobally) {
            Components.setProvider(componentProvider);
        }
        bind(ComponentProvider.class).toInstance(componentProvider);

        for (ImplementationConfiguration config : configuration.getImplementations()) {
            if (config.getImplementation() == null && config.getType() == null) {
                // do not fail when configuration is bogged, try to get server running anyway
                log.error("Can't instantiate factory, implementation and type are not set: " + config);
                continue;
            }
            registerImplementation(config.getType(), config.getImplementation());
        }
        for (InstanceConfiguration config : configuration.getInstances()) {
            registerInstance(config.getType(), config.getInstance());
        }
        for (ComponentFactoryConfiguration config : configuration.getFactories()) {
            ComponentFactory<?> factory;
            try {
                factory = (ComponentFactory<?>) config.getFactoryClass().newInstance();
                registerComponentFactory(config.getType(), factory);
            } catch (Exception e) {
                throw new RuntimeException("Can't instantiate factory: " + config.getFactoryClass().getName(), e);
            }
        }
        for (ConfiguredComponentConfiguration config : configuration.getConfigured()) {
            if (config.getType() == null) {
                log.error("Type definitions contain invalid configuration. " + config.getWorkspace() + ":" + config.getPath() + (config.isObserved() ? " [observed]" : ""));
                continue;
            }
            registerConfiguredComponent(config.getType(), config.getWorkspace(), config.getPath(), config.isObserved());
        }
    }

    private <T> void registerConfiguredComponent(Class<T> type, String workspace, String path, boolean observed) {
        if (observed) {
            bind(type).toProvider(new GuiceObservedComponentProvider<T>(workspace, path, type));
        } else {
            bind(type).toProvider(new GuiceConfiguredComponentProvider(path, workspace));
        }
    }

    private void registerComponentFactory(Class type, ComponentFactory<?> factory) {
        bind(type).toProvider(new GuiceComponentFactoryProviderAdapter(factory));
    }

    private void registerInstance(Class type, Object instance) {
        if (instance instanceof ComponentFactory) {
            bind(type).toProvider(new GuiceComponentFactoryProviderAdapter((ComponentFactory) instance));
        } else {
            bind(type).toInstance(instance);
        }
    }

    private void registerImplementation(Class type, Class implementation) {

        // FIXME this is hard coded because these are the components configured in mgnl-beans.properties and in module descriptors that are now part of the startup and no longer configurable

        if (type.equals(MessagesManager.class)) {
            return;
        }
        if (type.equals(SystemContext.class)) {
            return;
        }
        if (type.equals(info.magnolia.cms.util.UnicodeNormalizer.Normalizer.class)) {
            return;
        }
        if (type.equals(SystemContext.class)) {
            return;
        }
        if (type.equals(VersionConfig.class)) {
            return;
        }
        if (type.equals(WorkspaceAccessUtil.class)) {
            return;
        }
        if (type.equals(ConfigLoader.class)) {
            return;
        }
        if (type.equals(LicenseFileExtractor.class)) {
            return;
        }
        if (ComponentFactory.class.isAssignableFrom(implementation)) {
            bind(type).toProvider(new GuiceComponentFactoryProviderAdapter((Class<ComponentFactory<?>>) implementation));
        } else {

            // FIXME for now we keep only singletons in Guice, short living objects can be created using newInstance

            if (type.equals(implementation)) {
                if (implementation.isAnnotationPresent(Singleton.class)) {
                    bind(implementation);
                }
            } else {
                if (implementation.isAnnotationPresent(Singleton.class)) {
                    bind(type).to(implementation);
                }
            }
        }
    }
}
