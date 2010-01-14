/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.test.MgnlTestCase;

import javax.jcr.RepositoryException;
import java.io.IOException;

public class FactoryUtilTest extends MgnlTestCase {

    public static interface TestInterface {

    }

    public static class TestImplementation implements TestInterface {

    }

    public static class TestOtherImplementation extends TestImplementation {

    }

    public static final class TestInstanceFactory implements FactoryUtil.InstanceFactory {
        public Object newInstance() {
            return new TestOtherImplementation();
        }
    }

    public void testConfiguredImplementation() {
        FactoryUtil.setDefaultImplementation(TestInterface.class, TestImplementation.class);
        Object obj = FactoryUtil.getSingleton(TestInterface.class);
        assertTrue(obj instanceof TestImplementation);
    }

    public void testDontRedefineTheDefaultImplementation() {
        FactoryUtil.setDefaultImplementation(TestInterface.class, TestImplementation.class);
        FactoryUtil.setDefaultImplementation(TestInterface.class, "a.wrong.class.not.set");
        Object obj = FactoryUtil.getSingleton(TestInterface.class);
        assertTrue(obj instanceof TestImplementation);
    }

    public void testDefaultImplementation() {
        Object obj = FactoryUtil.getSingleton(TestImplementation.class);
        assertTrue(obj instanceof TestImplementation);
    }

    public void testSingleton() {
        assertEquals(FactoryUtil.getSingleton(TestImplementation.class), FactoryUtil.getSingleton(TestImplementation.class));
    }

    public void testNewInstance() {
        assertNotSame(FactoryUtil.newInstance(TestImplementation.class), FactoryUtil.newInstance(TestImplementation.class));
    }

    public void testSetSingletonInstance() {
        TestImplementation instance = new TestImplementation();
        FactoryUtil.setInstance(TestInterface.class, instance);
        assertSame(instance, FactoryUtil.getSingleton(TestInterface.class));
    }

    public void testInstanceFactory() {
        FactoryUtil.setInstanceFactory(TestInterface.class, new TestInstanceFactory());

        assertTrue(FactoryUtil.getSingleton(TestInterface.class) instanceof TestOtherImplementation);
    }

    public void testSingletonDefinedInRepository() throws RepositoryException, IOException {
        FactoryUtil.setDefaultImplementation(TestInterface.class, "/test");
        initMockConfigRepository(
                "test.class=" + TestImplementation.class.getName()
        );
        Object obj = FactoryUtil.getSingleton(TestInterface.class);
        assertNotNull(obj);
        assertTrue(obj instanceof TestImplementation);
    }

    public void testSingletonDefinedInRepositoryUsingRepositoryPrefix() throws RepositoryException, IOException {
        FactoryUtil.setDefaultImplementation(TestInterface.class, "config:/test");
        initMockConfigRepository(
                "test.class=" + TestImplementation.class.getName()
        );
        Object obj = FactoryUtil.getSingleton(TestInterface.class);
        assertNotNull(obj);
        assertTrue(obj instanceof TestImplementation);
    }

    public void testUseInstanceFactoryAsProperty() throws RepositoryException, IOException {
        FactoryUtil.setDefaultImplementation(TestInterface.class, TestInstanceFactory.class.getName());
        Object obj = FactoryUtil.getSingleton(TestInterface.class);
        assertNotNull(obj);
        assertTrue(obj instanceof TestImplementation);
    }


}
