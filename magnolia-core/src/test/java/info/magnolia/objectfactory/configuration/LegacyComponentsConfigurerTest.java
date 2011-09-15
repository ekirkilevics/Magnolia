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
package info.magnolia.objectfactory.configuration;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

import org.junit.Test;

import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.module.model.ComponentDefinition;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.AbstractMagnoliaTestCase;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LegacyComponentsConfigurerTest extends AbstractMagnoliaTestCase {

    public static interface TestInterface {
    }

    public static class TestComponent implements TestInterface {
    }

    public static class TestComponentFactory implements ComponentFactory<Integer> {
        @Override
        public Integer newInstance() {
            return null;
        }
    }

    @Test
    public void testLegacy() throws IOException {

        // Given
        Properties properties = new Properties();
        properties.put(TestInterface.class.getName(), TestComponent.class.getName());
        properties.put(String.class.getName(), "data:/some/path");
        properties.put(Integer.class.getName(), TestComponentFactory.class.getName());
        properties.put("this-property", "will be skipped");
        properties.put(BigDecimal.class.getName(), "this will be skipped too");
        TestMagnoliaConfigurationProperties magnoliaConfigurationProperties = new TestMagnoliaConfigurationProperties(properties);

        ComponentProvider componentProvider = mock(ComponentProvider.class);
        when(componentProvider.getComponent(MagnoliaConfigurationProperties.class)).thenReturn(magnoliaConfigurationProperties);

        // When
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        LegacyComponentsConfigurer configurer = new LegacyComponentsConfigurer();
        configurer.doWithConfiguration(componentProvider, configuration);

        // Then
        assertEquals(3, configuration.getComponents().size());
        ImplementationConfiguration ic = (ImplementationConfiguration) configuration.getComponents().get(TestInterface.class);
        assertEquals(TestInterface.class, ic.getType());
        assertEquals(TestComponent.class, ic.getImplementation());
        assertEquals(ComponentDefinition.SCOPE_SINGLETON, ic.getScope());
        assertTrue(ic.isLazy());

        ProviderConfiguration pc = (ProviderConfiguration) configuration.getComponents().get(Integer.class);
        assertEquals(Integer.class, pc.getType());
        assertEquals(TestComponentFactory.class, pc.getProviderClass());
        assertEquals(ComponentDefinition.SCOPE_SINGLETON, pc.getScope());
        assertTrue(pc.isLazy());

        ConfiguredComponentConfiguration cc = (ConfiguredComponentConfiguration) configuration.getComponents().get(String.class);
        assertEquals(String.class, cc.getType());
        assertEquals("data", cc.getWorkspace());
        assertEquals("/some/path", cc.getPath());
        assertTrue(cc.isObserved());
        assertEquals(ComponentDefinition.SCOPE_SINGLETON, cc.getScope());
        assertTrue(cc.isLazy());

        assertEquals(1, configuration.getTypeMapping().size());
        Class<?> typeMappingTarget = configuration.getTypeMapping().get(TestInterface.class);
        assertEquals(typeMappingTarget, TestComponent.class);
    }
}
