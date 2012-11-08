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
package info.magnolia.objectfactory.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
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
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ComponentProviderConfigurationBuilderTest extends AbstractMagnoliaTestCase {

    private static Deque<String> events = new LinkedList<String>();
    private MockContext mockContext;

    public static void addEvent(String event) {
        events.addLast(event);
    }

    public static void assertNoMoreEvents() {
        assertTrue("No more events expected, was: " + events, events.isEmpty());
    }

    public static void assertEvent(String event) {
        if (!events.isEmpty() && events.peekFirst().equals(event)) {
            events.removeFirst();
        } else {
            fail("Expected event " + event);
        }
    }

    public static class SimpleComponent {

        public SimpleComponent() {
            addEvent("SimpleComponent");
        }
    }

    @Before
    @Override
    public void setUp() throws Exception {
        mockContext = new MockWebContext();
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
        ComponentsTestUtil.clear();
    }

    @Test
    public void testNonScopedComponent() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-simple.xml");
        assertNoMoreEvents();

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEvent("SimpleComponent");
        assertNoMoreEvents();

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEvent("SimpleComponent");
        assertNoMoreEvents();
        assertNotSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertNoMoreEvents();
    }

    @Test
    public void testLazySingletonScopedComponent() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-simple-singleton.xml");
        assertNoMoreEvents();

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEvent("SimpleComponent");
        assertNoMoreEvents();

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertNoMoreEvents();
        assertSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertNoMoreEvents();
    }

    @Test
    public void testEagerSingletonScopedComponent() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-simple-eagersingleton.xml");
        assertEvent("SimpleComponent");
        assertNoMoreEvents();

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertNoMoreEvents();

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertNoMoreEvents();
        assertSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertNoMoreEvents();
    }

    public static class SimpleComponentProvider implements Provider<SimpleComponent> {

        @Override
        public SimpleComponent get() {
            return new SimpleComponent();
        }
    }

    @Test
    public void testProvider() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-provider.xml");
        assertNoMoreEvents();

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEvent("SimpleComponent");
        assertNoMoreEvents();

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEvent("SimpleComponent");
        assertNoMoreEvents();
        assertNotSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertNoMoreEvents();
    }

    @Test
    public void testSingletonScopedProvider() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-provider-singleton.xml");
        assertNoMoreEvents();

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEvent("SimpleComponent");
        assertNoMoreEvents();

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertNoMoreEvents();
        assertSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertNoMoreEvents();
    }

    public static class SimpleComponentFactory implements ComponentFactory<SimpleComponent> {

        @Override
        public SimpleComponent newInstance() {
            return new SimpleComponent();
        }
    }

    @Test
    public void testComponentFactory() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-componentfactory.xml");
        assertNoMoreEvents();

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEvent("SimpleComponent");
        assertNoMoreEvents();

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEvent("SimpleComponent");
        assertNoMoreEvents();
        assertNotSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertNoMoreEvents();
    }

    @Test
    public void testSingletonScopedComponentFactory() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-componentfactory-singleton.xml");
        assertNoMoreEvents();

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEvent("SimpleComponent");
        assertNoMoreEvents();

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertNoMoreEvents();
        assertSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertNoMoreEvents();
    }

    @Test
    public void testConfigured() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-configured.xml");
        assertNoMoreEvents();

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEvent("SimpleComponent");
        assertNoMoreEvents();

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertEvent("SimpleComponent");
        assertNoMoreEvents();
        assertNotSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertNoMoreEvents();
    }

    @Test
    public void testConfiguredSingleton() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-configured-singleton.xml");
        assertNoMoreEvents();

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertEvent("SimpleComponent");
        assertNoMoreEvents();

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertNoMoreEvents();
        assertSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertNoMoreEvents();
    }

    @Test
    public void testConfiguredEagerSingleton() {
        GuiceComponentProvider componentProvider = getComponentProvider("test-components-configured-eagersingleton.xml");
        assertEvent("SimpleComponent");
        assertNoMoreEvents();

        SimpleComponent simpleComponent = componentProvider.getComponent(SimpleComponent.class);
        assertNoMoreEvents();

        SimpleComponent simpleComponent2 = componentProvider.getComponent(SimpleComponent.class);
        assertNoMoreEvents();
        assertSame(simpleComponent, simpleComponent2);

        componentProvider.destroy();
        assertNoMoreEvents();
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
        assertNoMoreEvents();

        SimpleComponentWithProperty simpleComponent = componentProvider.getComponent(SimpleComponentWithProperty.class);
        assertEvent("SimpleComponent"); // There's two of these since the proxy also adds it
        assertEvent("SimpleComponent");
        assertNoMoreEvents();

        SimpleComponentWithProperty simpleComponent2 = componentProvider.getComponent(SimpleComponentWithProperty.class);
        assertEvent("SimpleComponent"); // There's two of these since the proxy also adds it
        assertEvent("SimpleComponent");
        assertNoMoreEvents();

        // Make sure that its two completely different instances behind the proxies
        simpleComponent.setName("1");
        simpleComponent2.setName("2");
        assertEquals("1", simpleComponent.getName());
        assertEquals("2", simpleComponent2.getName());

        componentProvider.destroy();
        assertNoMoreEvents();
    }

    private GuiceComponentProvider getComponentProvider(String resourcePath) {
        ComponentProviderConfigurationBuilder builder = new ComponentProviderConfigurationBuilder();
        ComponentProviderConfiguration configuration = builder.readConfiguration(Collections.singletonList("/info/magnolia/objectfactory/configuration/" + resourcePath), "main");
        configuration.registerImplementation(info.magnolia.jcr.node2bean.Node2BeanProcessor.class, info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl.class);
        configuration.registerImplementation(info.magnolia.jcr.node2bean.Node2BeanTransformer.class, info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl.class);
        configuration.registerImplementation(info.magnolia.jcr.node2bean.TransformationState.class, info.magnolia.jcr.node2bean.impl.TransformationStateImpl.class);
        configuration.registerImplementation(info.magnolia.jcr.node2bean.TypeMapping.class, info.magnolia.jcr.node2bean.impl.TypeMappingImpl.class);
        configuration.registerImplementation(ContextFactory.class, ContextFactory.class);
        configuration.registerInstance(SystemContext.class, mockContext);
        return new GuiceComponentProviderBuilder().withConfiguration(configuration).exposeGlobally().build();
    }
}
