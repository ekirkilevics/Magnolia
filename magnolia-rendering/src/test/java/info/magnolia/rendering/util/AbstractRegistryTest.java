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
package info.magnolia.rendering.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.rendering.renderer.Renderer;
import info.magnolia.rendering.renderer.registry.RendererProvider;
import info.magnolia.rendering.renderer.registry.RendererRegistry;
import info.magnolia.rendering.util.RegistrationException;

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
        final RendererRegistry rendererRegistry = new RendererRegistry();
        RendererProvider rendererProvider = mock(RendererProvider.class);
        when(rendererProvider.getId()).thenReturn(id2Unregister);
        rendererRegistry.register(rendererProvider);

        // WHEN
        rendererRegistry.unregister(id2Unregister);

        // THEN
        assertEquals(0, rendererRegistry.getProviders().values().size());
    }

    @Test
    public void testUnregisterAndRegister() throws RegistrationException {
        // GIVEN
        String rendererId = "onlyOneToRemove";
        final RendererRegistry rendererRegistry = new RendererRegistry();
        RendererProvider rendererProvider = mock(RendererProvider.class);
        when(rendererProvider.getId()).thenReturn(rendererId);
        rendererRegistry.register(rendererProvider);

        List<String> idsToRemove = new ArrayList<String>();
        idsToRemove.add(rendererId);

        RendererProvider rp1 = mock(RendererProvider.class);
        when(rp1.getId()).thenReturn("rp1");
        RendererProvider rp2 = mock(RendererProvider.class);
        when(rp2.getId()).thenReturn("rp2");
        rendererRegistry.register(rp1);
        rendererRegistry.register(rp2);

        List<RendererProvider> rendererProviders = new ArrayList<RendererProvider>();
        rendererProviders.add(rendererProvider);

        // WHEN
        Set<String> idsOfNewRegisteredProviders = rendererRegistry.unregisterAndRegister(idsToRemove, rendererProviders);

        // THEN
        assertTrue(idsOfNewRegisteredProviders.contains(rendererId));
        assertEquals(1, idsOfNewRegisteredProviders.size());
    }

    @Test(expected=RegistrationException.class)
    public void testUnregisterAndRegisterThrowsExceptiongivenTryingToRegisterExistingId() throws RegistrationException {
        // GIVEN
        String rendererId = "onlyOneToRemove";
        RendererProvider rendererProvider = mock(RendererProvider.class);
        when(rendererProvider.getId()).thenReturn(rendererId);
        final RendererRegistry rendererRegistry = new RendererRegistry();
        rendererRegistry.register(rendererProvider);

        List<String> idsToRemove = new ArrayList<String>();
        idsToRemove.add(rendererId);

        RendererProvider rp1 = mock(RendererProvider.class);
        when(rp1.getId()).thenReturn("rp1");
        rendererRegistry.register(rp1);

        List<RendererProvider> rendererProviders = new ArrayList<RendererProvider>();
        rendererProviders.add(rendererProvider);
        rendererProviders.add(rp1);

        // WHEN
        Set<String> idsOfNewRegisteredProviders = rendererRegistry.unregisterAndRegister(idsToRemove, rendererProviders);

        // THEN
        assertTrue(idsOfNewRegisteredProviders.contains(rendererId));
        assertEquals(1, idsOfNewRegisteredProviders.size());
    }
    @Test
    public void testGetRenderer() throws RegistrationException {
        // GIVEN
        String rendererId = "test";
        RendererProvider rendererProvider = mock(RendererProvider.class);
        Renderer mockRenderer = mock(Renderer.class);
        when(rendererProvider.getId()).thenReturn(rendererId);
        when(rendererProvider.getDefinition()).thenReturn(mockRenderer);
        final RendererRegistry rendererRegistry = new RendererRegistry();
        rendererRegistry.register(rendererProvider);

        // WHEN
        Renderer result = rendererRegistry.get(rendererId);

        // THEN
        assertEquals(mockRenderer, result);
    }

    @Test(expected = RegistrationException.class)
    public void testGetRendererThrowsExceptionIfIdIsNotRegistered() throws RegistrationException {
        // GIVEN
        final RendererRegistry rendererRegistry = new RendererRegistry();
        final String rendererId = "notRegistered";

        // WHEN
        rendererRegistry.get(rendererId);

        // THEN - empty as rendererId has not been registered and hence we expect an RendererRegistrationException...
    }
}
