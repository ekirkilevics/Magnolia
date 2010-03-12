/**
 * This file Copyright (c) 2010 Magnolia International
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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.content2bean.Bean2ContentProcessor;
import info.magnolia.content2bean.Content2BeanProcessor;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeMapping;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ObservedComponentFactoryTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // default impl's for content2bean TODO - refactor PropertiesInitializer
        ComponentsTestUtil.setImplementation(Content2BeanProcessor.class, "info.magnolia.content2bean.impl.Content2BeanProcessorImpl");
        ComponentsTestUtil.setImplementation(Content2BeanTransformer.class, "info.magnolia.content2bean.impl.Content2BeanTransformerImpl");
        ComponentsTestUtil.setImplementation(Bean2ContentProcessor.class, "info.magnolia.content2bean.Bean2ContentProcessorImpl");
        ComponentsTestUtil.setImplementation(TransformationState.class, "info.magnolia.content2bean.impl.TransformationStateImpl");
        ComponentsTestUtil.setImplementation(TypeMapping.class, "info.magnolia.content2bean.impl.PropertiesBasedTypeMapping");

    }

    @Override
    protected void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        SystemProperty.getProperties().clear();
        super.tearDown();
    }

    public void testReturnsAProxyToGivenTypeIfConcreteClassEvenIfPathDoesNotExistYet() throws Exception {
        MockUtil.createAndSetHierarchyManager("test", "/foo");

        final ObservedComponentFactory<MyComponent> compFac = new NonObservingObservedComponentFactory("test", "/foo/bar", MyComponent.class);
        final MyComponent observedCompo = compFac.newInstance();
        assertNotNull(observedCompo);
        assertEquals("default", observedCompo.getTheString());
    }

    public void testProxyIsSwappedOncePathGetsReloaded() throws Exception {
        MockUtil.createAndSetHierarchyManager("test", "/foo");

        final ObservedComponentFactory<MyComponent> compFac = new NonObservingObservedComponentFactory("test", "/foo/bar", MyComponent.class);
        final MyComponent observedCompo = compFac.newInstance();
        assertNotNull(observedCompo);
        assertEquals("default", observedCompo.getTheString());

        // now create the node
        final Content root = MgnlContext.getHierarchyManager("test").getContent("/foo");
        final Content content = root.createContent("bar");
        content.setNodeData("class", MyComponent.class.getName());
        content.setNodeData("theString", "new value");

        // fake observation
        compFac.onEvent(null);

        // the new state should be reflecting without re-fetching the component instance from the factory
        assertEquals("new value", observedCompo.getTheString());
    }

    /*
    public void testReturnsANullProxyUntilPathExistsForNonConcreteTypes() throws Exception {
        MockUtil.createAndSetHierarchyManager("test", "/foo");

        final ObservedComponentFactory<MyComponent> compFac = new NonObservingObservedComponentFactory("test", "/foo/bar", MyInterface.class);
        final MyComponent observedCompo = compFac.newInstance();
        // TODO : currently fails here:
        assertNotNull(observedCompo);
        assertEquals(null, observedCompo.getTheString());

        // now create the node
        final Content root = MgnlContext.getHierarchyManager("test").getContent("/foo");
        final Content content = root.createContent("bar");
        content.setNodeData("class", MyImpl.class.getName());
        content.setNodeData("someString", "new value");

        // fake observation
        compFac.onEvent(null);

        // the new state should be reflecting without re-fetching the component instance from the factory
        assertEquals("new value", observedCompo.getTheString());
    }
    */

    public static class MyComponent {
        private String theString = "default";

        public String getTheString() {
            return theString;
        }

        public void setTheString(String theString) {
            this.theString = theString;
        }
    }

    public static class SubComponent extends MyComponent {
        private int theInt = 42;

        public SubComponent() {
            setTheString("sub-default");
        }

        public int getTheInt() {
            return theInt;
        }

        public void setTheInt(int theInt) {
            this.theInt = theInt;
        }
    }

    public static interface MyInterface {
        String getSomeString();
    }

    public static class MyImpl implements MyInterface {
        private String someString;

        public String getSomeString() {
            return someString;
        }

        public void setSomeString(String someString) {
            this.someString = someString;
        }
    }

    private static class NonObservingObservedComponentFactory extends ObservedComponentFactory {
        public NonObservingObservedComponentFactory(String repository, String path, Class type) {
            super(repository, path, type);
        }

        @Override
        protected void startObservation(String handle) {
            // bypass observation for the purpose of the test
        }
    }
}
