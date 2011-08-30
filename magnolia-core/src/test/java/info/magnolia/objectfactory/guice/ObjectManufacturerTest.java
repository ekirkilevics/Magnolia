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

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Test case for ObjectManufacturer.
 */
public class ObjectManufacturerTest {

    private ObjectManufacturer manufacturer;
    private Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).toInstance("foobar");
            }
        });

        manufacturer = new ObjectManufacturer();
    }

    public static class NameableObject {

        protected String name;

        public NameableObject() {
        }

        public String getName() {
            return name;
        }
    }

    public static class ObjectWithAnnotatedConstructor extends NameableObject {

        @Inject
        public ObjectWithAnnotatedConstructor(String name) {
            this.name = name;
        }
    }

    @Test
    public void annotatedConstructor() {
        NameableObject object = (NameableObject) manufacturer.newInstance(
                ObjectWithAnnotatedConstructor.class,
                new GuiceParameterResolver(injector));
        assertEquals("foobar", object.getName());

        NameableObject object2 = (NameableObject) manufacturer.newInstance(
                ObjectWithAnnotatedConstructor.class,
                new CandidateParameterResolver(new Object[]{"12345"}),
                new GuiceParameterResolver(injector));
        assertEquals("12345", object2.getName());
    }

    public static class ObjectWithGreedyConstructor extends NameableObject {

        public ObjectWithGreedyConstructor() {
        }

        public ObjectWithGreedyConstructor(String name) {
            this.name = name;
        }

        public ObjectWithGreedyConstructor(String name, Class x) {
            fail();
        }
    }

    @Test
    public void greedyConstructor() {
        NameableObject object = (NameableObject) manufacturer.newInstance(
                ObjectWithGreedyConstructor.class,
                new GuiceParameterResolver(injector));
        assertEquals("foobar", object.getName());

        NameableObject object2 = (NameableObject) manufacturer.newInstance(
                ObjectWithGreedyConstructor.class,
                new CandidateParameterResolver(new Object[]{"12345"}),
                new GuiceParameterResolver(injector));
        assertEquals("12345", object2.getName());
    }

    public static class ObjectWithGreediestConstructorPrivate extends NameableObject {

        public ObjectWithGreediestConstructorPrivate() {
        }

        private ObjectWithGreediestConstructorPrivate(String name) {
        }
    }

    @Test
    public void ignorePrivateConstructor() {
        NameableObject object = (NameableObject) manufacturer.newInstance(
                ObjectWithGreediestConstructorPrivate.class,
                new GuiceParameterResolver(injector));
        assertNull(object.getName());
    }

    public static class ObjectWithMultipleAnnotatedConstructors extends NameableObject {

        @Inject
        public ObjectWithMultipleAnnotatedConstructors() {
        }

        @Inject
        public ObjectWithMultipleAnnotatedConstructors(String name) {
            this.name = name;
        }
    }

    @Test(expected = RuntimeException.class)
    public void multipleAnnotatedConstructors() {
        manufacturer.newInstance(ObjectWithMultipleAnnotatedConstructors.class);
    }


    public static class ObjectWithConstructorThatThrowsException extends NameableObject {

        @Inject
        public ObjectWithConstructorThatThrowsException(String name) {
            throw new IllegalArgumentException();
        }
    }

    @Test(expected = RuntimeException.class)
    public void failOnConstructorThatThrowsException() {
        manufacturer.newInstance(
                ObjectWithConstructorThatThrowsException.class,
                new GuiceParameterResolver(injector));
    }

    public static class ObjectWithNoPrivateConstructor extends NameableObject {

        protected ObjectWithNoPrivateConstructor() {
        }

        private ObjectWithNoPrivateConstructor(String name) {
        }
    }

    @Test(expected = RuntimeException.class)
    public void failWhenNoPublicConstructor() {
        manufacturer.newInstance(
                ObjectWithNoPrivateConstructor.class,
                new GuiceParameterResolver(injector));

    }
}
