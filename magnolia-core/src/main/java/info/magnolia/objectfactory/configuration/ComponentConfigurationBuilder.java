/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.objectfactory.configuration;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;

import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.SessionScoped;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ComponentDefinition;
import info.magnolia.module.model.ComponentsDefinition;
import info.magnolia.module.model.ComposerDefinition;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.TypeMappingDefinition;
import info.magnolia.objectfactory.ComponentComposer;
import info.magnolia.objectfactory.ComponentFactory;

/**
 * Builder for creating a ComponentProviderConfiguration from components configured in module descriptors.
 *
 * @version $Id$
 */
public class ComponentConfigurationBuilder {

    // TODO add support for reading components from other sources, to be used for platform components

    public ComponentProviderConfiguration getMergedComponents(ModuleRegistry moduleRegistry, String id) {

        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();

        for (ModuleDefinition moduleDefinition : moduleRegistry.getModuleDefinitions()) {
            for (ComponentsDefinition componentsDefinition : moduleDefinition.getComponents()) {
                if (componentsDefinition.getId().equals(id)) {
                    for (ComposerDefinition composerDefinition : componentsDefinition.getComposers()) {
                        configuration.addComposer(getComposer(composerDefinition));
                    }
                    for (ComponentDefinition componentDefinition : componentsDefinition.getComponents()) {
                        ComponentConfiguration component = getComponent(componentDefinition);
                        if (component != null) {
                            configuration.addComponent(component);
                        }
                    }
                    for (TypeMappingDefinition typeMappingDefinition : componentsDefinition.getTypeMappings()) {
                        configuration.addTypeMapping(classForName(typeMappingDefinition.getType()), classForName(typeMappingDefinition.getImplementation()));
                    }
                }
            }
        }

        return configuration;
    }

    private ComponentComposer getComposer(ComposerDefinition composerDefinition) {
        Class clazz = classForName(composerDefinition.getClassName());
        if (!ComponentComposer.class.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Composer must be of type ComponentComposer");
        }
        try {
            return (ComponentComposer) clazz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Unable to instantiate composer");
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to instantiate composer");
        }
    }

    private ComponentConfiguration getComponent(ComponentDefinition componentDefinition) {
        if (isProvider(componentDefinition)) {
            return getProvider(componentDefinition);
        } else if (isImplementation(componentDefinition)) {
            return getImplementation(componentDefinition);
        } else if (isConfigured(componentDefinition)) {
            return getConfigured(componentDefinition);
        } else if (isObserved(componentDefinition)) {
            return getObserved(componentDefinition);
        } else {
            throw new IllegalStateException("Unable to add component with key " + componentDefinition.getType());
        }
    }

    private ComponentConfiguration getObserved(ComponentDefinition componentDefinition) {
        Class<?> key = classForName(componentDefinition.getType());
        String workspace = StringUtils.isNotBlank(componentDefinition.getWorkspace()) ? componentDefinition.getWorkspace() : ContentRepository.CONFIG;
        return new ConfiguredComponentConfiguration(key, workspace, componentDefinition.getPath(), true);
    }

    private ComponentConfiguration getConfigured(ComponentDefinition componentDefinition) {
        Class<?> key = classForName(componentDefinition.getType());
        String workspace = StringUtils.isNotBlank(componentDefinition.getWorkspace()) ? componentDefinition.getWorkspace() : ContentRepository.CONFIG;
        return new ConfiguredComponentConfiguration(key, workspace, componentDefinition.getPath(), false);
    }

    private ComponentConfiguration getImplementation(ComponentDefinition componentDefinition) {
        Class type = classForName(componentDefinition.getType());
        Class implementation = classForName(componentDefinition.getImplementation());

        if (ComponentFactory.class.isAssignableFrom(implementation)) {
            return new ComponentFactoryConfiguration(type, implementation);
        } else {

            // TODO do we really need to say the scope here? wasn't this just to single out the non annotated classes?

            // TODO should be enough to just register all

            if (type.equals(implementation)) {
                if (implementation.isAnnotationPresent(Singleton.class)) {
                    return new ImplementationConfiguration(type, implementation);
                } else if (implementation.isAnnotationPresent(RequestScoped.class)) {
                    return new ImplementationConfiguration(type, implementation);
                } else if (implementation.isAnnotationPresent(SessionScoped.class)) {
                    return new ImplementationConfiguration(type, implementation);
                } else {
                    // non scoped component declaration
                }
            } else {
                if (implementation.isAnnotationPresent(Singleton.class)) {
                    return new ImplementationConfiguration(type, implementation);
                } else if (implementation.isAnnotationPresent(RequestScoped.class)) {
                    return new ImplementationConfiguration(type, implementation);
                } else if (implementation.isAnnotationPresent(SessionScoped.class)) {
                    return new ImplementationConfiguration(type, implementation);
                } else {
                    // non scoped component declaration
                }
            }
        }
        return null;
    }

    private ComponentConfiguration getProvider(ComponentDefinition componentDefinition) {
        Class key = classForName(componentDefinition.getType());
        Class provider = classForName(componentDefinition.getProvider());
        if (Provider.class.isAssignableFrom(provider)) {
            throw new UnsupportedOperationException("javax.inject.Provider not supported yet");
        } else if (ComponentFactory.class.isAssignableFrom(provider)) {
            return new ComponentFactoryConfiguration(key, provider);
        }
        throw new IllegalStateException("Unknown provider type " + provider);
    }

    private boolean isObserved(ComponentDefinition componentDefinition) {
        return StringUtils.isNotBlank(componentDefinition.getPath()) && Boolean.parseBoolean(componentDefinition.getObserved());
    }

    private boolean isConfigured(ComponentDefinition componentDefinition) {
        return StringUtils.isNotBlank(componentDefinition.getPath()) && !Boolean.parseBoolean(componentDefinition.getObserved());
    }

    private boolean isImplementation(ComponentDefinition componentDefinition) {
        return StringUtils.isNotBlank(componentDefinition.getImplementation());
    }

    private boolean isProvider(ComponentDefinition componentDefinition) {
        return StringUtils.isNotBlank(componentDefinition.getProvider());
    }

    private Class<?> classForName(String className) {
        // TODO use Classes
        try {
            return Class.forName(className, false, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
