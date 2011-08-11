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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class ObjectFactoryTest {
    @Before
    public void setUp() throws Exception {
        SystemProperty.setMagnoliaConfigurationProperties(new TestMagnoliaConfigurationProperties());
    }

    @After
    public void tearDown() throws Exception {
        SystemProperty.clear();
        Components.setProvider(null);
    }
    @Test
    public void testSettingSystemPropertyIntoDefaultComponentStillAllowsToSwapImplementationsLater() {
/*

        // TODO This test is ignored for now since registering components via SystemProperty is no longer supported.

        Components.setProvider(new DefaultComponentProvider(SystemProperty.getProperties()));
        SystemProperty.setProperty("java.lang.Object", "java.lang.String");
        final Object o1 = Components.getComponentProvider().newInstance(Object.class);
        assertTrue(o1 instanceof String);

        SystemProperty.setProperty("java.lang.Object", "java.util.Date");
        final Object o2 = Components.getComponentProvider().newInstance(Object.class);
        assertTrue(o2 instanceof java.util.Date);
*/
    }
}
