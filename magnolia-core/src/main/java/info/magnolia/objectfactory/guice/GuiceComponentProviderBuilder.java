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

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Stage;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.SessionScoped;
import com.google.inject.util.Modules;
import com.mycila.inject.jsr250.Jsr250;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;


/**
 * Builder for creating a GuiceComponentProvider.
 *
 * @version $Id$
 */
public class GuiceComponentProviderBuilder {

    private ComponentProviderConfiguration configuration;
    private boolean exposeGlobally;
    private GuiceComponentProvider parent;
    private List<Module> extraModules = new ArrayList<Module>();
    private Stage stage;

    public GuiceComponentProviderBuilder exposeGlobally() {
        this.exposeGlobally = true;
        return this;
    }

    public GuiceComponentProviderBuilder withConfiguration(ComponentProviderConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public GuiceComponentProviderBuilder withParent(GuiceComponentProvider parent) {
        this.parent = parent;
        return this;
    }

    public GuiceComponentProviderBuilder inStage(Stage stage) {
        this.stage = stage;
        return this;
    }

    public void addModule(Module module) {
        this.extraModules.add(module);
    }

    public GuiceComponentProvider build() {

        final GuiceComponentProvider componentProvider = new GuiceComponentProvider(configuration, parent);
        if (exposeGlobally) {
            Components.setProvider(componentProvider);
        }

        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                // requireExplicitBindings is switched off because Guice internally creates jit bindings and those are called for @PreDestroy, which fails if this is turned on
//                binder().requireExplicitBindings();
                bind(ComponentProvider.class).toInstance(componentProvider);
                install(new GuiceComponentProviderModule(configuration));
                for (Module extraModule : extraModules) {
                    binder().install(extraModule);
                }

                // We don't need to register these providers at every level, would be enough to do it in the top parent
                // TODO: maybe we should throw CreationException in these providers, plus what happens on an Exception, does it seep through or is it wrapped?
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
                bindScope(RequestScoped.class, MagnoliaServletScopes.REQUEST);
                bindScope(SessionScoped.class, MagnoliaServletScopes.SESSION);

                try {
                    bind(Class.forName("com.mycila.inject.jsr250.Jsr250KeyProvider"));
                    bind(Class.forName("com.mycila.inject.jsr250.Jsr250PostConstructHandler"));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                install(Jsr250.newJsr250Module());
            }
        };

        Stage stageToUse;
        if (parent != null) {
            stageToUse = stage != null ? stage : parent.getInjector().getInstance(Stage.class);
            GuiceParentBindingsModule parentBindingsModule = new GuiceParentBindingsModule(parent.getInjector());
            module = Modules.override(parentBindingsModule).with(module);
        } else {
            stageToUse = stage != null ? stage : Stage.PRODUCTION;
        }

        Injector injector = Guice.createInjector(stageToUse, module);

        return (GuiceComponentProvider) injector.getInstance(ComponentProvider.class);
    }
}
