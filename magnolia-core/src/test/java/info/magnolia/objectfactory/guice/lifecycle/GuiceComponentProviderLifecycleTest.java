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
package info.magnolia.objectfactory.guice.lifecycle;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.objectfactory.guice.lifecycle.packageprotected.LifecycleExtendsClassWithPackageProtectedMethods;
import info.magnolia.objectfactory.guice.lifecycle.packageprotected.LifecyclePackageProtectedMethod;

/**
 * This test case makes sure that @PostConstruct and @PreDestroy is implemented according to JSR250 when used in a class
 * hierarchy and on private and package protected methods.
 *
 * Support for @PreDestroy is currently disabled because it proved unreliable for lazy singletons and scoped objects.
 */
public class GuiceComponentProviderLifecycleTest {

    private boolean shouldTestDestroy = false;

    private List<String> events = new ArrayList<String>();

    /**
     * Tests the simplest case where a class has public annotated methods and no inheritance is involved.
     *
     * @see LifecycleSimple
     */
    @Test
    public void testSimpleLifeCycle() {

        // GIVEN
        GuiceComponentProvider p = createProvider(LifecycleSimple.class);

        // WHEN
        p.getComponent(LifecycleSimple.class);

        // THEN
        assertEvent("LifecycleSimple.init");
        assertNoMoreEvents();

        if (shouldTestDestroy) {

            // WHEN
            p.destroy();

            // THEN
            assertEvent("LifecycleSimple.destroy");
            assertNoMoreEvents();
        }
    }

    /**
     * Tests support for having multiple annotated methods. Note that they can be invoked in any order since the order
     * methods appear in the binary files are not guaranteed to match that in the source files.
     *
     * @see LifecycleMultiple
     */
    @Test
    public void testMultipleAnnotatedMethods() {

        // GIVEN
        GuiceComponentProvider p = createProvider(LifecycleMultiple.class);

        // WHEN
        p.getComponent(LifecycleMultiple.class);

        // THEN
        assertEventsInAnyOrder("LifecycleMultiple.init", "LifecycleMultiple.init2");
        assertNoMoreEvents();

        if (shouldTestDestroy) {

            // WHEN
            p.destroy();

            // THEN
            assertEventsInAnyOrder("LifecycleMultiple.destroy", "LifecycleMultiple.destroy2");
            assertNoMoreEvents();
        }
    }

    /**
     * Tests that methods are invoked in the correct order when inheritance is used. Methods with @PostConstruct are
     * invoked top down, and methods with @PreDestroy are invoked bottom up.
     *
     * @see LifecycleExtends
     */
    @Test
    public void testExtends() {

        // GIVEN
        GuiceComponentProvider p = createProvider(LifecycleExtends.class);

        // WHEN
        p.getComponent(LifecycleExtends.class);

        // THEN
        assertEvent("LifecycleSimple.init");
        assertEvent("LifecycleExtends.init2");
        assertNoMoreEvents();

        if (shouldTestDestroy) {

            // WHEN
            p.destroy();

            // THEN
            assertEvent("LifecycleExtends.destroy2");
            assertEvent("LifecycleSimple.destroy");
            assertNoMoreEvents();
        }
    }

    /**
     * Tests that the implementation detects overridden methods and invokes them only once. Otherwise it would call
     * it twice.
     *
     * @see LifecycleOverrides
     */
    @Test
    public void testOverrides() {

        // GIVEN
        GuiceComponentProvider p = createProvider(LifecycleOverrides.class);

        // WHEN
        p.getComponent(LifecycleOverrides.class);

        // THEN
        assertEvent("LifecycleOverrides.init");
        assertNoMoreEvents();

        if (shouldTestDestroy) {

            // WHEN
            p.destroy();

            // THEN
            assertEvent("LifecycleOverrides.destroy");
            assertNoMoreEvents();
        }
    }

    /**
     * Tests that methods that override an annotated method in the super class and does not itself declare the
     * annotation are not called.
     *
     * @see LifecycleOverridesRemovesAnnotations
     */
    @Test
    public void testOverridesRemovesAnnotations() {

        // GIVEN
        GuiceComponentProvider p = createProvider(LifecycleOverridesRemovesAnnotations.class);

        // WHEN
        p.getComponent(LifecycleOverridesRemovesAnnotations.class);

        // THEN
        assertNoMoreEvents();

        if (shouldTestDestroy) {

            // WHEN
            p.destroy();

            // THEN
            assertNoMoreEvents();
        }
    }

    /**
     * Tests that private lifecycle methods are invoked.
     *
     * @see LifecyclePrivateMethods
     */
    @Test
    public void testPrivateMethods() {

        // GIVEN
        GuiceComponentProvider p = createProvider(LifecyclePrivateMethods.class);

        // WHEN
        p.getComponent(LifecyclePrivateMethods.class);

        // THEN
        assertEvent("LifecyclePrivateMethods.init");
        assertNoMoreEvents();

        if (shouldTestDestroy) {

            // WHEN
            p.destroy();

            // THEN
            assertEvent("LifecyclePrivateMethods.destroy");
            assertNoMoreEvents();
        }
    }

    /**
     * Tests that private lifecycle methods in a class hierarchy where method signatures are identical in different
     * classes are all called.
     *
     * @see LifecyclePrivateMethods
     */
    @Test
    public void testSameNamePrivateMethods() {

        // GIVEN
        GuiceComponentProvider p = createProvider(LifecycleSameNamePrivateMethods.class);

        // WHEN
        p.getComponent(LifecycleSameNamePrivateMethods.class);

        // THEN
        assertEvent("LifecyclePrivateMethods.init");
        assertEvent("LifecycleSameNamePrivateMethods.init");
        assertNoMoreEvents();

        if (shouldTestDestroy) {

            // WHEN
            p.destroy();

            // THEN
            assertEvent("LifecycleSameNamePrivateMethods.destroy");
            assertEvent("LifecyclePrivateMethods.destroy");
            assertNoMoreEvents();
        }
    }

    /**
     * Tests that package protected methods are invoked.
     *
     * @see LifecyclePackageProtectedMethod
     */
    @Test
    public void testLifeCycleOnPackageProtectedMethods() {

        // GIVEN
        GuiceComponentProvider p = createProvider(LifecyclePackageProtectedMethod.class);

        // WHEN
        p.getComponent(LifecyclePackageProtectedMethod.class);

        // THEN
        assertEvent("LifecyclePackageProtectedMethod.init");
        assertNoMoreEvents();

        if (shouldTestDestroy) {

            // WHEN
            p.destroy();

            // THEN
            assertEvent("LifecyclePackageProtectedMethod.destroy");
            assertNoMoreEvents();
        }
    }

    /**
     * Tests that in a case where a class A that inherits from another class B and has methods with same signature as
     * package protected methods in that class, methods on both classes A and B are invoked because they were not
     * overridden.
     *
     * @see LifecycleExtendsClassWithPackageProtectedMethodsInOtherPackage
     */
    @Test
    public void testLifeCycleOnClassThatExtendsClassWithPackageProtectedMethods() {

        // GIVEN
        GuiceComponentProvider p = createProvider(LifecycleExtendsClassWithPackageProtectedMethodsInOtherPackage.class);

        // WHEN
        p.getComponent(LifecycleExtendsClassWithPackageProtectedMethodsInOtherPackage.class);

        // THEN
        assertEvent("LifecyclePackageProtectedMethod.init");
        assertEvent("LifecycleExtendsClassWithPackageProtectedMethodsInOtherPackage.init");
        assertNoMoreEvents();

        if (shouldTestDestroy) {

            // WHEN
            p.destroy();

            // THEN
            assertEvent("LifecycleExtendsClassWithPackageProtectedMethodsInOtherPackage.destroy");
            assertEvent("LifecyclePackageProtectedMethod.destroy");
            assertNoMoreEvents();
        }
    }

    /**
     * Tests that the implementation detects overridden package protected methods and invokes them only one. If it
     * doesn't they would be invoked twice.
     *
     * @see LifecycleExtendsClassWithPackageProtectedMethods
     */
    @Test
    public void testLifeCycleOnClassThatOverridesPackageProtectedMethods() {

        // GIVEN
        GuiceComponentProvider p = createProvider(LifecycleExtendsClassWithPackageProtectedMethods.class);

        // WHEN
        p.getComponent(LifecycleExtendsClassWithPackageProtectedMethods.class);

        // THEN
        assertEvent("LifecycleExtendsClassWithPackageProtectedMethods.init");
        assertNoMoreEvents();

        if (shouldTestDestroy) {

            // WHEN
            p.destroy();

            // THEN
            assertEvent("LifecycleExtendsClassWithPackageProtectedMethods.destroy");
            assertNoMoreEvents();
        }
    }

    private GuiceComponentProvider createProvider(Class<?> clazz) {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.registerInstance(List.class, events);
        configuration.registerImplementation(clazz);
        return new GuiceComponentProviderBuilder().withConfiguration(configuration).build();
    }

    private void assertNoMoreEvents() {
        if (!events.isEmpty()) {
            fail("Unexpected events: " + events);
        }
    }

    private void assertEvent(String expectedString) {
        if (events.isEmpty()) {
            fail("Expected event: " + expectedString + ", but the event list is empty");
        }
        String actualString = events.get(0);
        assertEquals(expectedString, actualString);
        events.remove(0);
    }

    private void assertEventsInAnyOrder(String... expectedEvents) {
        if (events.size() < expectedEvents.length) {
            fail("Expected " + StringUtils.join(expectedEvents, ",") + " but the event queue is: " + events);
        }

        List<String> list = events.subList(0, expectedEvents.length);
        for (String expectedEvent : expectedEvents) {
            assertTrue("Expected event " + expectedEvent + " was not found in queue", list.contains(expectedEvent));
        }

        for (@SuppressWarnings("UnusedDeclaration") String expectedEvent : expectedEvents) {
            events.remove(0);
        }
    }
}
