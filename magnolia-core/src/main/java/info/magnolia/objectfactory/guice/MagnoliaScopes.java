/**
 * This file Copyright (c) 2011-2012 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Scopes;

/**
 * Servlet scopes that use WebContext to get request and session.
 *
 * @version $Id$
 */
public class MagnoliaScopes {

    private MagnoliaScopes() {
    }

    public static final Scope LAZY_SINGLETON = new LazySingletonScope();

    /**
     * HTTP servlet request scope.
     */
    public static final Scope LOCAL = new LocalScope();

    /**
     * HTTP session scope.
     */
    public static final Scope SESSION = new SessionScope();

    /**
     * Scope for lazy singletons.
     *
     * @version $Id$
     * @see info.magnolia.objectfactory.annotation.LazySingleton
     */
    public static class LazySingletonScope implements Scope {

        @Override
        public <T> Provider<T> scope(Key<T> key, Provider<T> creator) {
            return Scopes.SINGLETON.scope(key, creator);
        }

        @Override
        public String toString() {
            return "MagnoliaScopes.LAZY_SINGLETON";
        }
    }

    /**
     * A sentinel attribute value representing null.
     */
    enum NullObject {
        INSTANCE
    }

    /**
     * Scope for object local to the current request.
     *
     * @version $Id$
     * @see info.magnolia.objectfactory.annotation.LocalScoped
     */
    public static class LocalScope implements Scope {

        @Override
        public <T> Provider<T> scope(Key<T> key, final Provider<T> creator) {
            final String name = key.toString();
            return new Provider<T>() {
                @Override
                public T get() {

                    HttpServletRequest request = getRequest();

                    if (request == null) {
                        return null;
                    }

                    synchronized (request) {
                        Object obj = request.getAttribute(name);
                        if (NullObject.INSTANCE == obj) {
                            return null;
                        }
                        @SuppressWarnings("unchecked")
                        T t = (T) obj;
                        if (t == null) {
                            t = creator.get();
                            request.setAttribute(name, (t != null) ? t : NullObject.INSTANCE);
                        }
                        return t;
                    }
                }

                @Override
                public String toString() {
                    return String.format("%s[%s]", creator, LOCAL);
                }
            };
        }

        @Override
        public String toString() {
            return "MagnoliaScopes.LOCAL";
        }
    }

    /**
     * Scope for object local to the current session.
     *
     * @version $Id$
     * @see info.magnolia.objectfactory.annotation.SessionScoped
     */
    public static class SessionScope implements Scope {

        @Override
        public <T> Provider<T> scope(Key<T> key, final Provider<T> creator) {
            final String name = key.toString();
            return new Provider<T>() {
                @Override
                public T get() {

                    HttpSession session = getSession();

                    synchronized (session) {
                        Object obj = session.getAttribute(name);
                        if (NullObject.INSTANCE == obj) {
                            return null;
                        }
                        @SuppressWarnings("unchecked")
                        T t = (T) obj;
                        if (t == null) {
                            t = creator.get();
                            session.setAttribute(name, (t != null) ? t : NullObject.INSTANCE);
                        }
                        return t;
                    }
                }

                @Override
                public String toString() {
                    return String.format("%s[%s]", creator, SESSION);
                }
            };
        }

        @Override
        public String toString() {
            return "MagnoliaScopes.SESSION";
        }
    }

    private static HttpServletRequest getRequest() {
        WebContext webContext;
        try {
            webContext = MgnlContext.getWebContextOrNull();
            // when injecting request to objects outside of the request scope (e.g. on destroy)
            if (webContext == null) {
                return null;
            }
        } catch (IllegalStateException e) {
            throw new OutOfScopeException("Cannot access scoped object." +
                    " MgnlContext does not have a WebContext set, this is most likely" +
                    " because we are not currently processing a HTTP request.", e);
        }
        HttpServletRequest request = webContext.getRequest();
        if (request == null) {
            throw new OutOfScopeException("Cannot access scoped object." +
                    " MgnlContext does not have a HttpServletRequest set, this is most likely" +
            " because we are not currently processing a HTTP request.");
        }
        return request;
    }

    private static HttpSession getSession() {
        try {
            return getRequest().getSession();
        } catch (IllegalStateException e) {
            throw new OutOfScopeException("Cannot access scoped object." +
                    " A session is not available and a new one could not be created," +
                    " likely because the response has already been committed.", e);
        }
    }
}
