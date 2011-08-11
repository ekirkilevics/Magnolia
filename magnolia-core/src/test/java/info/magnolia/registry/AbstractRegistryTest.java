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
package info.magnolia.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * @version $Id$
 */
public class AbstractRegistryTest {
    @Test
    public void testUnregister() throws RegistrationException{
        // GIVEN
        String id2Unregister = "onlyOneToRemove";
        final DummyRegistry rendererRegistry = new DummyRegistry();
        DummyProvider DummyProvider = mock(DummyProvider.class);
        when(DummyProvider.getId()).thenReturn(id2Unregister);
        rendererRegistry.register(DummyProvider);

        // WHEN
        rendererRegistry.unregister(id2Unregister);

        // THEN
        assertEquals(0, rendererRegistry.getProviders().values().size());
    }
    @Test
    public void testUnregisterAndRegister() throws RegistrationException {
        // GIVEN
        String rendererId = "onlyOneToRemove";
        final DummyRegistry rendererRegistry = new DummyRegistry();
        DummyProvider DummyProvider = mock(DummyProvider.class);
        when(DummyProvider.getId()).thenReturn(rendererId);
        rendererRegistry.register(DummyProvider);

        List<String> idsToRemove = new ArrayList<String>();
        idsToRemove.add(rendererId);

        DummyProvider rp1 = mock(DummyProvider.class);
        when(rp1.getId()).thenReturn("rp1");
        DummyProvider rp2 = mock(DummyProvider.class);
        when(rp2.getId()).thenReturn("rp2");
        rendererRegistry.register(rp1);
        rendererRegistry.register(rp2);

        List<DummyProvider> DummyProviders = new ArrayList<DummyProvider>();
        DummyProviders.add(DummyProvider);

        // WHEN
        Set<String> idsOfNewRegisteredProviders = rendererRegistry.unregisterAndRegister(idsToRemove, DummyProviders);

        // THEN
        assertTrue(idsOfNewRegisteredProviders.contains(rendererId));
        assertEquals(1, idsOfNewRegisteredProviders.size());
    }
    @Test
    public void testRegisteringSameProviderMultipleTimesOverwritesOldSetting() throws RegistrationException {
        // GIVEN
        String providerId = "toBeOverwritte";
        final DummyRegistry rendererRegistry = new DummyRegistry();
        DummyProvider initialProvider = mock(DummyProvider.class);
        when(initialProvider.getId()).thenReturn(providerId);
        when(initialProvider.getDefinition()).thenReturn("initialDefinition");
        rendererRegistry.register(initialProvider);
        DummyProvider newProvider = mock(DummyProvider.class);
        when(newProvider.getId()).thenReturn(providerId);
        when(newProvider.getDefinition()).thenReturn("newDefinition");
        rendererRegistry.register(newProvider);

        // WHEN
        String result = rendererRegistry.get(providerId);

        // THEN
        assertEquals("newDefinition", result);
    }
    @Test
    public void testGetRenderer() throws RegistrationException {
        // GIVEN
        String rendererId = "test";
        DummyProvider DummyProvider = mock(DummyProvider.class);
        String testString = "test";
        when(DummyProvider.getId()).thenReturn(rendererId);
        when(DummyProvider.getDefinition()).thenReturn(testString);
        final DummyRegistry rendererRegistry = new DummyRegistry();
        rendererRegistry.register(DummyProvider);

        // WHEN
        String result = rendererRegistry.get(rendererId);

        // THEN
        assertEquals(testString, result);
    }

    @Test(expected = RegistrationException.class)
    public void testGetStringThrowsExceptionIfIdIsNotRegistered() throws RegistrationException {
        // GIVEN
        final DummyRegistry rendererRegistry = new DummyRegistry();
        final String rendererId = "notRegistered";

        // WHEN
        rendererRegistry.get(rendererId);

        // THEN - empty as rendererId has not been registered and hence we expect an endererRegistrationException...
    }
}
