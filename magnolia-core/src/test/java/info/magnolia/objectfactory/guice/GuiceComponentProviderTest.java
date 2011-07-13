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
package info.magnolia.objectfactory.guice;

import java.io.IOException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.ContextFactory;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.jcr.util.SessionTestUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ConfiguredComponentConfiguration;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class GuiceComponentProviderTest {

    private MockContext mockContext;

    @Singleton
    public static class SingletonObject {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Before
    public void setUp() throws Exception {
        mockContext = MockUtil.initMockContext();
        MockSession session = SessionTestUtil.createSession("config",
                "/foo/bar/singleton.class=" + SingletonObject.class.getName(),
                "/foo/bar/singleton.name=foobar"
        );
        mockContext.addSession("config", session);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        SystemProperty.clear();
        MgnlContext.setInstance(null);
        Components.setProvider(null);
    }

    @Test
    public void testGetComponentProvider() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        GuiceComponentProvider p = createComponentProvider(configuration);
        assertSame(p, p.getComponent(ComponentProvider.class));
    }

    @Test
    public void getComponentReturnsNullForUnconfiguredType() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        GuiceComponentProvider p = createComponentProvider(configuration);
        assertNull(p.getComponent(StringBuilder.class));
    }

    @Test
    public void testInstance() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        SingletonObject singletonObject = new SingletonObject();
        configuration.registerInstance(SingletonObject.class, singletonObject);
        GuiceComponentProvider p = createComponentProvider(configuration);
        assertSame(singletonObject, p.getComponent(SingletonObject.class));
        assertSame(singletonObject, p.getComponent(SingletonObject.class));
    }

    @Test
    public void testImplementation() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.registerImplementation(SingletonObject.class, SingletonObject.class);
        GuiceComponentProvider p = createComponentProvider(configuration);
        SingletonObject singletonObject = p.getComponent(SingletonObject.class);
        assertNotNull(singletonObject);
        assertSame(singletonObject, p.getComponent(SingletonObject.class));
    }

    @Test
    public void testConfigured() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.addConfigured(new ConfiguredComponentConfiguration<SingletonObject>(SingletonObject.class, "/foo/bar/singleton"));

        GuiceComponentProvider p = createComponentProviderWithContent2Bean(configuration, true);
        SingletonObject singletonObject = p.getComponent(SingletonObject.class);
        assertNotNull(singletonObject);
        assertSame(singletonObject, p.getComponent(SingletonObject.class));
        assertEquals("foobar", p.getComponent(SingletonObject.class).getName());
    }

    @Test
    public void testObserved() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.addConfigured(new ConfiguredComponentConfiguration<SingletonObject>(SingletonObject.class, ContentRepository.CONFIG, "/foo/bar/singleton", true));
        GuiceComponentProvider p = createComponentProviderWithContent2Bean(configuration, true);
        SingletonObject singletonObject = p.getComponent(SingletonObject.class);
        assertNotNull(singletonObject);
        assertSame(singletonObject, p.getComponent(SingletonObject.class));
        assertEquals("foobar", p.getComponent(SingletonObject.class).getName());
    }

    @Singleton
    public static class OtherSingletonObject {
    }

    @Test
    public void testCreateChild() {

        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.registerImplementation(SingletonObject.class, SingletonObject.class);
        GuiceComponentProvider parent = createComponentProvider(configuration);

        ComponentProviderConfiguration childConfig = new ComponentProviderConfiguration();
        childConfig.registerImplementation(OtherSingletonObject.class, OtherSingletonObject.class);
        ComponentProvider child = parent.createChild(childConfig);

        assertNotSame(parent, child);

        assertNotNull(parent.getComponent(SingletonObject.class));
        assertNotNull(child.getComponent(SingletonObject.class));
        assertSame(parent.getComponent(SingletonObject.class), child.getComponent(SingletonObject.class));

        assertNull(parent.getComponent(OtherSingletonObject.class));
        assertNotNull(child.getComponent(OtherSingletonObject.class));
    }

    public interface SomeInterface {
    }

    @Singleton
    public static class LifecycleSuperClass implements SomeInterface {
        public int initialized;
        public int destroyed;

        @PostConstruct
        public void init() {
            initialized++;
        }

        @PreDestroy
        public void destroy() {
            destroyed++;
        }
    }

    @Test
    @Ignore
    public void testLifecycle() {

        // This fails because Guice creates an extra internal binding and Mycila destroys each of them
        // Issue filed with MycilaGuice https://code.google.com/p/mycila/issues/detail?id=31

        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.registerImplementation(SomeInterface.class, LifecycleSuperClass.class);
        GuiceComponentProvider p = createComponentProvider(configuration);
        LifecycleSuperClass component = (LifecycleSuperClass) p.getComponent(SomeInterface.class);
        assertEquals(1, component.initialized);
        assertEquals(0, component.destroyed);
        p.destroy();
        assertEquals(1, component.initialized);
        assertEquals(1, component.destroyed);
    }

    @Test
    public void destroyOfChildMustNotDestroyInParent() {

        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.registerImplementation(LifecycleSuperClass.class);
        GuiceComponentProvider parent = createComponentProvider(configuration);

        ComponentProviderConfiguration childConfig = new ComponentProviderConfiguration();
        GuiceComponentProvider child = parent.createChild(childConfig);

        LifecycleSuperClass component = parent.getComponent(LifecycleSuperClass.class);
        assertEquals(1, component.initialized);
        assertEquals(0, component.destroyed);
        child.destroy();
        assertEquals(1, component.initialized);
        assertEquals(0, component.destroyed);
        parent.destroy();
        assertEquals(1, component.initialized);
        assertEquals(1, component.destroyed);
    }

    @Singleton
    public static class SingletonWithPropertyDependencies {

        private String alpha;
        @Inject
        @Named("beta")
        private Provider<Boolean> beta;

        @Inject
        public SingletonWithPropertyDependencies(@Named("alpha") String alpha) {
            this.alpha = alpha;
        }

        public String getAlpha() {
            return alpha;
        }

        public Boolean getBeta() {
            return beta.get();
        }
    }

    @Test
    public void canAccessProperties() throws IOException {

        Properties properties = new Properties();
        properties.setProperty("alpha", "AAA");
        properties.setProperty("beta", "true");
        TestMagnoliaConfigurationProperties configurationProperties = new TestMagnoliaConfigurationProperties(properties);

        ComponentProviderConfiguration parentConfiguration = new ComponentProviderConfiguration();
        parentConfiguration.registerInstance(MagnoliaConfigurationProperties.class, configurationProperties);
        GuiceComponentProvider parent = new GuiceComponentProviderBuilder().withConfiguration(parentConfiguration).build();

        ComponentProviderConfiguration childConfiguration = new ComponentProviderConfiguration();
        childConfiguration.registerImplementation(SingletonWithPropertyDependencies.class);
        GuiceComponentProvider child = new GuiceComponentProviderBuilder().withConfiguration(childConfiguration).withParent(parent).build();

        SingletonWithPropertyDependencies component = child.getComponent(SingletonWithPropertyDependencies.class);
        assertEquals("AAA", component.getAlpha());
        assertTrue(component.getBeta());
    }

    private GuiceComponentProvider createComponentProviderWithContent2Bean(ComponentProviderConfiguration configuration, boolean exposeGlobally) {
        configuration.registerImplementation(info.magnolia.content2bean.Content2BeanProcessor.class, info.magnolia.content2bean.impl.Content2BeanProcessorImpl.class);
        configuration.registerImplementation(info.magnolia.content2bean.Content2BeanTransformer.class, info.magnolia.content2bean.impl.Content2BeanTransformerImpl.class);
        configuration.registerImplementation(info.magnolia.content2bean.TransformationState.class, info.magnolia.content2bean.impl.TransformationStateImpl.class);
        configuration.registerImplementation(info.magnolia.content2bean.TypeMapping.class, info.magnolia.content2bean.impl.TypeMappingImpl.class);
        configuration.registerImplementation(ContextFactory.class, ContextFactory.class);
        configuration.registerInstance(SystemContext.class, mockContext);
        return createComponentProvider(configuration, exposeGlobally);
    }

    private GuiceComponentProvider createComponentProvider(ComponentProviderConfiguration configuration) {
        return createComponentProvider(configuration, false);
    }

    private GuiceComponentProvider createComponentProvider(ComponentProviderConfiguration configuration, boolean exposeGlobally) {
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder().withConfiguration(configuration);
        if (exposeGlobally) {
            builder.exposeGlobally();
        }
        return builder.build();
    }
}
