/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.objectfactory.guice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.SessionScoped;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

/**
 * Registers request and session scopes and providers for accessing MgnlContext.
 *
 * @version $Id$
 */
public class GuiceContextAndScopesComposer extends AbstractGuiceComponentComposer {

    @Override
    protected void configure() {

        // We don't need to register these providers at every level, would be enough to do it in the top parent
        bind(Context.class).toProvider(new Provider<Context>() {
            @Override
            public Context get() {
                return MgnlContext.getInstance();
            }
        });
        bind(WebContext.class).toProvider(new Provider<WebContext>() {
            @Override
            public WebContext get() {
                return MgnlContext.getWebContext();
            }
        });
        bind(AggregationState.class).toProvider(new Provider<AggregationState>() {
            @Override
            public AggregationState get() {
                return MgnlContext.getAggregationState();
            }
        });
        bind(HttpSession.class).toProvider(new Provider<HttpSession>() {
            @Override
            public HttpSession get() {
                return MgnlContext.getWebContext().getRequest().getSession();
            }
        });
        bind(HttpServletRequest.class).toProvider(new Provider<HttpServletRequest>() {
            @Override
            public HttpServletRequest get() {
                return MgnlContext.getWebContext().getRequest();
            }
        });
        bind(HttpServletResponse.class).toProvider(new Provider<HttpServletResponse>() {
            @Override
            public HttpServletResponse get() {
                return MgnlContext.getWebContext().getResponse();
            }
        });

        // But the scopes need to be registered at every level
        bindScope(RequestScoped.class, MagnoliaServletScopes.REQUEST);
        bindScope(SessionScoped.class, MagnoliaServletScopes.SESSION);
    }
}
