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

import java.util.List;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ComponentDefinition;
import info.magnolia.module.model.ComponentsDefinition;
import info.magnolia.module.model.ConfigurerDefinition;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.TypeMappingDefinition;
import info.magnolia.objectfactory.ComponentConfigurer;
import info.magnolia.objectfactory.ComponentFactory;

/**
 * Builder for creating a ComponentProviderConfiguration from components configured in module descriptors.
 *
 * @version $Id$
 */
public class ComponentConfigurationBuilder {

    public ComponentProviderConfiguration readConfiguration(List<String> resourcePaths) {
        ComponentConfigurationReader reader = new ComponentConfigurationReader();
        List<ComponentsDefinition> componentsDefinitions = reader.readAll(resourcePaths);
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        for (ComponentsDefinition componentsDefinition : componentsDefinitions) {
            addComponents(configuration, componentsDefinition);
        }
        return configuration;
    }

    public ComponentProviderConfiguration getComponentsFromModules(ModuleRegistry moduleRegistry, String id) {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        for (ModuleDefinition moduleDefinition : moduleRegistry.getModuleDefinitions()) {
            for (ComponentsDefinition componentsDefinition : moduleDefinition.getComponents()) {
                if (componentsDefinition.getId().equals(id)) {
                    addComponents(configuration, componentsDefinition);
                }
            }
        }
        return configuration;
    }

    public void addComponents(ComponentProviderConfiguration configuration, ComponentsDefinition componentsDefinition) {
        for (ConfigurerDefinition configurerDefinition : componentsDefinition.getConfigurers()) {
            configuration.addConfigurer(getConfigurer(configurerDefinition));
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

    protected ComponentConfigurer getConfigurer(ConfigurerDefinition configurerDefinition) {
        Class clazz = classForName(configurerDefinition.getClassName());
        if (!ComponentConfigurer.class.isAssignableFrom(clazz)) {
            throw new ComponentConfigurationException("Configurer must be of type ComponentConfigurer");
        }
        try {
            return (ComponentConfigurer) clazz.newInstance();
        } catch (InstantiationException e) {
            throw new ComponentConfigurationException("Unable to instantiate configurer");
        } catch (IllegalAccessException e) {
            throw new ComponentConfigurationException("Unable to instantiate configurer");
        }
    }

    protected ComponentConfiguration getComponent(ComponentDefinition componentDefinition) {
        if (isProvider(componentDefinition)) {
            return getProvider(componentDefinition);
        } else if (isImplementation(componentDefinition)) {
            return getImplementation(componentDefinition);
        } else if (isConfigured(componentDefinition)) {
            return getConfigured(componentDefinition);
        } else if (isObserved(componentDefinition)) {
            return getObserved(componentDefinition);
        } else {
            throw new ComponentConfigurationException("Unable to add component with key " + componentDefinition.getType());
        }
    }

    protected ComponentConfiguration getObserved(ComponentDefinition componentDefinition) {
        Class<?> key = classForName(componentDefinition.getType());
        String workspace = StringUtils.isNotBlank(componentDefinition.getWorkspace()) ? componentDefinition.getWorkspace() : ContentRepository.CONFIG;
        return new ConfiguredComponentConfiguration(key, workspace, componentDefinition.getPath(), true);
    }

    protected ComponentConfiguration getConfigured(ComponentDefinition componentDefinition) {
        Class<?> key = classForName(componentDefinition.getType());
        String workspace = StringUtils.isNotBlank(componentDefinition.getWorkspace()) ? componentDefinition.getWorkspace() : ContentRepository.CONFIG;
        return new ConfiguredComponentConfiguration(key, workspace, componentDefinition.getPath(), false);
    }

    protected ComponentConfiguration getImplementation(ComponentDefinition componentDefinition) {
        Class type = classForName(componentDefinition.getType());
        Class implementation = classForName(componentDefinition.getImplementation());

        if (ComponentFactory.class.isAssignableFrom(implementation)) {
            return new ComponentFactoryConfiguration(type, implementation);
        } else {
            if (type.equals(implementation)) {
                return new ImplementationConfiguration(type, implementation);
            } else {
                return new ImplementationConfiguration(type, implementation);
            }
        }
    }

    protected ComponentConfiguration getProvider(ComponentDefinition componentDefinition) {
        Class key = classForName(componentDefinition.getType());
        Class provider = classForName(componentDefinition.getProvider());
        if (ComponentFactory.class.isAssignableFrom(provider)) {
            return new ComponentFactoryConfiguration(key, provider);
        }
        throw new ComponentConfigurationException("Unknown provider type " + provider);
    }

    protected boolean isObserved(ComponentDefinition componentDefinition) {
        return StringUtils.isNotBlank(componentDefinition.getPath()) && Boolean.parseBoolean(componentDefinition.getObserved());
    }

    protected boolean isConfigured(ComponentDefinition componentDefinition) {
        return StringUtils.isNotBlank(componentDefinition.getPath()) && !Boolean.parseBoolean(componentDefinition.getObserved());
    }

    protected boolean isImplementation(ComponentDefinition componentDefinition) {
        return StringUtils.isNotBlank(componentDefinition.getImplementation());
    }

    protected boolean isProvider(ComponentDefinition componentDefinition) {
        return StringUtils.isNotBlank(componentDefinition.getProvider());
    }

    /**
     * Returns the class denoted by the supplied class name without initializing the class.
     */
    protected Class<?> classForName(String className) {
        try {
            return Class.forName(className, false, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ComponentConfigurationException(e);
        }
    }
}
