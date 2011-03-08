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

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import junit.framework.TestCase;
import org.apache.commons.beanutils.ConstructorUtils;

import java.lang.reflect.Field;
import java.util.Date;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $) 
 */
public class ClassesTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
        SystemProperty.clear();
        SystemProperty.setMagnoliaConfigurationProperties(new TestMagnoliaConfigurationProperties());
        Components.setProvider(new MockComponentProvider(SystemProperty.getProperties()));
        resetCFP();
    }

    protected void tearDown() throws Exception {
        resetCFP();
        SystemProperty.clear();
        Components.setProvider(null);
        super.tearDown();
    }

    private void resetCFP() throws NoSuchFieldException, IllegalAccessException {
        // reset the classFactoryProvider field. TODO: really wish i could have made this one final ...
        final Field cfpField = Classes.class.getDeclaredField("cfp");
        cfpField.setAccessible(true);
        cfpField.set(null, new Classes.ClassFactoryProvider(new DefaultClassFactory()));
    }

    public void testDefaultClassFactoryWorksJustFine() throws ClassNotFoundException {
        final String s = Classes.newInstance("java.lang.String", "hello");
        assertEquals("hello", s);

        final ClassFactory cf = Classes.getClassFactory();
        assertTrue(cf instanceof DefaultClassFactory);
    }

    public void testCanSetupADifferentClassFactory() throws ClassNotFoundException {
        SystemProperty.setProperty(ClassFactory.class.getName(), TestClassFactory.class.getName());
        final String s = Classes.newInstance("chalala", "hello");
        // this validates we're indeed using our custom TestClassFactory, since "chalala" isn't a real class name, afaik.
        assertEquals("hello", s);

        final ClassFactory cf = Classes.getClassFactory();
        assertTrue(cf instanceof TestClassFactory);
    }
/*
    public void testCanSetupADifferentClassFactoryThatNeedsComponents() throws ClassNotFoundException {

        // TODO This test is ignored for now since registering components via SystemProperty is no longer supported.

        SystemProperty.setProperty(ClassFactory.class.getName(), TestClassFactoryWithComponents.class.getName());
        SystemProperty.setProperty(Whatever.class.getName(), Chenanigans.class.getName());
        final String s = Classes.newInstance("chalala", "hello");
        assertEquals("hello", s);
        final Whatever w = Components.getSingleton(Whatever.class);
        assertEquals(327, w.blah());

        final ClassFactory cf = Classes.getClassFactory();
        assertTrue(cf instanceof TestClassFactoryWithComponents);
        final TestClassFactoryWithComponents cfc = (TestClassFactoryWithComponents) cf;
        assertTrue(cfc.getWhatever() instanceof Chenanigans);
        assertEquals(327, cfc.getWhatever().blah());
        final Calendar now = Calendar.getInstance();
        // a fairly vague comparison of those two dates
        final Calendar then = Calendar.getInstance();
        then.setTime(cfc.getDate());
        assertEquals(now.get(Calendar.DAY_OF_YEAR), then.get(Calendar.DAY_OF_YEAR));
    }
*/
    public static interface Whatever {
        int blah();
    }

    public static class Chenanigans implements Whatever {
        public int blah() {
            return 327;
        }
    }

    /**
     * forName only accepts test-specific names, and newInstance() implementations don't handle exceptions, don't check params/types and don't take any shortcut either. 
     */
    public static class TestClassFactory implements ClassFactory {
        public TestClassFactory() {
        }

        public <C> Class<C> forName(String className) throws ClassNotFoundException {
            if ("chalala".equals(className)) {
                return (Class<C>) String.class;
            }
            throw new IllegalStateException("unexpected call with " + className);
        }

        public <T> T newInstance(Class<T> c, Object... params) {
            try {
                return (T) ConstructorUtils.invokeConstructor(c, params);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public <T> T newInstance(Class<T> c, Class<?>[] argTypes, Object... params) {
            try {
                return (T) ConstructorUtils.invokeExactConstructor(c, params, argTypes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class TestClassFactoryWithComponents extends TestClassFactory {
        private final ClassFactory delegate;
        private final Whatever whatever;
        private final Date date;

        public TestClassFactoryWithComponents() throws ClassNotFoundException {
            this.delegate = new DefaultClassFactory();
            this.whatever = Components.getSingleton(Whatever.class);
            this.date = Classes.newInstance("java.util.Date");
        }

        public Whatever getWhatever() {
            return whatever;
        }

        public Date getDate() {
            return date;
        }
    }
}
