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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

/**
 * Servlet scopes that use WebContext to get request and session. Based on {@link com.google.inject.servlet.ServletScopes}.
 *
 * @version $Id$
 */
public class MagnoliaServletScopes {

    private MagnoliaServletScopes() {
    }

    /**
     * A sentinel attribute value representing null.
     */
    enum NullObject {
        INSTANCE
    }

    /**
     * HTTP servlet request scope.
     */
    public static final Scope REQUEST = new Scope() {
        @Override
        public <T> Provider<T> scope(Key<T> key, final Provider<T> creator) {
            final String name = key.toString();
            return new Provider<T>() {
                @Override
                public T get() {

                    HttpServletRequest request = getRequest();

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

                public String toString() {
                    return String.format("%s[%s]", creator, REQUEST);
                }
            };
        }

        public String toString() {
            return "MagnoliaServletScopes.REQUEST";
        }
    };

    /**
     * HTTP session scope.
     */
    public static final Scope SESSION = new Scope() {
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

                public String toString() {
                    return String.format("%s[%s]", creator, SESSION);
                }
            };
        }

        public String toString() {
            return "MagnoliaServletScopes.SESSION";
        }
    };

    private static HttpServletRequest getRequest() {
        WebContext webContext;
        try {
            webContext = MgnlContext.getWebContext();
        } catch (IllegalStateException e) {
            throw new OutOfScopeException("Cannot access scoped object." +
                    " MgnlContext does not a WebContext set, this is most likely " +
                    "because we are not currently processing an HTTP request.", e);
        }
        return webContext.getRequest();
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
