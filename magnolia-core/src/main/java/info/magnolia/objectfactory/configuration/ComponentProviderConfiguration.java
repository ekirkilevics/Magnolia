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

import java.util.ArrayList;
import java.util.Collection;


/**
 * Configuration passed to {@link info.magnolia.objectfactory.MutableComponentProvider#configure(ComponentProviderConfiguration)}.
 *
 * @version $Id$
 */
public class ComponentProviderConfiguration implements Cloneable {

    private Collection<ImplementationConfiguration<?>> implementations = new ArrayList<ImplementationConfiguration<?>>();

    private Collection<InstanceConfiguration<?>> instances = new ArrayList<InstanceConfiguration<?>>();

    private Collection<ComponentFactoryConfiguration<?>> factories = new ArrayList<ComponentFactoryConfiguration<?>>();

    private Collection<ConfiguredComponentConfiguration<?>> configured = new ArrayList<ConfiguredComponentConfiguration<?>>();

    public Collection<ImplementationConfiguration<?>> getImplementations() {
        return implementations;
    }

    public void setImplementations(Collection<ImplementationConfiguration<?>> implementations) {
        this.implementations = implementations;
    }

    public void addImplementation(ImplementationConfiguration<?> configuration) {
        implementations.add(configuration);
    }

    public Collection<InstanceConfiguration<?>> getInstances() {
        return instances;
    }

    public void setInstances(Collection<InstanceConfiguration<?>> instances) {
        this.instances = instances;
    }

    public void addInstance(InstanceConfiguration<?> configuration) {
        instances.add(configuration);
    }

    public Collection<ComponentFactoryConfiguration<?>> getFactories() {
        return factories;
    }

    public void setFactories(Collection<ComponentFactoryConfiguration<?>> factories) {
        this.factories = factories;
    }

    public void addFactory(ComponentFactoryConfiguration<?> configuration) {
        factories.add(configuration);
    }

    public Collection<ConfiguredComponentConfiguration<?>> getConfigured() {
        return configured;
    }

    public void setConfigured(Collection<ConfiguredComponentConfiguration<?>> configured) {
        this.configured = configured;
    }

    public void addConfigured(ConfiguredComponentConfiguration<?> configuration) {
        configured.add(configuration);
    }

    @Override
    public ComponentProviderConfiguration clone() {
        try {
            ComponentProviderConfiguration clone = (ComponentProviderConfiguration) super.clone();
            clone.implementations = new ArrayList<ImplementationConfiguration<?>>();
            for (ImplementationConfiguration<?> implementation : implementations) {
                clone.implementations.add(implementation.clone());
            }
            clone.instances = new ArrayList<InstanceConfiguration<?>>();
            for (InstanceConfiguration<?> instance : instances) {
                clone.instances.add(instance.clone());
            }
            clone.factories = new ArrayList<ComponentFactoryConfiguration<?>>();
            for (ComponentFactoryConfiguration<?> factory : factories) {
                clone.factories.add(factory.clone());
            }
            clone.configured = new ArrayList<ConfiguredComponentConfiguration<?>>();
            for (ConfiguredComponentConfiguration<?> componentConfiguration : configured) {
                clone.configured.add(componentConfiguration.clone());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    public <T> void registerImplementation(Class<T> type, Class<? extends T> implementation) {
        addImplementation(ImplementationConfiguration.valueOf(type, implementation));
    }

    public <T> void registerImplementation(Class<T> type) {
        registerImplementation(type, type);
    }

    public <T> void registerInstance(Class<T> type, T instance) {
        addInstance(InstanceConfiguration.valueOf(type, instance));
    }

    public void combine(ComponentProviderConfiguration components) {
        components = components.clone();
        this.implementations.addAll(components.implementations);
        this.instances.addAll(components.instances);
        this.factories.addAll(components.factories);
        this.configured.addAll(components.configured);
    }

    public boolean hasConfigFor(Class<?> type) {
        for (ImplementationConfiguration<?> implementation : implementations) {
            if (implementation.getType().equals(type)) return true;
        }
        for (InstanceConfiguration<?> instance : instances) {
            if (instance.getType().equals(type)) return true;
        }
        for (ComponentFactoryConfiguration<?> factory : factories) {
            if (factory.getType().equals(type)) return true;
        }
        for (ConfiguredComponentConfiguration<?> configuration : configured) {
            if (configuration.getType().equals(type)) return true;
        }
        return false;
    }
}
