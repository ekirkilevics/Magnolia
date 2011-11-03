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
package info.magnolia.objectfactory.configuration;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import info.magnolia.module.model.ComponentDefinition;
import info.magnolia.module.model.ComponentsDefinition;
import info.magnolia.module.model.ConfigurerDefinition;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.TypeMappingDefinition;
import info.magnolia.repository.RepositoryConstants;

/**
 * Builder for creating {@link ComponentProviderConfiguration}s from component definitions.
 *
 * @version $Id$
 */
public class ComponentProviderConfigurationBuilder {

    /**
     * Reads component definitions from the specified resources and returns a {@link ComponentProviderConfiguration}.
     */
    public ComponentProviderConfiguration readConfiguration(List<String> resourcePaths, String id) {
        ComponentConfigurationReader reader = new ComponentConfigurationReader();
        List<ComponentsDefinition> componentsDefinitions = reader.readAll(resourcePaths);
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        for (ComponentsDefinition componentsDefinition : componentsDefinitions) {
            if (componentsDefinition.getId().equals(id)) {
                addComponents(configuration, componentsDefinition);
            }
        }
        return configuration;
    }

    /**
     * Reads component definitions from module descriptors and return a {@link ComponentProviderConfiguration}
     * containing all components with the given id.
     */
    public ComponentProviderConfiguration getComponentsFromModules(String id, List<ModuleDefinition> moduleDefinitions) {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        for (ModuleDefinition moduleDefinition : moduleDefinitions) {
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
            configuration.addComponent(getComponent(componentDefinition));
        }
        for (TypeMappingDefinition typeMappingDefinition : componentsDefinition.getTypeMappings()) {
            configuration.addTypeMapping(classForName(typeMappingDefinition.getType()), classForName(typeMappingDefinition.getImplementation()));
        }
    }

    protected ComponentConfigurer getConfigurer(ConfigurerDefinition configurerDefinition) {
        Class clazz = classForName(configurerDefinition.getClassName());
        if (!ComponentConfigurer.class.isAssignableFrom(clazz)) {
            throw new ComponentConfigurationException("Configurer must be of type ComponentConfigurer [" + clazz + "]");
        }
        try {
            return (ComponentConfigurer) clazz.newInstance();
        } catch (InstantiationException e) {
            throw new ComponentConfigurationException("Unable to instantiate configurer [" + clazz + "]", e);
        } catch (IllegalAccessException e) {
            throw new ComponentConfigurationException("Unable to instantiate configurer [" + clazz + "]", e);
        }
    }

    protected ComponentConfiguration getComponent(ComponentDefinition definition) {
        if (isProvider(definition)) {
            return getProvider(definition);
        } else if (isImplementation(definition)) {
            return getImplementation(definition);
        } else if (isConfigured(definition)) {
            return getConfigured(definition);
        } else if (isObserved(definition)) {
            return getObserved(definition);
        } else {
            throw new ComponentConfigurationException("Unable to add component with key " + definition.getType());
        }
    }

    protected ImplementationConfiguration getImplementation(ComponentDefinition definition) {
        ImplementationConfiguration configuration = new ImplementationConfiguration();
        configuration.setType(classForName(definition.getType()));
        configuration.setImplementation(classForName(definition.getImplementation()));
        configuration.setScope(definition.getScope());
        configuration.setLazy(parseLazyFlag(definition));
        return configuration;
    }

    protected ComponentConfiguration getProvider(ComponentDefinition definition) {
        ProviderConfiguration configuration = new ProviderConfiguration();
        configuration.setType(classForName(definition.getType()));
        configuration.setProviderClass(classForName(definition.getProvider()));
        configuration.setScope(definition.getScope());
        configuration.setLazy(parseLazyFlag(definition));
        return configuration;
    }

    protected ComponentConfiguration getConfigured(ComponentDefinition definition) {
        ConfiguredComponentConfiguration configuration = new ConfiguredComponentConfiguration();
        configuration.setType(classForName(definition.getType()));
        configuration.setWorkspace(StringUtils.defaultIfEmpty(configuration.getWorkspace(), RepositoryConstants.CONFIG));
        configuration.setPath(definition.getPath());
        configuration.setObserved(false);
        configuration.setScope(definition.getScope());
        configuration.setLazy(parseLazyFlag(definition));
        return configuration;
    }

    protected ComponentConfiguration getObserved(ComponentDefinition definition) {
        ConfiguredComponentConfiguration configuration = new ConfiguredComponentConfiguration();
        configuration.setType(classForName(definition.getType()));
        configuration.setWorkspace(StringUtils.defaultIfEmpty(configuration.getWorkspace(), RepositoryConstants.CONFIG));
        configuration.setPath(definition.getPath());
        configuration.setObserved(true);
        configuration.setScope(definition.getScope());
        configuration.setLazy(parseLazyFlag(definition));
        return configuration;
    }

    protected boolean isImplementation(ComponentDefinition definition) {
        return StringUtils.isNotBlank(definition.getImplementation());
    }

    protected boolean isProvider(ComponentDefinition definition) {
        return StringUtils.isNotBlank(definition.getProvider());
    }

    protected boolean isConfigured(ComponentDefinition definition) {
        return StringUtils.isNotBlank(definition.getPath()) && !Boolean.parseBoolean(definition.getObserved());
    }

    protected boolean isObserved(ComponentDefinition definition) {
        return StringUtils.isNotBlank(definition.getPath()) && Boolean.parseBoolean(definition.getObserved());
    }

    protected boolean parseLazyFlag(ComponentDefinition definition) {
        String lazy = definition.getLazy();
        return StringUtils.isEmpty(lazy) || Boolean.parseBoolean(lazy);
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
