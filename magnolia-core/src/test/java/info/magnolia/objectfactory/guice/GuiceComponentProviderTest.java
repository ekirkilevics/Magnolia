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

import javax.inject.Singleton;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.context.ContextFactory;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.util.SessionTestUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ConfiguredComponentConfiguration;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import static org.junit.Assert.*;

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
        MgnlContext.setInstance(null);
        Components.setProvider(null);
    }

    @Test
    public void testGetComponentProvider() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        GuiceComponentProvider p = create(configuration, false);
        assertSame(p, p.getComponent(ComponentProvider.class));
    }

    @Test
    public void getComponentReturnsNullForUnconfiguredType() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        GuiceComponentProvider p = create(configuration, false);
        assertNull(p.getComponent(StringBuilder.class));
    }

    @Test
    public void testInstance() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        SingletonObject singletonObject = new SingletonObject();
        configuration.registerInstance(SingletonObject.class, singletonObject);

        GuiceComponentProvider p = create(configuration, false);
        assertSame(singletonObject, p.getComponent(SingletonObject.class));
        assertSame(singletonObject, p.getComponent(SingletonObject.class));
    }

    @Test
    public void testImplementation() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.registerImplementation(SingletonObject.class, SingletonObject.class);

        GuiceComponentProvider p = create(configuration, false);
        SingletonObject singletonObject = p.getComponent(SingletonObject.class);
        assertNotNull(singletonObject);
        assertSame(singletonObject, p.getComponent(SingletonObject.class));
    }

    @Test
    public void testConfigured() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.addConfigured(new ConfiguredComponentConfiguration<SingletonObject>(SingletonObject.class, "/foo/bar/singleton"));

        try {
            GuiceComponentProvider p = create(configuration, true);
            SingletonObject singletonObject = p.getComponent(SingletonObject.class);
            assertNotNull(singletonObject);
            assertSame(singletonObject, p.getComponent(SingletonObject.class));
            assertEquals("foobar", p.getComponent(SingletonObject.class).getName());
        } finally {
            Components.setProvider(null);
        }
    }

    @Test
    public void testObserved() {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.addConfigured(new ConfiguredComponentConfiguration<SingletonObject>(SingletonObject.class, ContentRepository.CONFIG, "/foo/bar/singleton", true));

        try {
            GuiceComponentProvider p = create(configuration, true);
            SingletonObject singletonObject = p.getComponent(SingletonObject.class);
            assertNotNull(singletonObject);
            assertSame(singletonObject, p.getComponent(SingletonObject.class));
            assertEquals("foobar", p.getComponent(SingletonObject.class).getName());
        } finally {
            Components.setProvider(null);
        }
    }

    private GuiceComponentProvider create(ComponentProviderConfiguration configuration, boolean exposeGlobally) {
        configuration.registerImplementation(info.magnolia.content2bean.Content2BeanProcessor.class, info.magnolia.content2bean.impl.Content2BeanProcessorImpl.class);
        configuration.registerImplementation(info.magnolia.content2bean.Content2BeanTransformer.class, info.magnolia.content2bean.impl.Content2BeanTransformerImpl.class);
        configuration.registerImplementation(info.magnolia.content2bean.TransformationState.class, info.magnolia.content2bean.impl.TransformationStateImpl.class);
        configuration.registerImplementation(info.magnolia.content2bean.TypeMapping.class, info.magnolia.content2bean.impl.TypeMappingImpl.class);
        configuration.registerImplementation(ContextFactory.class, ContextFactory.class);
        configuration.registerInstance(SystemContext.class, mockContext);

        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder().withConfiguration(configuration);
        if (exposeGlobally) {
            builder.exposeGlobally();
        }
        return builder.build();
    }

    @Singleton
    public static class OtherSingletonObject {
    }

    @Test
    public void testCreateChild() {

        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.registerImplementation(SingletonObject.class, SingletonObject.class);
        GuiceComponentProvider parent = create(configuration, false);

        ComponentProviderConfiguration childConfig = new ComponentProviderConfiguration();
        childConfig.registerImplementation(OtherSingletonObject.class, OtherSingletonObject.class);
        ComponentProvider child = parent.createChild(childConfig);

        assertNotNull(parent.getComponent(SingletonObject.class));
        assertNotNull(child.getComponent(SingletonObject.class));
        assertSame(parent.getComponent(SingletonObject.class), child.getComponent(SingletonObject.class));

        assertNull(parent.getComponent(OtherSingletonObject.class));
        assertNotNull(child.getComponent(OtherSingletonObject.class));
    }
}
