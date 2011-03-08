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
package info.magnolia.objectfactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.util.DeprecationUtil;

/**
 * Abstract ComponentProvider that supports component factories. Subclasses are responsible for registering components
 * and factories. Both components and factories are created on-demand when registered as classes.
 *
 * @author tmattsson
 */
public abstract class AbstractComponentProvider implements HierarchicalComponentProvider {

    private static class ComponentDefinition {

        private Class<?> type;
        private Object instance;
        private ComponentFactory factory;
        private Class<? extends ComponentFactory> factoryClass;
        private Class<?> implementationType;

        public Class<?> getType() {
            return type;
        }

        public void setType(Class<?> type) {
            this.type = type;
        }

        public Object getInstance() {
            return instance;
        }

        public void setInstance(Object instance) {
            this.instance = instance;
        }

        public ComponentFactory getFactory() {
            return factory;
        }

        public void setFactory(ComponentFactory factory) {
            this.factory = factory;
        }

        /**
         * The implementation type is unknown for components created by ComponentFactories.
         */
        public Class<?> getImplementationType() {
            return implementationType;
        }

        public void setImplementationType(Class<?> implementationType) {
            this.implementationType = implementationType;
        }

        public Class<? extends ComponentFactory> getFactoryClass() {
            return factoryClass;
        }

        public void setFactoryClass(Class<? extends ComponentFactory> factoryClass) {
            this.factoryClass = factoryClass;
        }

        public boolean isFactory() {
            return factoryClass != null || factory != null;
        }
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private HierarchicalComponentProvider parent;
    private final Map<Class<?>, ComponentDefinition> definitions = new ConcurrentHashMap<Class<?>, ComponentDefinition>();

    protected AbstractComponentProvider() {
    }

    protected AbstractComponentProvider(HierarchicalComponentProvider parent) {
        this.parent = parent;
    }

    public HierarchicalComponentProvider getParent() {
        return parent;
    }

    public <T> T getSingleton(Class<T> type) {
        DeprecationUtil.isDeprecated();
        return getComponent(type);
    }

    public synchronized <T> T getComponent(Class<T> type) {
        ComponentDefinition definition = getComponentDefinition(type);
        if (definition == null) {
            if (parent != null && parent.isConfiguredFor(type))
                return parent.getComponent(type);
            // Register the component on-demand
            if (!Classes.isConcrete(type))
                throw new MgnlInstantiationException("No concrete implementation defined for " + type);
            registerComponent(type, type);
            definition = getComponentDefinition(type);
        }
        Object instance = definition.getInstance();
        if (instance == null) {
            log.debug("No instance for {} yet, creating new one.", type);
            instance = newInstance(type);
            definition.setInstance(instance);
            log.debug("New instance for {} created: {}", type, instance);
        }
        return (T) instance;
    }

    public synchronized <T> T newInstance(Class<T> type) {
        if (type == null) {
            log.error("type can't be null", new Throwable());
            return null;
        }
        try {
            ComponentDefinition definition = getComponentDefinition(type);
            if (definition == null) {
                if (parent != null && parent.isConfiguredFor(type))
                    return parent.newInstance(type);
                if (!Classes.isConcrete(type)) {
                    throw new MgnlInstantiationException("No concrete implementation defined for " + type);
                }
                return (T) createInstance(type);
            }
            if (definition.isFactory()) {
                return this.<T>instantiateFactoryIfNecessary(definition).newInstance();
            }
            return (T) createInstance(definition.getImplementationType());
        } catch (Exception e) {
            if (e instanceof MgnlInstantiationException) {
                throw (MgnlInstantiationException) e;
            }
            throw new MgnlInstantiationException("Can't instantiate an implementation of this class [" + type.getName() + "]: " + ExceptionUtils.getMessage(e), e);
        }
    }

    public <C> Class<? extends C> getImplementation(Class<C> type) throws ClassNotFoundException {
        ComponentDefinition definition = getComponentDefinition(type);
        if (definition == null)
            if (parent != null && parent.isConfiguredFor(type))
                return parent.getImplementation(type);
            else
                return type;
        if (definition.isFactory())
            return type;
        return (Class<? extends C>) definition.getImplementationType();
    }

    protected synchronized void registerComponent(Class<?> type, Class<?> implementationType) {
        if (definitions.containsKey(type))
            throw new MgnlInstantiationException("Component already registered for type " + type.getName());
        if (!Classes.isConcrete(implementationType)) {
            throw new MgnlInstantiationException("Implementation type is not a concrete class for type" + type);
        }
        if (ComponentFactory.class.isAssignableFrom(implementationType)) {
            ComponentDefinition definition = new ComponentDefinition();
            definition.setType(type);
            definition.setFactoryClass((Class<? extends ComponentFactory>) implementationType);
            definitions.put(type, definition);
        } else {
            ComponentDefinition definition = new ComponentDefinition();
            definition.setType(type);
            definition.setImplementationType(implementationType);
            definitions.put(type, definition);
        }
    }

    protected synchronized void registerComponentFactory(Class<?> type, ComponentFactory componentFactory) {
        if (definitions.containsKey(type))
            throw new MgnlInstantiationException("Component already registered for type " + type.getName());
        ComponentDefinition definition = new ComponentDefinition();
        definition.setType(type);
        definition.setFactory(componentFactory);
        definitions.put(type, definition);
    }

    protected synchronized void registerInstance(Class<?> type, Object instance) {
        if (definitions.containsKey(type))
            throw new MgnlInstantiationException("Component already registered for type " + type.getName());
        ComponentDefinition definition = new ComponentDefinition();
        definition.setType(type);
        definition.setInstance(instance);
        definitions.put(type, definition);
    }

    private ComponentDefinition getComponentDefinition(Class<?> type) {
        return definitions.get(type);
    }

    private <T> ComponentFactory<T> instantiateFactoryIfNecessary(ComponentDefinition definition) {
        if (definition.getFactoryClass() != null && definition.getFactory() == null) {
            definition.setFactory(createInstance(definition.getFactoryClass()));
        }
        return definition.getFactory();
    }

    protected <T> T createInstance(Class<T> implementationType) {
        return Classes.getClassFactory().newInstance(implementationType);
    }

    protected synchronized void removeComponent(Class<?> type) {
        definitions.remove(type);
    }

    protected synchronized void clear() {
        definitions.clear();
    }

    public boolean isConfiguredFor(Class<?> type) {
        ComponentDefinition definition = getComponentDefinition(type);
        if (definition != null)
            return true;
        if (parent != null)
            return parent.isConfiguredFor(type);
        return false;
    }
}
