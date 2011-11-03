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

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ProviderWithDependencies;
import com.mycila.inject.internal.guava.collect.ImmutableSet;
import com.mycila.inject.internal.guava.collect.Sets;
import com.mycila.inject.jsr250.Jsr250;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.ComponentFactoryUtil;
import info.magnolia.objectfactory.ComponentProvider;
import static com.mycila.inject.internal.guava.base.Preconditions.checkNotNull;

/**
 * Utilities for guice.
 *
 * @version $Id$
 */
public class GuiceUtils {

    public static <T> Provider<T> providerForInstance(final T instance) {
        return new Provider<T>() {
            @Override
            public T get() {
                return instance;
            }

            @Override
            public String toString() {
                return "Provider for: " + instance.toString();
            }
        };
    }

    public static <T> Provider<T> providerForComponentFactory(final Class<? extends ComponentFactory<T>> componentFactoryClass) {
        return new Provider<T>() {

            @Inject
            private ComponentProvider componentProvider;

            @Override
            public T get() {
                try {
                    ComponentFactory<T> componentFactory = ComponentFactoryUtil.createFactory(componentFactoryClass, componentProvider);
                    ((GuiceComponentProvider) componentProvider).injectMembers(componentFactory);
                    T instance = componentFactory.newInstance();
                    Jsr250.preDestroy(componentFactory);
                    return instance;
                } catch (InstantiationException e) {
                    throw new IllegalStateException("Failed to create ComponentFactory [" + componentFactoryClass + "]", e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Failed to create ComponentFactory [" + componentFactoryClass + "]", e);
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException("Failed to create ComponentFactory [" + componentFactoryClass + "]", e);
                }
            }

            @Override
            public String toString() {
                return "ComponentFactory: " + componentFactoryClass.toString();
            }
        };
    }

    public static <T> Provider<T> providerForComponentFactory(final ComponentFactory<T> componentFactory) {
        Set<InjectionPoint> injectionPoints = InjectionPoint.forInstanceMethodsAndFields(componentFactory.getClass());
        Set<Dependency<?>> mutableDeps = Sets.newHashSet();
        for (InjectionPoint ip : injectionPoints) {
            mutableDeps.addAll(ip.getDependencies());
        }
        final Set<Dependency<?>> dependencies = ImmutableSet.copyOf(mutableDeps);
        return new ProviderWithDependencies<T>() {

            @Override
            public Set<Dependency<?>> getDependencies() {
                return dependencies;
            }

            @Inject
            private void initialize(Injector injector) {
                injector.injectMembers(componentFactory);
            }

            @Override
            public T get() {
                return componentFactory.newInstance();
            }

            @PreDestroy
            void destroy() {
                Jsr250.preDestroy(componentFactory);
            }

            @Override
            public String toString() {
                return "ComponentFactory: " + componentFactory.toString();
            }
        };
    }

    public static boolean hasExplicitBindingFor(Injector injector, Class<?> type) {
        Key<?> key = Key.get(type);
        Injector target = injector;
        do {
            if (target.getBindings().containsKey(key)) {
                return true;
            }
            target = target.getParent();
        } while (target != null);
        return false;
    }

    /**
     * This is a copy of com.google.inject.util.Providers#guicify(javax.inject.Provider<T>) with added @PreDestroy
     * methods.
     */
    public static <T> com.google.inject.Provider<T> guicify(javax.inject.Provider<T> provider) {
        if (provider instanceof com.google.inject.Provider) {
            return (com.google.inject.Provider<T>) provider;
        }

        final javax.inject.Provider<T> delegate = checkNotNull(provider, "provider");

        // Ensure that we inject all injection points from the delegate provider.
        Set<InjectionPoint> injectionPoints =
                InjectionPoint.forInstanceMethodsAndFields(provider.getClass());
        if (injectionPoints.isEmpty()) {
            return new com.google.inject.Provider<T>() {
                @Override
                public T get() {
                    return delegate.get();
                }

                @Override
                public String toString() {
                    return "guicified(" + delegate + ")";
                }
                @PreDestroy
                void destroy() {
                    Jsr250.preDestroy(delegate);
                }
            };
        } else {
            Set<Dependency<?>> mutableDeps = Sets.newHashSet();
            for (InjectionPoint ip : injectionPoints) {
                mutableDeps.addAll(ip.getDependencies());
            }
            final Set<Dependency<?>> dependencies = ImmutableSet.copyOf(mutableDeps);
            return new ProviderWithDependencies<T>() {
                @SuppressWarnings("unused")
                @com.google.inject.Inject
                void initialize(Injector injector) {
                    injector.injectMembers(delegate);
                }

                @Override
                public Set<Dependency<?>> getDependencies() {
                    return dependencies;
                }

                @Override
                public T get() {
                    return delegate.get();
                }

                @Override
                public String toString() {
                    return "guicified(" + delegate + ")";
                }
                @PreDestroy
                void destroy() {
                    Jsr250.preDestroy(delegate);
                }
            };
        }
    }
}
