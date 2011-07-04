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
import java.util.logging.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Stage;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;


/**
 * Bridges a Guice Injector to another Injector by adding bindings for all explicit bindings in the injector being
 * used as parent. Bindings in the parent injector which will be configured locally are skipped.
 *
 * @version $Id$
 */
public class GuiceParentBindingsModule extends AbstractModule {

    private Injector parentInjector;
    private ComponentProviderConfiguration configuration;

    public GuiceParentBindingsModule(Injector parentInjector, ComponentProviderConfiguration configuration) {
        this.parentInjector = parentInjector;
        this.configuration = configuration;
    }

    @Override
    protected void configure() {

        for (Map.Entry<Key<?>, Binding<?>> entry : parentInjector.getBindings().entrySet()) {
            Key<?> key = entry.getKey();
            if (Key.get(ComponentProvider.class).equals(key)) {
                continue;
            }
            if (Key.get(Injector.class).equals(key)) {
                continue;
            }
            if (Key.get(Stage.class).equals(key)) {
                continue;
            }
            if (Key.get(Logger.class).equals(key)) {
                continue;
            }
            if (configuration.hasConfigFor(key.getTypeLiteral().getRawType())) {
                System.out.println("Skipping parent binding " + key.getTypeLiteral().getRawType());
                continue;
            }
            final Provider<?> provider = entry.getValue().getProvider();
            bind(key).toProvider(new Provider() {
                @Override
                public Object get() {
                    return provider.get();
                }
            });
        }
    }
}
