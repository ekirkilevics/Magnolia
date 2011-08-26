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
import javax.inject.Inject;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.mycila.inject.jsr250.Jsr250Injector;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.HierarchicalComponentProvider;
import info.magnolia.objectfactory.MutableComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;


/**
 * ComponentProvider implementation that uses a Guice Injector to look up singletons.
 *
 * @version $Id$
 */
public class GuiceComponentProvider implements HierarchicalComponentProvider {

    @Inject
    private Jsr250Injector injector;
    private ObjectManufacturer manufacturer;
    private final Map<Class<?>, Class<?>> typeMappings;
    private GuiceComponentProvider parentComponentProvider;

    public GuiceComponentProvider(Map<Class<?>, Class<?>> typeMappings, GuiceComponentProvider parentComponentProvider) {
        this.parentComponentProvider = parentComponentProvider;
        this.typeMappings = typeMappings;
    }

    @Override
    public <T> Class<? extends T> getImplementation(Class<T> type) {
        Class<?> implementation = typeMappings.get(type);
        if (implementation == null) {
            if (parentComponentProvider != null) {
                return parentComponentProvider.getImplementation(type);
            }
            return type;
        }
        if (ComponentFactory.class.isAssignableFrom(implementation)) {
            return type;
        }
        return (Class<? extends T>) implementation;
    }

    @Override
    @Deprecated
    public <T> T getSingleton(Class<T> type) {
        return getComponent(type);
    }

    @Override
    public <T> T getComponent(Class<T> type) {
        if (!hasExplicitBindingFor(injector, type)) {
            return null;
        }
        return injector.getInstance(type);
    }

    @Override
    public <T> T newInstance(Class<T> type, Object... parameters) {
        if (this.manufacturer == null) {
            this.manufacturer = new ObjectManufacturer(injector);
        }
        return (T) manufacturer.manufacture(getImplementation(type), parameters);
    }

    @Override
    public GuiceComponentProvider createChild(ComponentProviderConfiguration componentsConfiguration) {
        return new GuiceComponentProviderBuilder().withConfiguration(componentsConfiguration).withParent(this).build();
    }

    public Injector getInjector() {
        return injector;
    }

    public void destroy() {
        injector.destroy();
    }

    @Override
    public HierarchicalComponentProvider getParent() {
        return parentComponentProvider;
    }

    @Override
    public boolean isConfiguredFor(Class<?> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MutableComponentProvider createChild() {
        throw new UnsupportedOperationException();
    }

    private static boolean hasExplicitBindingFor(Injector injector, Class<?> type) {
        Injector target = injector;
        do {
            if (target.getBindings().containsKey(Key.get(type))) {
                return true;
            }
            target = target.getParent();
        } while (target != null);
        return false;
    }
}
