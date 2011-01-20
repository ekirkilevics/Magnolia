/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.objectfactory;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.content2bean.Content2BeanProcessor;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeMapping;
import info.magnolia.content2bean.impl.Content2BeanProcessorImpl;
import info.magnolia.content2bean.impl.TypeMappingImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $) 
 */
public class DefaultComponentProviderTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        SystemProperty.getProperties().clear();
    }

    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        SystemProperty.getProperties().clear();
        super.tearDown();
    }

    public void testReturnsGivenConcreteClassIfNoneConfigured() {
        final DefaultComponentProvider componentProvider = new DefaultComponentProvider(new Properties());
        Object obj = componentProvider.getSingleton(TestImplementation.class);
        assertTrue(obj instanceof TestImplementation);
    }

    public void testBlowsIfGivenInterfaceAndNoImplementationIsConfigured() {
        final DefaultComponentProvider componentProvider = new DefaultComponentProvider(new Properties());
        try {
            componentProvider.getSingleton(TestInterface.class);
            fail("should have thrown a MgnlInstantiationException");
        } catch (MgnlInstantiationException e) {
            assertTrue("expecting a MgnlInstantiationException with a specific message", e instanceof MgnlInstantiationException);
            assertEquals("No concrete implementation defined for interface info.magnolia.objectfactory.DefaultComponentProviderTest$TestInterface", e.getMessage());
        }
    }

    public void testReturnsConfiguredImplementation() {
        final Properties p = new Properties();
        p.setProperty("info.magnolia.objectfactory.DefaultComponentProviderTest$TestInterface", "info.magnolia.objectfactory.DefaultComponentProviderTest$TestImplementation");
        final DefaultComponentProvider componentProvider = new DefaultComponentProvider(p);
        Object obj = componentProvider.getSingleton(TestInterface.class);
        assertTrue(obj instanceof TestImplementation);
    }

    public void testGetSingletonReturnsSameInstance() {
        final ComponentProvider cp = new DefaultComponentProvider(new Properties());

        assertEquals(cp.getSingleton(TestImplementation.class), cp.getSingleton(TestImplementation.class));
        assertSame(cp.getSingleton(TestImplementation.class), cp.getSingleton(TestImplementation.class));
    }

    public void testNewInstanceReallyReturnsNewInstance() {
        final ComponentProvider cp = new DefaultComponentProvider(new Properties());
        assertNotSame(cp.newInstance(TestImplementation.class), cp.newInstance(TestImplementation.class));
    }

    public void testUsesComponentFactoryIfSuchFactoryIsConfigured() {
        final Properties p = new Properties();
        p.setProperty("info.magnolia.objectfactory.DefaultComponentProviderTest$TestInterface", "info.magnolia.objectfactory.DefaultComponentProviderTest$TestInstanceFactory");
        final DefaultComponentProvider componentProvider = new DefaultComponentProvider(p);

        final TestInterface obj = componentProvider.getSingleton(TestInterface.class);
        // DefaultComponentProviderTest$TestInstanceFactory will instantiate a TestOtherImplementation
        assertTrue(obj instanceof TestOtherImplementation);
        // double-check we still get the same instance, since we're calling getSingleton()
        assertSame(obj, componentProvider.getSingleton(TestInterface.class));
    }


    /**
     * TODO - these tests uses ComponentsTestUtil and {@link Components#getSingleton(Class)}: since
     * C2B and ObserverComponentFactory both use {@link Components} to retrieve their ... components.
     * (sort of a cyclic-dependency there)
     */
    public void testSingletonDefinedInRepositoryDefaultToConfigWorkspace() throws RepositoryException, IOException {
        setDefaultImplementationsAndInitMockRepository("/test", ContentRepository.CONFIG,
                "test.class=" + TestImplementation.class.getName()
        );

        Object obj = Components.getSingleton(TestInterface.class);
        assertNotNull(obj);
        assertTrue(obj instanceof TestImplementation);
    }


    /**
     * TODO - these tests uses ComponentsTestUtil and {@link Components#getSingleton(Class)}: since
     * C2B and ObserverComponentFactory both use {@link Components} to retrieve their ... components.
     * (sort of a cyclic-dependency there)
     */
    public void testSingletonDefinedInRepositoryUsesGivenRepoName() throws RepositoryException, IOException {
        Components.getSingleton(String.class);
        setDefaultImplementationsAndInitMockRepository("dummy:/test", "dummy",
                "test.class=" + TestImplementation.class.getName()
        );
        Object obj = Components.getSingleton(TestInterface.class);
        assertNotNull(obj);
        assertTrue(obj instanceof TestImplementation);
    }

    /**
     * TODO - these tests uses ComponentsTestUtil and {@link Components#getSingleton(Class)}: since
     * C2B and ObserverComponentFactory both use {@link Components} to retrieve their ... components.
     * (sort of a cyclic-dependency there)
     */
    public void testProxiesReturnedByObserverComponentFactoryCanBeCastToTheirSubclass() throws Exception {
        setDefaultImplementationsAndInitMockRepository("config:/test", "config",
                "test.class=" + TestOtherImplementation.class.getName()
        );
        TestInterface obj = Components.getSingleton(TestInterface.class);
        assertNotNull(obj);
        // so, I know my project is configured to use a subclass of TestInterface, I can cast away if i want (typically, a module replacing a default implementation)
        assertTrue(obj instanceof TestOtherImplementation);
        assertEquals("bar", ((TestOtherImplementation) obj).getFoo());
    }

    private void setDefaultImplementationsAndInitMockRepository(String componentPropertyValue, String expectedRepoName, String repoContent) throws RepositoryException, IOException {
        // configuration value for the interface, i.e the value set in magnolia.properties, for instance
        ComponentsTestUtil.setImplementation(TestInterface.class, componentPropertyValue);

        // default impl's for content2bean TODO - refactor PropertiesInitializer
        final TypeMappingImpl typeMapping = new TypeMappingImpl();
        ComponentsTestUtil.setInstance(TypeMapping.class, typeMapping);
        ComponentsTestUtil.setInstance(Content2BeanProcessor.class, new Content2BeanProcessorImpl(typeMapping));
        ComponentsTestUtil.setImplementation(Content2BeanTransformer.class, "info.magnolia.content2bean.impl.Content2BeanTransformerImpl");
        ComponentsTestUtil.setImplementation(TransformationState.class, "info.magnolia.content2bean.impl.TransformationStateImpl");

        MockUtil.initMockContext();
        MockUtil.createAndSetHierarchyManager(expectedRepoName, repoContent);
    }

    public static interface TestInterface {

    }

    public static class TestImplementation implements TestInterface {

    }

    public static class TestOtherImplementation extends TestImplementation {
        public String getFoo() {
            return "bar";
        }
    }

    public static final class TestInstanceFactory implements ComponentFactory<TestInterface> {
        public TestInterface newInstance() {
            return new TestOtherImplementation();
        }
    }
}
