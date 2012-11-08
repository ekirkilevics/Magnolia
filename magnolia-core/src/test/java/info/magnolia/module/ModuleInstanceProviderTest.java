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
package info.magnolia.module;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.util.Providers;
import info.magnolia.test.MgnlTestCase;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test case for {@link ModuleInstanceProvider}.
 *
 * @version $Id$
 */
public class ModuleInstanceProviderTest extends MgnlTestCase {

    public static class ModuleInstanceTestDummyModule {
    }

    @Test
    public void testThrowsExceptionWhenModuleInstanceIsNotAvailable() throws Exception {

        // Given
        ModuleRegistry moduleRegistry = mock(ModuleRegistry.class);
        when(moduleRegistry.getModuleInstance("dummy-module")).thenReturn(null);

        // When / Then
        Injector injector = createInjector(moduleRegistry);
        try {
            injector.getInstance(ModuleInstanceTestDummyModule.class);
            fail();
        } catch (ProvisionException e) {
            assertTrue(e.getMessage().contains("Module instance for module [dummy-module] not available, most likely because the module has not yet been started. Inject a Provider<> instead to get access to the module instance when it's available."));
        }
    }

    @Test
    public void testProvidesTheSameInstanceAsReturnedFromModuleRegistry() throws Exception {

        // Given
        ModuleInstanceTestDummyModule module = new ModuleInstanceTestDummyModule();
        ModuleRegistry moduleRegistry = mock(ModuleRegistry.class);
        when(moduleRegistry.getModuleInstance("dummy-module")).thenReturn(module);

        // When
        Injector injector = createInjector(moduleRegistry);
        ModuleInstanceTestDummyModule instance = injector.getInstance(ModuleInstanceTestDummyModule.class);

        // Then
        assertNotNull(instance);
        assertSame(module, instance);
    }

    private Injector createInjector(final ModuleRegistry moduleRegistry) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ModuleInstanceTestDummyModule.class).toProvider(Providers.guicify(new ModuleInstanceProvider<ModuleInstanceTestDummyModule>("dummy-module")));
                bind(ModuleRegistry.class).toInstance(moduleRegistry);
            }
        });
    }
}
