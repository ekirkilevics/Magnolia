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

import java.util.Arrays;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.Injector;
import com.mycila.inject.jsr250.Jsr250Injector;
import info.magnolia.objectfactory.CandidateParameterResolver;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.ParameterResolver;


/**
 * ComponentProvider implementation based on Guice.
 *
 * @version $Id$
 * @see ComponentProvider
 * @see GuiceComponentProviderBuilder
 */
public class GuiceComponentProvider implements ComponentProvider {

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
        if (!GuiceUtils.hasExplicitBindingFor(injector, type)) {
            throw new IllegalStateException("No component configuration for type [" + type.getName() + "] found. Please add a configuration to your module descriptor.");
        }
        return injector.getInstance(type);
    }

    @Override
    public <T> T newInstance(Class<T> type, Object... parameters) {
        return newInstanceWithParameterResolvers(type, new CandidateParameterResolver(parameters));
    }

    @Override
    public <T> T newInstanceWithParameterResolvers(Class<T> type, ParameterResolver... parameterResolvers) {
        if (this.manufacturer == null) {
            this.manufacturer = new ObjectManufacturer();
        }
        Class<? extends T> implementation = getImplementation(type);
        parameterResolvers = concat(parameterResolvers, new GuiceParameterResolver(injector));
        T instance = (T) manufacturer.newInstance(implementation, parameterResolvers);
        injectMembers(instance);
        return instance;
    }

    private ParameterResolver[] concat(ParameterResolver[] array, GuiceParameterResolver extra) {
        ParameterResolver[] newArray = Arrays.copyOf(array, array.length + 1);
        newArray[array.length] = extra;
        return newArray;
    }

    public Injector getInjector() {
        return injector;
    }

    public <T> Provider<T> getProvider(Class<T> type) {
        if (!GuiceUtils.hasExplicitBindingFor(injector, type)) {
            return null;
        }
        return injector.getProvider(type);
    }

    public void injectMembers(Object instance) {
        injector.injectMembers(instance);
    }

    public void destroy() {
        injector.destroy();
    }

    @Override
    public GuiceComponentProvider getParent() {
        return parentComponentProvider;
    }
}
