/**
 * This file Copyright (c) 2012-2012 Magnolia International
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

import javax.inject.Named;
import javax.inject.Provider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import info.magnolia.objectfactory.ObjectManufacturer;

/**
 * Test case for {@link GuiceParameterResolver}.
 */
public class GuiceParameterResolverTest {

    public static class TestDependency {
    }

    public static class SimpleInjectionTestClass {

        private TestDependency dependency;

        public SimpleInjectionTestClass(TestDependency dependency) {
            this.dependency = dependency;
        }

        public TestDependency getDependency() {
            return dependency;
        }
    }

    public static class ProviderInjectionTestClass {

        private Provider<TestDependency> dependency;

        public ProviderInjectionTestClass(Provider<TestDependency> dependency) {
            this.dependency = dependency;
        }

        public Provider<TestDependency> getDependency() {
            return dependency;
        }
    }

    public static class NamedDependencyInjectionTestClass {

        private TestDependency dependency;

        public NamedDependencyInjectionTestClass(@Named("named") TestDependency dependency) {
            this.dependency = dependency;
        }

        public TestDependency getDependency() {
            return dependency;
        }
    }

    @Test
    public void testSimpleInjection() {

        // GIVEN
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TestDependency.class);
            }
        });
        ObjectManufacturer objectManufacturer = new ObjectManufacturer();

        // WHEN
        SimpleInjectionTestClass instance = (SimpleInjectionTestClass) objectManufacturer.newInstance(SimpleInjectionTestClass.class, new GuiceParameterResolver(injector));

        // THEN
        assertNotNull(instance);
        assertNotNull(instance.getDependency());
    }

    @Test
    public void testInjectionOfDependencyAsProvider() {

        // GIVEN
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TestDependency.class);
            }
        });
        ObjectManufacturer objectManufacturer = new ObjectManufacturer();

        // WHEN
        ProviderInjectionTestClass instance = (ProviderInjectionTestClass) objectManufacturer.newInstance(ProviderInjectionTestClass.class, new GuiceParameterResolver(injector));

        // THEN
        assertNotNull(instance);
        assertNotNull(instance.getDependency());
        assertNotNull(instance.getDependency().get());
    }

    @Test
    public void testInjectionOfNamedDependency() {

        // GIVEN
        final TestDependency dependency = new TestDependency();
        Injector injector = Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                bind(TestDependency.class).annotatedWith(Names.named("named")).toProvider(Providers.of(dependency));
            }
        });
        ObjectManufacturer objectManufacturer = new ObjectManufacturer();

        // WHEN
        NamedDependencyInjectionTestClass instance = (NamedDependencyInjectionTestClass) objectManufacturer.newInstance(NamedDependencyInjectionTestClass.class, new GuiceParameterResolver(injector));

        // THEN
        assertNotNull(instance);
        assertNotNull(instance.getDependency());
        assertSame(dependency, instance.getDependency());
        assertNotSame(dependency, injector.getInstance(TestDependency.class));
    }
}
