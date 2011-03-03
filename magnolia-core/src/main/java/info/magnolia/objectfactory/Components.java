/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
 * Entry point to a ComponentProvider. The instance to be used must be set using {@link #setProvider(ComponentProvider)}.
 * There should not be any good reason to change this at runtime.
 *
 * @see info.magnolia.cms.servlets.MgnlServletContextListener
 * @see ComponentProvider
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class Components {
    private static ComponentProvider componentProvider = new NullComponentProvider();

    /**
     * Are you sure you really need to do this ?
     * @see info.magnolia.cms.servlets.MgnlServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public static void setProvider(ComponentProvider provider) {
        componentProvider = provider;
    }

    /**
     * @deprecated since 5.0, use IoC. If you really need to look up a component, then use {@link #getComponent(Class)}
     * Additionally, it should not be up to the client to decide whether this component is a singleton or not.
     */
    public static <T> T getSingleton(Class<T> type) {
        return getComponent(type);
    }

    /**
     * Convenience method for retrieving a component out of the {@link ComponentProvider}.
     * Consider using IoC instead.
     */
    public static <T> T getComponent(Class<T> type) {
        return getComponentProvider().getComponent(type);
    }

    public static ComponentProvider getComponentProvider() {
        ComponentProvider scoped = scopes.get();
        if (scoped != null)
            return scoped;
        return componentProvider;
    }

    public static void pushScope(ComponentProvider scope) {
        if (scopes.get() != null)
            throw new IllegalStateException("Only one additional scope is supported at this time");
        scopes.set(scope);
    }

    public static void popScope(ComponentProvider scope) {
        scopes.remove();
    }

    private static ThreadLocal<ComponentProvider> scopes = new ThreadLocal<ComponentProvider>();

    private static class NullComponentProvider implements ComponentProvider {
        public <C> Class<? extends C> getImplementation(Class<C> type) throws ClassNotFoundException {
            throw new IllegalStateException("No ComponentProvider has been set yet, something must have gone terribly wrong at startup.");
        }

        public <T> T getComponent(Class<T> type) {
            throw new IllegalStateException("No ComponentProvider has been set yet, something must have gone terribly wrong at startup.");
        }

        public <T> T getSingleton(Class<T> type) {
            throw new IllegalStateException("No ComponentProvider has been set yet, something must have gone terribly wrong at startup.");
        }

        public <T> T newInstance(Class<T> type) {
            throw new IllegalStateException("No ComponentProvider has been set yet, something must have gone terribly wrong at startup.");
        }
    }
}
