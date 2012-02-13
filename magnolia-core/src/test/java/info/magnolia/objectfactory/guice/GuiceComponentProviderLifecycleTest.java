/**
 * This file Copyright (c) 2011 Magnolia International
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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.junit.Ignore;
import org.junit.Test;

import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Ignore
public class GuiceComponentProviderLifecycleTest {

    public static abstract class LifecycleBase {
        public String startSequence = "";
        public String stopSequence = "";
    }

    @Singleton
    public static class LifecycleSimple extends LifecycleBase {
        @PostConstruct
        public void init() {
            startSequence += "A";
        }

        @PreDestroy
        public void destroy() {
            stopSequence += "A";
        }
    }

    @Singleton
    public static class LifecycleMultiple extends LifecycleBase {
        @PostConstruct
        public void init() {
            startSequence += "A";
        }

        @PostConstruct
        public void init2() {
            startSequence += "B";
        }

        @PreDestroy
        public void destroy() {
            stopSequence += "A";
        }

        @PreDestroy
        public void destroy2() {
            stopSequence += "B";
        }
    }

    @Singleton
    public static class LifecycleExtends extends LifecycleSimple {
        @PostConstruct
        public void init2() {
            startSequence += "X";
        }

        @PreDestroy
        public void destroy2() {
            stopSequence += "X";
        }
    }

    @Singleton
    public static class LifecycleOverrides extends LifecycleSimple {
        @Override
        @PostConstruct
        public void init() {
            startSequence += "B";
        }

        @Override
        @PreDestroy
        public void destroy() {
            stopSequence += "B";
        }
    }

    @Singleton
    public static class LifecycleOverridesRemovesAnnotations extends LifecycleOverrides {
        @Override
        public void init() {
            startSequence += "C";
        }

        @Override
        public void destroy() {
            stopSequence += "C";
        }
    }

    @Singleton
    public static class LifecyclePrivateMethods extends LifecycleBase {
        @PostConstruct
        private void init() {
            startSequence += "D";
        }

        @PreDestroy
        private void destroy() {
            stopSequence += "D";
        }
    }

    @Singleton
    public static class LifecycleSameNamePrivateMethods extends LifecyclePrivateMethods {
        @PostConstruct
        private void init() {
            startSequence += "E";
        }

        @PreDestroy
        private void destroy() {
            stopSequence += "E";
        }
    }
    @Test
    public void testSimpleLifeCycle() {
        assertLifeCycleSequence(LifecycleSimple.class, "A", "A");
    }
    @Test
    public void testMultipleAnnotatedMethods() {
        // order among methods in same class is undefined so we just test that all of them were called
        assertLifeCycleSequenceContainsAll(LifecycleMultiple.class, "AB", "BA");
    }

    @Test
    public void testExtends() {
        assertLifeCycleSequence(LifecycleExtends.class, "AX", "XA");
    }
    @Test
    public void testOverrides() {
        assertLifeCycleSequence(LifecycleOverrides.class, "B", "B");
    }
    @Test
    public void testOverridesRemovesAnnotations() {
        assertLifeCycleSequence(LifecycleOverridesRemovesAnnotations.class, "", "");
    }
    @Test
    public void testPrivateMethods() {
        assertLifeCycleSequence(LifecyclePrivateMethods.class, "D", "D");
    }

    @Test
    public void testSameNamePrivateMethods() {
        assertLifeCycleSequence(LifecycleSameNamePrivateMethods.class, "DE", "ED");
    }

    private void assertLifeCycleSequenceContainsAll(final Class<? extends LifecycleBase> clazz, String startSequence, String endSequence) {
        GuiceComponentProvider p = createProvider(clazz);
        LifecycleBase component = p.getComponent(clazz);
        assertContainsAll(component.startSequence, startSequence);
        p.destroy();
        assertContainsAll(component.stopSequence, endSequence);
    }

    private void assertLifeCycleSequence(Class<? extends LifecycleBase> clazz, String expectedStartSequence, String expectedStopSequence) {
        GuiceComponentProvider p = createProvider(clazz);
        LifecycleBase component = p.getComponent(clazz);
        assertEquals(expectedStartSequence, component.startSequence);
        p.destroy();
        assertEquals(expectedStopSequence, component.stopSequence);
    }

    private GuiceComponentProvider createProvider(Class<? extends LifecycleBase> clazz) {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.registerImplementation(clazz);
        return new GuiceComponentProviderBuilder().withConfiguration(configuration).build();
    }

    private void assertContainsAll(String string, String requiredCharacters) {
        for (char ch : requiredCharacters.toCharArray()) {
            if (string.indexOf(ch) == -1) {
                fail("String [" + string + "] does not contain character [" + ch + "]");
            }
        }
    }
}
