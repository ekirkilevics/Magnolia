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

import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import info.magnolia.objectfactory.configuration.ComponentConfiguration;
import info.magnolia.objectfactory.configuration.ComponentFactoryConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ConfiguredComponentConfiguration;
import info.magnolia.objectfactory.configuration.ImplementationConfiguration;
import info.magnolia.objectfactory.configuration.InstanceConfiguration;


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
            ComponentConfiguration config = entry.getValue();
            if (config instanceof ImplementationConfiguration) {
                registerImplementation((ImplementationConfiguration) config);
            } else if (config instanceof InstanceConfiguration) {
                registerInstance((InstanceConfiguration) config);
            } else if (config instanceof ComponentFactoryConfiguration) {
                registerComponentFactory((ComponentFactoryConfiguration) config);
            } else if (config instanceof ConfiguredComponentConfiguration) {
                registerConfiguredComponent((ConfiguredComponentConfiguration) config);
            } else {
                throw new IllegalStateException();
            }
        }

        for (Object configurer : configuration.getConfigurers()) {
            if (configurer instanceof Module) {
                install((Module) configurer);
            }
        }
    }

    private <T> void registerConfiguredComponent(ConfiguredComponentConfiguration configuration) {
        Class key = configuration.getType();
        if (configuration.isObserved()) {
            bind(key).toProvider(new GuiceObservedComponentProvider<T>(configuration.getWorkspace(), configuration.getPath(), key)).in(Scopes.SINGLETON);
        } else {
            bind(key).toProvider(new GuiceConfiguredComponentProvider(configuration.getPath(), configuration.getWorkspace())).in(Scopes.SINGLETON);
        }
    }

    private void registerComponentFactory(ComponentFactoryConfiguration configuration) {
        bind(configuration.getType()).toProvider(new GuiceComponentFactoryProviderAdapter(configuration.getFactoryClass())).in(Scopes.SINGLETON);
    }

    private void registerInstance(InstanceConfiguration configuration) {
        bind(configuration.getType()).toInstance(configuration.getInstance());
    }

    private void registerImplementation(ImplementationConfiguration configuration) {
        Class key = configuration.getType();
        Class implementation = configuration.getImplementation();

        if (key.equals(implementation)) {
            bind(implementation);
        } else {
            bind(key).to(implementation);
        }
    }
}
