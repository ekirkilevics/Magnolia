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
package info.magnolia.rendering.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.rendering.renderer.Renderer;
import info.magnolia.rendering.renderer.registry.RendererProvider;
import info.magnolia.rendering.renderer.registry.RendererRegistrationException;
import info.magnolia.rendering.renderer.registry.RendererRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class RendererRegistryTest {

    private RendererRegistry rendererRegistry;

    @Before
    public void setUp() {
        rendererRegistry = new RendererRegistry();
    }

    @Test
    public void testUnregisterAndRegister() throws RendererRegistrationException {
        String rendererId = "onlyOneToRemove";
        RendererProvider rendererProvider = mock(RendererProvider.class);
        when(rendererProvider.getId()).thenReturn(rendererId);
        rendererRegistry.register(rendererId, rendererProvider);

        List<String> idsToRemove = new ArrayList<String>();
        idsToRemove.add(rendererId);

        RendererProvider rp1 = mock(RendererProvider.class);
        RendererProvider rp2 = mock(RendererProvider.class);
        rendererRegistry.register("rp1", rp1);
        rendererRegistry.register("rp2", rp2);

        List<RendererProvider> rendererProviders = new ArrayList<RendererProvider>();
        rendererProviders.add(rendererProvider);

        Set<String> idsOfNewRegisteredProviders = rendererRegistry.unregisterAndRegister(idsToRemove, rendererProviders);

        assertTrue(idsOfNewRegisteredProviders.contains(rendererId));
        assertEquals(1, idsOfNewRegisteredProviders.size());
    }

    @Test(expected=RendererRegistrationException.class)
    public void testUnregisterAndRegisterThrowsExceptionWhenTryingToRegisterExistingId() throws RendererRegistrationException {
        String rendererId = "onlyOneToRemove";
        RendererProvider rendererProvider = mock(RendererProvider.class);
        rendererRegistry.register(rendererId, rendererProvider);

        List<String> idsToRemove = new ArrayList<String>();
        idsToRemove.add(rendererId);

        RendererProvider rp1 = mock(RendererProvider.class);
        rendererRegistry.register("rp1", rp1);

        List<RendererProvider> rendererProviders = new ArrayList<RendererProvider>();
        rendererProviders.add(rendererProvider);
        rendererProviders.add(rp1);

        Set<String> idsOfNewRegisteredProviders = rendererRegistry.unregisterAndRegister(idsToRemove, rendererProviders);

        assertTrue(idsOfNewRegisteredProviders.contains(rendererId));
        assertEquals(1, idsOfNewRegisteredProviders.size());
    }
    @Test
    public void testGetRenderer() throws RendererRegistrationException {
        String rendererId = "test";
        RendererProvider rendererProvider = mock(RendererProvider.class);
        Renderer mockRenderer = mock(Renderer.class);
        when(rendererProvider.getRenderer()).thenReturn(mockRenderer);
        rendererRegistry.register(rendererId, rendererProvider);

        assertEquals(mockRenderer, rendererRegistry.getRenderer(rendererId));
    }

    @Test(expected = RendererRegistrationException.class)
    public void testGetRendererThrowsExceptionAfterUnregister() throws RendererRegistrationException {
        String rendererId = "test";
        RendererProvider rendererProvider = mock(RendererProvider.class);
        rendererRegistry.register(rendererId, rendererProvider);

        rendererRegistry.unregister(rendererId);

        // as rendererId has been unregistered we expect an RendererRegistrationException now...
        rendererRegistry.getRenderer(rendererId);
    }

}
