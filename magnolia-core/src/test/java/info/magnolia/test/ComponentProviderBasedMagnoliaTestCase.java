/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.test;

import java.util.ArrayList;
import java.util.Properties;

import org.junit.Before;

import com.google.inject.Stage;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.content2bean.Content2BeanProcessor;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeMapping;
import info.magnolia.content2bean.impl.Content2BeanProcessorImpl;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;
import info.magnolia.content2bean.impl.TransformationStateImpl;
import info.magnolia.content2bean.impl.TypeMappingImpl;
import info.magnolia.context.ContextFactory;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.init.AbstractMagnoliaConfigurationProperties;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.init.MagnoliaInitPaths;
import info.magnolia.init.PropertySource;
import info.magnolia.init.properties.AbstractPropertySource;
import info.magnolia.init.properties.ClasspathPropertySource;
import info.magnolia.init.properties.InitPathsPropertySource;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockWebContext;

/**
 * Abstract base class for tests that require a ComponentProvider to wire up components. Subclasses extend methods in
 * this class to configure the components it needs. This way it can add mocks.
 *
 * NOTE: do not use MockUtil.initMockContext() or any methods in ComponentsTestUtil since they will replace the
 * ComponentProvider set in Components.
 *
 * @version $Id: MgnlTestCase.java 48658 2011-08-25 15:26:53Z tmattsson $
 */
public abstract class ComponentProviderBasedMagnoliaTestCase extends AbstractMagnoliaTestCase {

    private GuiceComponentProvider componentProvider;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        ComponentProviderConfiguration components = new ComponentProviderConfiguration();
        customizeComponents(components);
        builder.withConfiguration(components);
        builder.inStage(Stage.PRODUCTION);
        builder.exposeGlobally();
        componentProvider = builder.build();

        initContext();
    }

    @Override
    public void tearDown() {
        componentProvider.destroy();
        super.tearDown();
    }

    protected void initContext() {
        // We can't use MockUtil.initMockContext() since it destroys the ComponentProvider set in Components
//        MockUtil.initMockContext();
        MockContext ctx = new MockWebContext();
        MgnlContext.setInstance(ctx);
    }

    protected void customizeProperties(Properties properties) throws Exception {
    }

    protected void customizeComponents(ComponentProviderConfiguration components) throws Exception {
        TestMagnoliaInitPaths initPaths = new TestMagnoliaInitPaths();
        MagnoliaConfigurationProperties configurationProperties = new FakeMagnoliaConfigurationProperties(initPaths);
        SystemProperty.setMagnoliaConfigurationProperties(configurationProperties);
        components.registerInstance(MagnoliaInitPaths.class, initPaths);
        components.registerInstance(MagnoliaConfigurationProperties.class, configurationProperties);

        components.registerImplementation(ContextFactory.class, ContextFactory.class);
        components.registerImplementation(SystemContext.class, MockContext.class);

        components.registerImplementation(Content2BeanProcessor.class, Content2BeanProcessorImpl.class);
        components.registerImplementation(TypeMapping.class, TypeMappingImpl.class);
        components.registerImplementation(TransformationState.class, TransformationStateImpl.class);
        components.registerImplementation(Content2BeanTransformer.class, Content2BeanTransformerImpl.class);
    }

    private class FakeMagnoliaConfigurationProperties extends AbstractMagnoliaConfigurationProperties {
        private FakeMagnoliaConfigurationProperties(TestMagnoliaInitPaths initPaths) throws Exception {
            super(new ArrayList<PropertySource>());
            Properties properties = new Properties();
            customizeProperties(properties);
            sources.add(new FakePropertySource(properties));
            sources.add(new InitPathsPropertySource(initPaths));
            sources.add(new ClasspathPropertySource("/test-magnolia.properties"));
        }
    }

    private class FakePropertySource extends AbstractPropertySource {
        private FakePropertySource(Properties properties) {
            super(properties);
        }
    }
}
