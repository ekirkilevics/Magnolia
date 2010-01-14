/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Central factory instantiating most Magnolia beans, managers, observers and so on.
 *
 * @author Philipp Bracher
 * @version $Revision: 25238 $ ($Author: pbaerfuss $)
 */
public class DefaultComponentProvider<T> implements ComponentProvider<T> {
    private final static Logger log = LoggerFactory.getLogger(DefaultComponentProvider.class);

    /**
     * Registered singleton instances.
     */
    private final Map<Class<T>, T> instances = new HashMap<Class<T>, T>();

    /**
     * Registered Prototypes used for new Instance.
     */
    private final Map<Class<T>, ComponentFactory<T>> factories = new HashMap<Class<T>, ComponentFactory<T>>();

    private final Properties mappings;

    DefaultComponentProvider(Properties mappings) {
        // Ideally, the dependency should be on SystemProperty or other relevant object.
        // Hopefully, we'll de-staticize SystemProperty soon.
        this.mappings = mappings;

        // TODO : we have a dependency on ClassFactory, but we can't inject it here,
        // since it might get swapped later
    }

    public synchronized T getSingleton(Class<T> type) {
        T instance = instances.get(type);
        if (instance == null) {
            instance = newInstance(type);
            instances.put(type, instance);
        }

        return instance;
    }

    /**
     * Creates a new instance of the passed interface / class by using the registered implementation.
     * If this fails a {@link IllegalStateException} is thrown.
     *
     * @throws IllegalStateException
     */
    public T newInstance(Class<T> type) {
        if (type == null) {
            log.error("type can't be null", new Throwable());
            return null;
        }
        try {
            if (factories.containsKey(type)) {
                return factories.get(type).newInstance();
            }

            final String className = getImplementationName(type);
            if (isInRepositoryDefinition(className)) {
                String repository = ContentRepository.CONFIG;
                String path = className;
                if (className.indexOf(':') >= 0) {
                    repository = StringUtils.substringBefore(className, ":");
                    path = StringUtils.substringAfter(className, ":");
                }
                final ObservedComponentFactory<T> factory = new ObservedComponentFactory<T>(repository, path, type);
                setInstanceFactory(type, factory);
                // now that the factory is registered, we call ourself again
                return newInstance(type);
            } else {
                Class clazz = ObjectFactory.classes().forName(className);
                Object instance = ObjectFactory.classes().newInstance(clazz);

                if (instance instanceof ComponentFactory) {
                    final ComponentFactory<T> factory = (ComponentFactory<T>) instance;
                    setInstanceFactory(type, factory);
                    return factory.newInstance();
                }
                return (T) instance;
            }
        }
        catch (Exception e) {
            throw new IllegalStateException("Can't instantiate an implementation of this class [" + type.getName() + "]: " + ExceptionUtils.getMessage(e), e);
        }
    }

    public <C> Class<? extends C> getImplementation(Class<C> type) throws ClassNotFoundException {
        String className = getImplementationName(type);
        if (!isInRepositoryDefinition(className)) {
            return ObjectFactory.classes().forName(className);
        } else {
            return type;
        }
    }

    protected String getImplementationName(Class type) {
        final String name = type.getName();
        return mappings.getProperty(name, name);
    }

    private boolean isInRepositoryDefinition(String className) {
        return className.startsWith("/") || className.indexOf(':') >= 0;
    }

    /**
     * todo - this is only used in tests
     */
    public void setDefaultImplementation(Class<T> type, Class<? extends T> impl) {
        setDefaultImplementation(type, impl.getName());
    }

    /**
     * todo - this is only used in tests
     */
    public void setDefaultImplementation(Class type, String impl) {
        if (!mappings.containsKey(type.getName())) {
            setImplementation(type, impl);
        }
    }

    /**
     * todo - this is only used in tests
     */
    public void setImplementation(Class<T> type, Class<? extends T> impl) {
        setImplementation(type, impl.getName());
    }

    /**
     * todo - this is only used internally
     */
    public void setImplementation(Class type, String impl) {
        mappings.setProperty(type.getName(), impl);
    }

    /**
     * Register an instance which will be returned by getSingleton().
     * todo - this is only used in tests
     */
    public void setInstance(Class<T> type, T instance) {
        instances.put(type, instance);
    }

    /**
     * newInstance will use this prototype for cloning a new object.
     * todo - this is only used in tests
     */
    public void setInstanceFactory(Class<T> type, ComponentFactory<T> factory) {
        factories.put(type, factory);
    }

    /**
     * todo - this is only used in tests
     */
    public void clear() {
        factories.clear();
        instances.clear();
    }

}