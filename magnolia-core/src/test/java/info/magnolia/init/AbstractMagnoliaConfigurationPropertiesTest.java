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
package info.magnolia.init;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class AbstractMagnoliaConfigurationPropertiesTest {

    private MagnoliaConfigurationProperties p;

    @Before
    public void setUp() throws Exception {
        p = new TestMagnoliaConfigurationProperties(getClass().getResourceAsStream("/test-init.properties"));
    }

    @After
    public void tearDown() throws Exception {
        SystemProperty.clear();
        System.getProperties().remove("testProp");
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testSimpleProperty() throws Exception {
        assertEquals("property", p.getProperty("test.one"));
    }

    @Test
    public void testNestedProperty() throws Exception {
        assertEquals("nested property", p.getProperty("test.two"));
    }

    @Test
    public void testNestedPropertyMoreLevels() throws Exception {
        assertEquals("another nested property", p.getProperty("test.three"));
    }

    @Test
    public void testNestedSomeMore() throws Exception {
        assertEquals("nest property nested property another nested property", p.getProperty("test.four"));
    }

    @Test
    public void testCircularProperty() throws Exception {
        assertEquals("${test.circular2}", p.getProperty("test.circular1"));
        assertEquals("${test.circular1}", p.getProperty("test.circular2"));
    }

    @Test
    public void testSelfReferencingProperty() throws Exception {
        assertEquals("${test.circular3}", p.getProperty("test.circular3"));
    }

    @Test
    public void testValuesAreTrimmed() throws Exception {
        assertEquals("foo", p.getProperty("test.whitespaces"));
    }

    @Test
    public void testValuesForNestedPropertiesAreTrimmed() throws Exception {
        // TODO : i get the feeling this passes by accident, and would not pass if .nested was iterated on first
        assertEquals("bar foo", p.getProperty("test.whitespaces.nested"));
    }

    @Test
    public void describeAndToStringAreNotRepeatingThemselves() throws Exception {
        final TestMagnoliaConfigurationProperties p = new TestMagnoliaConfigurationProperties("foo", "bar");
        assertEquals("[TestMagnoliaConfigurationProperties with sources: [TestPropertySource][ClasspathPropertySource from /test-magnolia.properties][InitPathsPropertySource]]", p.describe());
        assertTrue(p.toString().startsWith(p.describe() + " with properties: "));
    }
}
