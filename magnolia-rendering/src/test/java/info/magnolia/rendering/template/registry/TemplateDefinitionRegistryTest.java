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
package info.magnolia.rendering.template.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import info.magnolia.rendering.util.RegistrationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * @version $Id$
 */
public class TemplateDefinitionRegistryTest {

    @Test
    public void testUnregisterAndRegister() throws RegistrationException {
        // GIVEN
        String rendererId = "onlyOneToRemove";
        final TemplateDefinitionRegistry registry = new TemplateDefinitionRegistry();
        TemplateDefinitionProvider rendererProvider = mock(TemplateDefinitionProvider.class);
        when(rendererProvider.getId()).thenReturn(rendererId);
        registry.register(rendererProvider);

        List<String> idsToRemove = new ArrayList<String>();
        idsToRemove.add(rendererId);

        TemplateDefinitionProvider rp1 = mock(TemplateDefinitionProvider.class);
        when(rp1.getId()).thenReturn("rp1");
        TemplateDefinitionProvider rp2 = mock(TemplateDefinitionProvider.class);
        when(rp2.getId()).thenReturn("rp2");
        registry.register(rp1);
        registry.register(rp2);

        List<TemplateDefinitionProvider> rendererProviders = new ArrayList<TemplateDefinitionProvider>();
        rendererProviders.add(rendererProvider);

        // WHEN
        Set<String> idsOfNewRegisteredProviders = registry.unregisterAndRegister(idsToRemove, rendererProviders);

        // THEN
        assertTrue(idsOfNewRegisteredProviders.contains(rendererId));
        assertEquals(1, idsOfNewRegisteredProviders.size());
    }
}
