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

import junit.framework.TestCase;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $) 
 */
public class DefaultClassFactoryTest extends TestCase {
    public void testCanInstantiateWithEmptyConstructor() {
        final DefaultClassFactory classFactory = new DefaultClassFactory();
        assertEquals("default", classFactory.newInstance(FooBar.class).getValue());
        assertEquals("default", classFactory.newInstance(FooBar.class, new String[0]).getValue());
        assertEquals("default", classFactory.newInstance(FooBar.class, null).getValue());
    }

    public void testCanInstantiateWithAppropriateConstructor() {
        final DefaultClassFactory classFactory = new DefaultClassFactory();
        assertEquals("bingo", classFactory.newInstance(FooBar.class, "bingo").getValue());
        assertEquals("bingo123", classFactory.newInstance(FooBar.class, "bingo", Long.valueOf(123)).getValue());
        /*
          org.apache.commons.beanutils.ConstructorUtils.invokeConstructor chokes on null arguments
        assertEquals("bingonull", classFactory.newInstance(FooBar.class, "bingo", null).getValue());
        assertEquals("null7", classFactory.newInstance(FooBar.class, null, Long.valueOf(7)).getValue());
         */
    }

    /*
        org.apache.commons.beanutils.ConstructorUtils.invokeConstructor chokes on null arguments
    public void testCanInstantiateWithSingleArgConstructorAndNullParam() {
        final DefaultClassFactory classFactory = new DefaultClassFactory();
        assertEquals("null", classFactory.newInstance(FooBar.class, new Object[]{null}).getValue());
        assertEquals("null", classFactory.newInstance(FooBar.class, new String[]{null}).getValue());
    }
    */

    public void testCanInstantiateWithAppropriateConstructorAndNullParamsWhenSignatureIspecified() {
        final DefaultClassFactory classFactory = new DefaultClassFactory();
        assertEquals("bingo", classFactory.newInstance(FooBar.class, arr(String.class), "bingo").getValue());
        assertEquals("bingo123", classFactory.newInstance(FooBar.class, arr(String.class, Object.class), "bingo", Long.valueOf(123)).getValue());
        assertEquals("bingonull", classFactory.newInstance(FooBar.class, arr(String.class, Object.class), "bingo", null).getValue());
        assertEquals("null7", classFactory.newInstance(FooBar.class, arr(String.class, Object.class), null, Long.valueOf(7)).getValue());
    }

    public void testWillFailIfSignatureWrongSpecifiedEvenIfArgumentsCouldFit() {
        final DefaultClassFactory classFactory = new DefaultClassFactory();
        try {
            assertEquals("bingo", classFactory.newInstance(FooBar.class, arr(Object.class), "bingo").getValue());
            fail("should have failed");
        } catch (MgnlInstantiationException e) {
            // this is what we want
        }

        try {
            assertEquals("null7", classFactory.newInstance(FooBar.class, arr(String.class, Long.class), null, Long.valueOf(7)).getValue());
            fail("should have failed");
        } catch (MgnlInstantiationException e) {
            // this is what we want
        }
    }

    public void testCanInstantiateWithSingleArgConstructorAndNullParamWhenSignatureIspecified() {
        final DefaultClassFactory classFactory = new DefaultClassFactory();
        assertEquals(null, classFactory.newInstance(FooBar.class, arr(String.class), new Object[]{null}).getValue());
        assertEquals(null, classFactory.newInstance(FooBar.class, arr(String.class), new String[]{null}).getValue());

        // not with completely null args
        // classFactory.newInstance(FooBar.class, arr(String.class), null).getValue());

        // not with no-args either, as this calls the newInstance(Class c, Object... params) method
        // classFactory.newInstance(FooBar.class, arr(String.class)).getValue());
    }

    private static Class<?>[] arr(Class<?>... classes) {
        return classes;
    }

    public static class FooBar {
        private final String value;

        public FooBar() {
            this("default");
        }

        public FooBar(String value) {
            this.value = value;
        }

        public FooBar(String value, Object dummy) {
            this.value = value + String.valueOf(dummy);
        }

        public String getValue() {
            return value;
        }
    }
}
