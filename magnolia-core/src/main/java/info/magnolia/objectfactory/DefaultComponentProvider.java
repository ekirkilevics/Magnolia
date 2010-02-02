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
 * This {@link info.magnolia.objectfactory.ComponentProvider} is using the configuration provided by
 * {@link info.magnolia.cms.core.SystemProperty}. Each property key is the interface/base-class, and the value
 * is either the implementation-to-use class name, an implementation of {@link info.magnolia.objectfactory.ComponentFactory}
 * which is used to instantiate the desired implementation, or the path to a node in the repository (in the form of
 * <code>repository:/path/to/node</code> or <code>/path/to/node</code>, which defaults to the <code>config</code>
 * repository). In the latter case, the component is constructed via {@link info.magnolia.objectfactory.ObservedComponentFactory}
 * and reflects (through observation) the contents of the given path.
 *
 * @author Philipp Bracher
 * @version $Revision: 25238 $ ($Author: pbaerfuss $)
 */
public class DefaultComponentProvider implements ComponentProvider {
    private final static Logger log = LoggerFactory.getLogger(DefaultComponentProvider.class);

    /**
     * Registered singleton instances.
     */
    private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();

    /**
     * Registered prototypes used by newInstance().
     */
    private final Map<Class<?>, ComponentFactory<?>> factories = new HashMap<Class<?>, ComponentFactory<?>>();

    private final Properties mappings;

    DefaultComponentProvider(Properties mappings) {
        // Ideally, the dependency should be on SystemProperty or other relevant object.
        // Hopefully, we'll de-staticize SystemProperty soon.
        this.mappings = mappings;

        // TODO : we have a dependency on ClassFactory, but we can't inject it here,
        // since it might get swapped later
    }

    public synchronized <T> T getSingleton(Class<T> type) {
        T instance = (T) instances.get(type);
        if (instance == null) {
            log.debug("No instance for {} yet, creating new one.", type);
            instance = newInstance(type);
            instances.put(type, instance);
            log.debug("New instance for {} created: {}", type, instance);
        }

        return instance;
    }

    /**
     * Creates a new instance of the passed interface / class by using the registered implementation.
     * If this fails a {@link IllegalStateException} is thrown.
     *
     * @throws IllegalStateException
     */
    public <T> T newInstance(Class<T> type) {
        if (type == null) {
            log.error("type can't be null", new Throwable());
            return null;
        }

        try {
            if (factories.containsKey(type)) {
                final ComponentFactory<T> factory = (ComponentFactory<T>) factories.get(type);
                return factory.newInstance();
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
                final Class<?> clazz = Classes.getClassFactory().forName(className);
                final Object instance = Classes.getClassFactory().newInstance(clazz);

                if (instance instanceof ComponentFactory) {
                    final ComponentFactory<T> factory = (ComponentFactory<T>) instance;
                    setInstanceFactory(type, factory);
                    return factory.newInstance();
                }
                return (T) instance;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Can't instantiate an implementation of this class [" + type.getName() + "]: " + ExceptionUtils.getMessage(e), e);
        }
    }

    // TODO - is this needed / correct ?
    public <C> Class<? extends C> getImplementation(Class<C> type) throws ClassNotFoundException {
        final String className = getImplementationName(type);
        if (!isInRepositoryDefinition(className)) {
            return (Class<? extends C>) Classes.getClassFactory().forName(className);
        } else {
            return type;
        }
    }

    protected String getImplementationName(Class<?> type) {
        final String name = type.getName();
        return mappings.getProperty(name, name);
    }

    private boolean isInRepositoryDefinition(String className) {
        return className.startsWith("/") || className.indexOf(':') >= 0;
    }

    /**
     * Used only in tests.
     * @see {@link info.magnolia.test.ComponentsTestUtil}
     */
    public void setImplementation(Class<?> type, String impl) {
        mappings.setProperty(type.getName(), impl);
    }

    /**
     * Used only in tests.
     * @see {@link info.magnolia.test.ComponentsTestUtil}
     */
    public void setInstance(Class<?> type, Object instance) {
        instances.put(type, instance);
    }

    /**
     * Used only in tests.
     * @see {@link info.magnolia.test.ComponentsTestUtil}
     */
    public void setInstanceFactory(Class<?> type, ComponentFactory<?> factory) {
        factories.put(type, factory);
    }

    /**
     * Used only in tests.
     * <strong>Warning:</strong> this does NOT clear the *mappings*. With the current/default implementation,
     * this means tests also have to call SystemProperty.clearr()
     * @see {@link info.magnolia.test.ComponentsTestUtil}
     */
    public void clear() {
        factories.clear();
        instances.clear();
    }

}