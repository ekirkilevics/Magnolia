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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Stage;


/**
 * Bridges a Guice Injector to another Injector by adding bindings for all explicit bindings in the injector being
 * used as parent.
 *
 * @version $Id$
 */
public class GuiceParentBindingsModule extends AbstractModule {

    private Injector parentInjector;
    private Set<Key<?>> excluded = new HashSet<Key<?>>();

    public GuiceParentBindingsModule(Injector parentInjector) {
        this.parentInjector = parentInjector;
        exclude(Injector.class, Stage.class, Logger.class);
    }

    public void exclude(Key<?>... keys) {
        this.excluded.addAll(Arrays.asList(keys));
    }

    public void exclude(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            this.excluded.add(Key.get(clazz));
        }
    }

    @Override
    protected void configure() {

        Injector injector = parentInjector;
        do {
            Map<Key<?>, Binding<?>> explicitBindings = injector.getBindings();
            for (Map.Entry<Key<?>, Binding<?>> entry : explicitBindings.entrySet()) {
                Key<?> key = entry.getKey();
                if (!excluded.contains(key)) {
                    final Provider<?> provider = entry.getValue().getProvider();
                    // We wrap in a nop provider here to hide details like scope
                    bind(key).toProvider(new Provider() {
                        @Override
                        public Object get() {
                            return provider.get();
                        }
                    });
                }
            }
            injector = injector.getParent();
        } while (injector != null);
    }
}
