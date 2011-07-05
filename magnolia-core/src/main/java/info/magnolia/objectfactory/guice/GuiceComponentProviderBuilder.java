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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
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

    // TODO should be inStage()
    public GuiceComponentProviderBuilder useStage(Stage stage) {
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
                binder().requireExplicitBindings();
                bind(ComponentProvider.class).toInstance(componentProvider);
                install(new GuiceComponentProviderModule(configuration));
                for (Module extraModule : extraModules) {
                    binder().install(extraModule);
                }
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
