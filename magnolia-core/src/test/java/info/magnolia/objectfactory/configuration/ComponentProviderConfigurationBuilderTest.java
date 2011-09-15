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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Provider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.ContextFactory;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.test.AbstractMagnoliaTestCase;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ComponentProviderConfigurationBuilderTest extends AbstractMagnoliaTestCase {

    private static List<String> events = new ArrayList<String>();
    private MockContext mockContext;

    public static class SimpleComponent {

        public SimpleComponent() {
            events.add("SimpleComponent");
        }

        @PostConstruct
        private void postConstruct() {
            events.add("SimpleComponent.postConstruct");
        }

        @PreDestroy
        private void preDestroy() {
            events.add("SimpleComponent.preDestroy");
        }
    }

    @Before
    @Override
    public void setUp() throws Exception {
        mockContext  = new MockWebContext();
        MgnlContext.setInstance(mockContext);

        try {
            final InputStream in = getClass().getResourceAsStream("/test-magnolia.properties");
            final TestMagnoliaConfigurationProperties cfg = new TestMagnoliaConfigurationProperties(in);
            SystemProperty.setMagnoliaConfigurationProperties(cfg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MockSession session = SessionTestUtil.createSession("config",
                "/foo/bar/component.class=" + SimpleComponent.class.getName(),
                "/foo/bar/componentWithProperty.class=" + SimpleComponentWithProperty.class.getName()
        );
        MockUtil.setSessionAndHierarchyManager(session);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        events.clear();
    }

    @Test
    public void testNonScopedComponent() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-simple.xml");
        assertTrue(events.isEmpty());
        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(4, events.size());
        assertNotSame(simpleComponent, simpleComponent2);
        assertTrue(events.contains("SimpleComponent"));
        assertTrue(events.contains("SimpleComponent.postConstruct"));
        componentProvider.destroy();
        // pre-destroy is not called for non-scoped components
        assertFalse(events.contains("SimpleComponent.preDestroy"));
    }

    @Test
    public void testLazySingletonScopedComponent() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-simple-singleton.xml");
        assertTrue(events.isEmpty());
        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        assertSame(simpleComponent, simpleComponent2);

        assertTrue(events.contains("SimpleComponent"));
        assertTrue(events.contains("SimpleComponent.postConstruct"));

        componentProvider.destroy();
        assertTrue(events.contains("SimpleComponent.preDestroy"));
    }

    @Test
    public void testEagerSingletonScopedComponent() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-simple-eagersingleton.xml");
        assertEquals(2, events.size());
        assertTrue(events.contains("SimpleComponent"));
        assertTrue(events.contains("SimpleComponent.postConstruct"));
        assertFalse(events.contains("SimpleComponent.preDestroy"));
        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        assertSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertTrue(events.contains("SimpleComponent.preDestroy"));
    }

    public static class SimpleComponentProvider implements Provider<SimpleComponent> {

        @Override
        public SimpleComponent get() {
            return new SimpleComponent();
        }

        @PostConstruct
        private void postConstruct() {
            events.add("SimpleComponentProvider.postConstruct");
        }

        @PreDestroy
        private void preDestroy() {
            events.add("SimpleComponentProvider.preDestroy");
        }
    }

    @Test
    public void testProvider() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-provider.xml");
        assertTrue(events.isEmpty());

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        assertTrue(events.contains("SimpleComponentProvider.postConstruct"));
        assertTrue(events.contains("SimpleComponent"));
        assertFalse(events.contains("SimpleComponent.postConstruct"));
        assertFalse(events.contains("SimpleComponent.preDestroy"));

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(4, events.size());
        assertFalse(events.contains("SimpleComponentProvider.preConstruct"));
        assertNotSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertFalse(events.contains("SimpleComponent.preDestroy"));
        assertFalse(events.contains("SimpleComponentProvider.preDestroy"));
    }

    @Test
    public void testSingletonScopedProvider() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-provider-singleton.xml");
        assertTrue(events.isEmpty());

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        assertTrue(events.contains("SimpleComponentProvider.postConstruct"));
        assertTrue(events.contains("SimpleComponent"));
        assertFalse(events.contains("SimpleComponent.postConstruct"));
        assertFalse(events.contains("SimpleComponent.preDestroy"));

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        assertSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertEquals(3, events.size());
        assertTrue(events.contains("SimpleComponent.preDestroy"));
        assertFalse(events.contains("SimpleComponentProvider.preDestroy"));
    }

    public static class SimpleComponentFactory implements ComponentFactory<SimpleComponent> {

        @Override
        public SimpleComponent newInstance() {
            return new SimpleComponent();
        }

        @PostConstruct
        private void postConstruct() {
            events.add("SimpleComponentFactory.postConstruct");
        }

        @PreDestroy
        private void preDestroy() {
            events.add("SimpleComponentFactory.preDestroy");
        }
    }

    @Test
    public void testComponentFactory() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-componentfactory.xml");
        assertTrue(events.isEmpty());

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        assertTrue(events.contains("SimpleComponentFactory.postConstruct"));
        assertTrue(events.contains("SimpleComponent"));
        assertFalse(events.contains("SimpleComponent.postConstruct"));
        assertFalse(events.contains("SimpleComponent.preDestroy"));

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(4, events.size());
        assertFalse(events.contains("SimpleComponentFactory.preConstruct"));
        assertNotSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertFalse(events.contains("SimpleComponent.preDestroy"));
        assertFalse(events.contains("SimpleComponentFactory.preDestroy"));
    }

    @Test
    public void testSingletonScopedComponentFactory() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-componentfactory-singleton.xml");
        assertTrue(events.isEmpty());

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        assertTrue(events.contains("SimpleComponentFactory.postConstruct"));
        assertTrue(events.contains("SimpleComponent"));
        assertFalse(events.contains("SimpleComponent.postConstruct"));
        assertFalse(events.contains("SimpleComponent.preDestroy"));

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        assertSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertEquals(3, events.size());
        assertTrue(events.contains("SimpleComponent.preDestroy"));
        assertFalse(events.contains("SimpleComponentFactory.preDestroy"));
    }

    @Test
    public void testConfigured() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-configured.xml");
        assertTrue(events.isEmpty());

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        assertTrue(events.contains("SimpleComponent"));
        assertTrue(events.contains("SimpleComponent.postConstruct"));
        assertFalse(events.contains("SimpleComponent.preDestroy"));

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(4, events.size());
        assertNotSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertEquals(4, events.size());
        assertFalse(events.contains("SimpleComponent.preDestroy"));
    }

    @Test
    public void testConfiguredSingleton() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-configured-singleton.xml");
        assertTrue(events.isEmpty());

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        assertTrue(events.contains("SimpleComponent"));
        assertTrue(events.contains("SimpleComponent.postConstruct"));
        assertFalse(events.contains("SimpleComponent.preDestroy"));

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        assertSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertEquals(3, events.size());
        assertTrue(events.contains("SimpleComponent.preDestroy"));
    }

    @Test
    public void testConfiguredEagerSingleton() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-configured-eagersingleton.xml");
        assertEquals(2, events.size());
        assertTrue(events.contains("SimpleComponent"));
        assertTrue(events.contains("SimpleComponent.postConstruct"));
        assertFalse(events.contains("SimpleComponent.preDestroy"));

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEquals(2, events.size());
        assertSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertEquals(3, events.size());
        assertTrue(events.contains("SimpleComponent.preDestroy"));
    }

    public static class SimpleComponentWithProperty extends SimpleComponent {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testObserved() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-observed.xml");
        assertTrue(events.isEmpty());

        SimpleComponentWithProperty simpleComponent = componentProvider.getComponent(SimpleComponentWithProperty.class);
        assertEquals(3, events.size());
        assertTrue(events.contains("SimpleComponent")); // There's two of these since the proxy also adds it
        assertTrue(events.contains("SimpleComponent.postConstruct"));
        assertFalse(events.contains("SimpleComponent.preDestroy"));

        SimpleComponentWithProperty simpleComponent2 = componentProvider.getComponent(SimpleComponentWithProperty.class);
        assertEquals(6, events.size()); // two more from the ctor, one is from the proxy
        assertNotSame(simpleComponent, simpleComponent2);

        // Make sure that its two completely different instances behind the proxies
        simpleComponent.setName("1");
        simpleComponent2.setName("2");
        assertEquals("1", simpleComponent.getName());
        assertEquals("2", simpleComponent2.getName());

        componentProvider.destroy();
        assertEquals(6, events.size());
        assertFalse(events.contains("SimpleComponent.preDestroy"));
    }

    private GuiceComponentProvider getComponentProvider(String resourcePath) {
        ComponentProviderConfigurationBuilder builder = new ComponentProviderConfigurationBuilder();
        ComponentProviderConfiguration configuration = builder.readConfiguration(Collections.singletonList("/info/magnolia/objectfactory/configuration/" + resourcePath), "main");
        configuration.registerImplementation(info.magnolia.content2bean.Content2BeanProcessor.class, info.magnolia.content2bean.impl.Content2BeanProcessorImpl.class);
        configuration.registerImplementation(info.magnolia.content2bean.Content2BeanTransformer.class, info.magnolia.content2bean.impl.Content2BeanTransformerImpl.class);
        configuration.registerImplementation(info.magnolia.content2bean.TransformationState.class, info.magnolia.content2bean.impl.TransformationStateImpl.class);
        configuration.registerImplementation(info.magnolia.content2bean.TypeMapping.class, info.magnolia.content2bean.impl.TypeMappingImpl.class);
        configuration.registerImplementation(ContextFactory.class, ContextFactory.class);
        configuration.registerInstance(SystemContext.class, mockContext);
        return new GuiceComponentProviderBuilder().withConfiguration(configuration).exposeGlobally().build();
    }
}
