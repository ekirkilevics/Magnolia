/**
 * This file Copyright (c) 2010-2012 Magnolia International
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

/**
 * Static access utility for the currently set {@link ComponentProvider}. The current {@link ComponentProvider} is set
 * during start-up and there should be little reason to change it at runtime.
 *
 * Since Magnolia 4.5, you are encouraged to use IoC, only in rare cases should you need to directly use this class.
 *
 * @version $Id$
 * @see ComponentProvider
 */
public class Components {

    private static volatile ComponentProvider componentProvider = new NullComponentProvider();
    private static ThreadLocal<ComponentProvider> threadLocalHierarchy = new ThreadLocal<ComponentProvider>();

    /**
     * Sets the current {@link ComponentProvider}.
     */
    public static void setComponentProvider(ComponentProvider provider) {
        componentProvider = provider;
    }

    /**
     * Returns the currently set {@link ComponentProvider}.
     */
    public static ComponentProvider getComponentProvider() {
        ComponentProvider scoped = threadLocalHierarchy.get();
        if (scoped != null) {
            return scoped;
        }
        return componentProvider;
    }

    public static void pushProvider(ComponentProvider provider) {
        if (threadLocalHierarchy.get() != null) {
            throw new IllegalStateException("Only one additional scope is supported");
        }
        threadLocalHierarchy.set(provider);
    }

    public static void popProvider() {
        threadLocalHierarchy.remove();
    }

    /**
     * Returns a component from the currently set {@link ComponentProvider}.
     *
     * @see ComponentProvider#getComponent(Class)
     * @deprecated since 4.5, use IoC to inject the component or use #getComponent(Class).
     */
    public static <T> T getSingleton(Class<T> type) {
        return getComponent(type);
    }

    public static <T> T newInstance(Class<T> type, Object... parameters) {
        return getComponentProvider().newInstance(type, parameters);
    }

    /**
     * Returns a component from the currently set {@link ComponentProvider}. Consider using IoC to inject the component instead.
     */
    public static <T> T getComponent(Class<T> type) {
        return getComponentProvider().getComponent(type);
    }

    private static class NullComponentProvider implements ComponentProvider {

        @Override
        public <C> Class<? extends C> getImplementation(Class<C> type) throws ClassNotFoundException {
            throw new IllegalStateException("No ComponentProvider has been set yet, something must have gone terribly wrong at startup.");
        }

        @Override
        public <T> T getComponent(Class<T> type) {
            throw new IllegalStateException("No ComponentProvider has been set yet, something must have gone terribly wrong at startup.");
        }

        @Override
        public <T> T getSingleton(Class<T> type) {
            throw new IllegalStateException("No ComponentProvider has been set yet, something must have gone terribly wrong at startup.");
        }

        public <T> T newInstance(Class<T> type) {
            throw new IllegalStateException("No ComponentProvider has been set yet, something must have gone terribly wrong at startup.");
        }

        @Override
        public <T> T newInstance(Class<T> type, Object... parameters) {
            throw new IllegalStateException("No ComponentProvider has been set yet, something must have gone terribly wrong at startup.");
        }

        @Override
        public <T> T newInstanceWithParameterResolvers(Class<T> type, ParameterResolver... parameters) {
            throw new IllegalStateException("No ComponentProvider has been set yet, something must have gone terribly wrong at startup.");
        }

        @Override
        public ComponentProvider getParent() {
            throw new IllegalStateException("No ComponentProvider has been set yet, something must have gone terribly wrong at startup.");
        }

    }
}
