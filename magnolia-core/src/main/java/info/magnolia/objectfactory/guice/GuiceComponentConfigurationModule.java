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

import java.lang.annotation.Annotation;
import java.util.Map;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.util.Providers;
import info.magnolia.module.model.ComponentDefinition;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.annotation.LazySingleton;
import info.magnolia.objectfactory.annotation.RequestScoped;
import info.magnolia.objectfactory.annotation.SessionScoped;
import info.magnolia.objectfactory.configuration.ComponentConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ConfiguredComponentConfiguration;
import info.magnolia.objectfactory.configuration.ImplementationConfiguration;
import info.magnolia.objectfactory.configuration.InstanceConfiguration;
import info.magnolia.objectfactory.configuration.ProviderConfiguration;


/**
 * Guice configuration module that adds bindings based on a ComponentProviderConfiguration.
 *
 * @version $Id$
 */
public class GuiceComponentConfigurationModule extends AbstractModule {

    private ComponentProviderConfiguration configuration;

    public GuiceComponentConfigurationModule(ComponentProviderConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {

        for (Map.Entry<Class, ComponentConfiguration> entry : configuration.getComponents().entrySet()) {
            bindConfiguration(entry.getValue());
        }

        for (Object configurer : configuration.getConfigurers()) {
            if (configurer instanceof Module) {
                install((Module) configurer);
            }
        }
    }

    private <T> void bindConfiguration(ComponentConfiguration<T> configuration) {
        if (configuration instanceof ImplementationConfiguration) {
            bindImplementation((ImplementationConfiguration<T>) configuration);
        } else if (configuration instanceof InstanceConfiguration) {
            bindInstance((InstanceConfiguration<T>) configuration);
        } else if (configuration instanceof ProviderConfiguration) {
            bindProvider((ProviderConfiguration<T>) configuration);
        } else if (configuration instanceof ConfiguredComponentConfiguration) {
            ConfiguredComponentConfiguration<T> configured = (ConfiguredComponentConfiguration<T>) configuration;
            if (configured.isObserved()) {
                bindObservedComponent(configured);
            } else {
                bindConfiguredComponent(configured);
            }
        } else {
            throw new IllegalStateException("Component configuration is ambiguous for component with type [" + configuration.getType() + "]");
        }
    }

    private <T> void bindConfiguredComponent(ConfiguredComponentConfiguration<T> configuration) {
        Provider<T> provider = new GuiceConfiguredComponentProvider<T>(configuration.getWorkspace(), configuration.getPath());
        ScopedBindingBuilder builder = bindProvider(configuration.getType(), provider);
        bindInScope(builder, configuration);
    }

    private <T> void bindObservedComponent(ConfiguredComponentConfiguration<T> configuration) {
        Class<T> key = configuration.getType();
        Provider<T> provider = new GuiceObservedComponentProvider<T>(configuration.getWorkspace(), configuration.getPath(), key);
        ScopedBindingBuilder builder = bindProvider(configuration.getType(), provider);
        bindInScope(builder, configuration);
    }

    private <T> void bindProvider(ProviderConfiguration<T> configuration) {
        Class<?> factoryClass = configuration.getProviderClass();

        if (ComponentFactory.class.isAssignableFrom(factoryClass)) {
            Provider<T> provider = GuiceUtils.providerForComponentFactory((Class<? extends ComponentFactory<T>>) factoryClass);
            ScopedBindingBuilder builder = bindProvider(configuration.getType(), provider);
            bindInScope(builder, configuration);
        } else if (Provider.class.isAssignableFrom(factoryClass)) {
            ScopedBindingBuilder builder = bindProvider(configuration.getType(), (Class<? extends Provider<T>>) factoryClass);
            bindInScope(builder, configuration);
        } else {
            throw new IllegalStateException("Unsupported provider class [" + factoryClass + "] for component with type [" + configuration.getType() + "]");
        }
    }

    private <T> void bindInstance(InstanceConfiguration<T> configuration) {
        Class<T> key = configuration.getType();
        Object instance = configuration.getInstance();
        if (instance instanceof Provider) {
            bindProvider(configuration.getType(), (Provider<T>) instance);
        } else if (instance instanceof ComponentFactory) {
            bindProvider(configuration.getType(), GuiceUtils.providerForComponentFactory((ComponentFactory<T>) instance));
        } else {
            bind(key).toInstance((T) instance);
        }
        // we don't apply any scope here since instance are natural singletons
    }

    private <T> void bindImplementation(ImplementationConfiguration<T> configuration) {
        Class<T> key = configuration.getType();
        Class<? extends T> implementation = configuration.getImplementation();

        ScopedBindingBuilder builder;
        if (key.equals(implementation)) {
            builder = bind(implementation);
        } else {
            builder = bind(key).to(implementation);
        }
        bindInScope(builder, configuration);
    }

    private <T> ScopedBindingBuilder bindProvider(Class<T> type, Provider<T> provider) {
        return bind(type).toProvider(Providers.guicify(provider));
    }

    private <T> ScopedBindingBuilder bindProvider(Class<T> type, Class<? extends Provider<T>> provider) {
        return bind(type).toProvider(provider);
    }

    private <T> void bindInScope(ScopedBindingBuilder builder, ComponentConfiguration<T> configuration) {
        Class<? extends Annotation> scopeAnnotation = getScope(configuration);
        if (scopeAnnotation != null) {
            builder.in(scopeAnnotation);
        }
    }

    private Class<? extends Annotation> getScope(ComponentConfiguration<?> componentConfiguration) {
        String scope = componentConfiguration.getScope();
        if (StringUtils.isEmpty(scope)) {
            return null;
        }
        if (ComponentDefinition.SCOPE_SINGLETON.equalsIgnoreCase(scope) && !componentConfiguration.isLazy()) {
            return Singleton.class;
        }
        if (ComponentDefinition.SCOPE_SINGLETON.equalsIgnoreCase(scope) && componentConfiguration.isLazy()) {
            return LazySingleton.class;
        }
        if (ComponentDefinition.SCOPE_REQUEST.equalsIgnoreCase(scope)) {
            return RequestScoped.class;
        }
        if (ComponentDefinition.SCOPE_SESSION.equalsIgnoreCase(scope)) {
            return SessionScoped.class;
        }
        throw new IllegalStateException("Unknown scope [" + scope + "] for component with type [" + componentConfiguration.getType() + "]");
    }
}
