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
package info.magnolia.test;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.DefaultComponentProviderTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link info.magnolia.test.ComponentsTestUtil}
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ComponentsTestUtilTest {
    @Before
    public void setUp() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        SystemProperty.clear();
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        SystemProperty.clear();
    }

    @Test
    public void testConfiguredImplementation() {
        ComponentsTestUtil.setImplementation(DefaultComponentProviderTest.TestInterface.class, DefaultComponentProviderTest.TestImplementation.class);
        Object obj = Components.getSingleton(DefaultComponentProviderTest.TestInterface.class);
        assertTrue(obj instanceof DefaultComponentProviderTest.TestImplementation);
    }

    @Test
    public void testSetSingletonInstance() {
        DefaultComponentProviderTest.TestImplementation instance = new DefaultComponentProviderTest.TestImplementation();
        ComponentsTestUtil.setInstance(DefaultComponentProviderTest.TestInterface.class, instance);
        assertSame(instance, Components.getSingleton(DefaultComponentProviderTest.TestInterface.class));
    }

    @Test
    public void testInstanceFactory() {
        ComponentsTestUtil.setInstanceFactory(DefaultComponentProviderTest.TestInterface.class, new DefaultComponentProviderTest.TestInstanceFactory());

        // TestInstanceFactory instantiates TestOtherImplementation
        final Object obj = Components.getSingleton(DefaultComponentProviderTest.TestInterface.class);
        assertTrue(obj instanceof DefaultComponentProviderTest.TestOtherImplementation);
    }
}
