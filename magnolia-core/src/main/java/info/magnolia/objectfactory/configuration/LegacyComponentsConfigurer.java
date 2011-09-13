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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.module.model.ComponentDefinition;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.ComponentConfigurationPath;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.ComponentProvider;

/**
 * This ComponentConfigurer configures components from properties. Each property key is the interface/base-class, and the
 * value is either the implementation-to-use class name, an implementation of {@link ComponentFactory} which is used to
 * instantiate the desired implementation, or the path to a node in the repository (in the form of
 * <code>repository:/path/to/node</code> or <code>/path/to/node</code>, which defaults to the <code>config</code>
 * repository). In the latter case, the component is constructed via
 * {@link info.magnolia.objectfactory.ObservedComponentFactory} and reflects (through observation) the contents of the
 * given path.
 * <p/>
 * This behaviour exists for backwards compatibility reasons, prefer configuring your components in a module
 * descriptor instead, inside a components tag.
 * <p/>
 * In order to remain backwards compatible implementations are added both as type mappings and as components and then
 * always scoped as lazy singletons.
 *
 * @version $Id$
 */
public class LegacyComponentsConfigurer implements ComponentConfigurer {

    private final static Logger log = LoggerFactory.getLogger(LegacyComponentsConfigurer.class);

    @Override
    public void doWithConfiguration(ComponentProvider parentComponentProvider, ComponentProviderConfiguration configuration) {

        MagnoliaConfigurationProperties configurationProperties = parentComponentProvider.getComponent(MagnoliaConfigurationProperties.class);

        if (configurationProperties != null) {
            for (String key : configurationProperties.getKeys()) {
                addComponent(configuration, key, configurationProperties.getProperty(key));
            }
        }
    }

    protected <T> void addComponent(ComponentProviderConfiguration componentProviderConfiguration, String key, String value) {

        final Class<T> type = (Class<T>) classForName(key);
        if (type == null) {
            log.debug("{} does not seem to resolve to a class. (property value: {})", key, value);
            return;
        }

        if (ComponentConfigurationPath.isComponentConfigurationPath(value)) {
            componentProviderConfiguration.addComponent(getObserved(type, value));
        } else {
            Class<? extends T> valueType = (Class<? extends T>) classForName(value);
            if (valueType == null) {
                log.debug("{} does not seem to resolve a class or a configuration path. (property key: {})", value, key);
            } else {
                if (ComponentFactory.class.isAssignableFrom(valueType)) {
                    componentProviderConfiguration.addComponent(getComponentFactory(type, (Class<? extends ComponentFactory<T>>) valueType));
                } else {
                    componentProviderConfiguration.addComponent(getImplementation(type, valueType));
                    componentProviderConfiguration.addTypeMapping(type, valueType);
                }
            }
        }
    }

    protected <T> ImplementationConfiguration getImplementation(Class<T> type, Class<? extends T> implementation) {
        ImplementationConfiguration configuration = new ImplementationConfiguration<T>();
        configuration.setType(type);
        configuration.setImplementation(implementation);
        configuration.setScope(ComponentDefinition.SCOPE_SINGLETON);
        configuration.setLazy(true);
        return configuration;
    }

    protected <T> ProviderConfiguration<T> getComponentFactory(Class<T> type, Class<? extends ComponentFactory<T>> factoryClass) {
        ProviderConfiguration<T> configuration = new ProviderConfiguration<T>();
        configuration.setType(type);
        configuration.setProviderClass(factoryClass);
        configuration.setScope(ComponentDefinition.SCOPE_SINGLETON);
        configuration.setLazy(true);
        return configuration;
    }

    protected <T> ConfiguredComponentConfiguration<T> getObserved(Class<T> type, String workspaceAndPath) {
        ComponentConfigurationPath path = new ComponentConfigurationPath(workspaceAndPath);
        ConfiguredComponentConfiguration<T> configuration = new ConfiguredComponentConfiguration<T>();
        configuration.setType(type);
        configuration.setWorkspace(path.getRepository());
        configuration.setPath(path.getPath());
        configuration.setObserved(true);
        configuration.setScope(ComponentDefinition.SCOPE_SINGLETON);
        configuration.setLazy(true);
        return configuration;
    }

    protected Class<?> classForName(String value) {
        try {
            return Classes.getClassFactory().forName(value);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
