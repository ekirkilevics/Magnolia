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
package info.magnolia.objectfactory.guice;

import java.io.IOException;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mockrunner.mock.web.MockHttpServletRequest;
import info.magnolia.context.ContextFactory;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.module.model.ComponentDefinition;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.NoSuchComponentException;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ConfiguredComponentConfiguration;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.AbstractMagnoliaTestCase;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import static org.junit.Assert.*;

public class GuiceComponentProviderTest extends AbstractMagnoliaTestCase {

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

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockContext = MockUtil.initMockContext();
        MockSession session = SessionTestUtil.createSession("config",
                "/foo/bar/singleton.class=" + SingletonObject.class.getName(),
                "/foo/bar/singleton.name=foobar"
        );
        MockUtil.setSessionAndHierarchyManager(session);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        super.tearDown();
    }

    @Test
    public void testGetComponentProvider() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        GuiceComponentProvider p = createComponentProvider(configuration);
        assertSame(p, p.getComponent(ComponentProvider.class));
    }

    @Test (expected=NoSuchComponentException.class)
    public void getComponentThrowsExeptionForUnconfiguredType() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        GuiceComponentProvider p = createComponentProvider(configuration);
        p.getComponent(StringBuilder.class);
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
        configuration.addComponent(new ConfiguredComponentConfiguration<SingletonObject>(SingletonObject.class, "/foo/bar/singleton"));

        GuiceComponentProvider p = createComponentProviderWithContent2Bean(configuration, true);
        SingletonObject singletonObject = p.getComponent(SingletonObject.class);
        assertNotNull(singletonObject);
        assertNotSame(singletonObject, p.getComponent(SingletonObject.class));
        assertEquals("foobar", p.getComponent(SingletonObject.class).getName());
    }

    @Test
    public void testConfiguredInSingletonScope() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        ConfiguredComponentConfiguration<SingletonObject> componentConfiguration = new ConfiguredComponentConfiguration<SingletonObject>(SingletonObject.class, "/foo/bar/singleton");
        componentConfiguration.setScope(ComponentDefinition.SCOPE_SINGLETON);
        configuration.addComponent(componentConfiguration);

        GuiceComponentProvider p = createComponentProviderWithContent2Bean(configuration, true);
        SingletonObject singletonObject = p.getComponent(SingletonObject.class);
        assertNotNull(singletonObject);
        assertSame(singletonObject, p.getComponent(SingletonObject.class));
        assertEquals("foobar", p.getComponent(SingletonObject.class).getName());
    }

    @Test
    public void testObserved() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        ConfiguredComponentConfiguration<SingletonObject> observed = new ConfiguredComponentConfiguration<SingletonObject>(SingletonObject.class, RepositoryConstants.CONFIG, "/foo/bar/singleton", true);
        observed.setScope(ComponentDefinition.SCOPE_SINGLETON);
        configuration.addComponent(observed);
        GuiceComponentProvider p = createComponentProviderWithContent2Bean(configuration, true);
        SingletonObject singletonObject = p.getComponent(SingletonObject.class);
        assertNotNull(singletonObject);
        assertSame(singletonObject, p.getComponent(SingletonObject.class));
        assertEquals("foobar", p.getComponent(SingletonObject.class).getName());
    }

    @Test
    public void testObservedInRequestScope() {

        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        ConfiguredComponentConfiguration<SingletonObject> observed = new ConfiguredComponentConfiguration<SingletonObject>(SingletonObject.class, RepositoryConstants.CONFIG, "/foo/bar/singleton", true);
        observed.setScope(ComponentDefinition.SCOPE_LOCAL);
        configuration.addComponent(observed);

        MockHttpServletRequest request = new MockHttpServletRequest();
        ((MockWebContext) MgnlContext.getWebContext()).setRequest(request);
        GuiceComponentProvider p = createComponentProviderWithContent2Bean(configuration, true);

        SingletonObject singletonObject = p.getComponent(SingletonObject.class);
        assertNotNull(singletonObject);
        assertSame(singletonObject, p.getComponent(SingletonObject.class));
        assertEquals("foobar", p.getComponent(SingletonObject.class).getName());

        request.clearAttributes();

        SingletonObject singletonObject2 = p.getComponent(SingletonObject.class);
        assertNotNull(singletonObject2);
        assertNotSame(singletonObject2, singletonObject);
        assertSame(singletonObject2, p.getComponent(SingletonObject.class));
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
        GuiceComponentProvider child = createChild(parent, childConfig);

        assertNotSame(parent, child);

        assertNotNull(parent.getComponent(SingletonObject.class));
        assertNotNull(child.getComponent(SingletonObject.class));
        assertSame(parent.getComponent(SingletonObject.class), child.getComponent(SingletonObject.class));

        try{
            parent.getComponent(OtherSingletonObject.class);
            fail("the parent should not know " + OtherSingletonObject.class);
        }
        catch(NoSuchComponentException e){
            // this is expected
        }

        assertNotNull(child.getComponent(OtherSingletonObject.class));
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
        configuration.registerImplementation(info.magnolia.jcr.node2bean.Node2BeanProcessor.class, info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl.class);
        configuration.registerImplementation(info.magnolia.jcr.node2bean.Node2BeanTransformer.class, info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl.class);
        configuration.registerImplementation(info.magnolia.jcr.node2bean.TransformationState.class, info.magnolia.jcr.node2bean.impl.TransformationStateImpl.class);
        configuration.registerImplementation(info.magnolia.jcr.node2bean.TypeMapping.class, info.magnolia.jcr.node2bean.impl.TypeMappingImpl.class);
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

    public GuiceComponentProvider createChild(GuiceComponentProvider parent, ComponentProviderConfiguration configuration) {
        return new GuiceComponentProviderBuilder().withConfiguration(configuration).withParent(parent).build();
    }
}
