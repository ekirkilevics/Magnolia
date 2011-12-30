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
package info.magnolia.rendering.template.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @version $Id$
 */
public class TemplateDefinitionRegistryTest {

    private static class SimpleTemplateDefinitionProvider implements TemplateDefinitionProvider {

        private String id;
        private TemplateDefinition templateDefinition;

        private SimpleTemplateDefinitionProvider(String id, TemplateDefinition templateDefinition) {
            this.id = id;
            this.templateDefinition = templateDefinition;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public TemplateDefinition getTemplateDefinition() throws RegistrationException {
            return templateDefinition;
        }
        @Override
        public String toString() {
            return "SimpleTemplateDefinitionProvider [id=" + id + ", templateDefinition=" + templateDefinition + "]";
        }
    }

    @Test
    public void testRegisterAndGetTemplateDefinition() throws RegistrationException {

        // GIVEN
        TemplateDefinitionRegistry registry = new TemplateDefinitionRegistry(null);
        ConfiguredTemplateDefinition definition = new ConfiguredTemplateDefinition();

        // WHEN
        registry.register(new SimpleTemplateDefinitionProvider("foobar", definition));

        // THEN
        assertSame(definition, registry.getTemplateDefinition("foobar"));
        assertEquals(1, registry.getTemplateDefinitions().size());
        assertSame(definition, registry.getTemplateDefinitions().iterator().next());
    }

    @Test
    public void testGetTemplateDefinitionsWithProviderReturningNullDefinition() {
        // GIVEN
        final TemplateDefinitionRegistry registry = new TemplateDefinitionRegistry(null);
        registry.register(new SimpleTemplateDefinitionProvider("returnsNullTemplateDefinition", null));

        // WHEN
        final Collection<TemplateDefinition> definitions = registry.getTemplateDefinitions();

        // THEN
        // make sure getTemplateDefinitions doesn't throw an exception but delivers all "valid" TemplateDefinitions - none in that case
        assertTrue(definitions.isEmpty());
    }

    @Test(expected = RegistrationException.class)
    public void testGetTemplateDefinitionThrowsOnBadId() throws RegistrationException {

        // GIVEN
        TemplateDefinitionRegistry registry = new TemplateDefinitionRegistry(null);

        // WHEN
        registry.getTemplateDefinition("nonExistingId");

        // THEN we get RegistrationException
    }

    @Test
    public void testGetTemplateDefinitionsIgnoresFailingProvider() {

        // GIVEN
        TemplateDefinitionRegistry registry = new TemplateDefinitionRegistry(null);
        registry.register(new TemplateDefinitionProvider() {
            @Override
            public String getId() {
                return null;
            }

            @Override
            public TemplateDefinition getTemplateDefinition() throws RegistrationException {
                throw new RegistrationException("failing provider");
            }
        });

        // WHEN
        Collection<TemplateDefinition> templateDefinitions = registry.getTemplateDefinitions();

        // THEN
        assertTrue(templateDefinitions.isEmpty());
    }

    @Test
    public void testUnregisterAndRegister() {
        // GIVEN
        String providerId = "onlyOneToRemove";
        final TemplateDefinitionRegistry registry = new TemplateDefinitionRegistry(null);
        TemplateDefinitionProvider provider = mock(TemplateDefinitionProvider.class);
        when(provider.getId()).thenReturn(providerId);
        registry.register(provider);

        List<String> idsToRemove = new ArrayList<String>();
        idsToRemove.add(providerId);

        TemplateDefinitionProvider rp1 = mock(TemplateDefinitionProvider.class);
        when(rp1.getId()).thenReturn("rp1");
        TemplateDefinitionProvider rp2 = mock(TemplateDefinitionProvider.class);
        when(rp2.getId()).thenReturn("rp2");
        registry.register(rp1);
        registry.register(rp2);

        List<TemplateDefinitionProvider> rendererProviders = new ArrayList<TemplateDefinitionProvider>();
        rendererProviders.add(provider);

        // WHEN
        Set<String> idsOfNewRegisteredProviders = registry.unregisterAndRegister(idsToRemove, rendererProviders);

        // THEN
        assertTrue(idsOfNewRegisteredProviders.contains(providerId));
        assertEquals(1, idsOfNewRegisteredProviders.size());
    }
}
