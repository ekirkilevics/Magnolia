/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.jcr.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockNode;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class PropertiesImportExportTest {

    private PropertiesImportExport pie;

    @Before
    public void setUp() {
        pie = new PropertiesImportExport() {
            /**
             * Override to allow setting required setting of identifier
             */
            @Override
            protected void setIdentifier(Node c, String valueStr) {
                ((MockNode) c).setIdentifier(valueStr);
            }
        };

    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testConvertsToWrapperType() {
        assertEquals(Boolean.TRUE, pie.convertPropertyStringToObject("boolean:true"));
        assertEquals(Boolean.FALSE, pie.convertPropertyStringToObject("boolean:false"));
        assertEquals(Integer.valueOf(5), pie.convertPropertyStringToObject("integer:5"));
        final Object dateConvertedObject = pie.convertPropertyStringToObject("date:2009-10-14T08:59:01.227-04:00");
        assertTrue(dateConvertedObject instanceof Calendar);
        assertEquals(1255525141227L, ((Calendar) dateConvertedObject).getTimeInMillis());
        // It's null if it doesn't match the exact format string
        final Object dateOnlyObject = pie.convertPropertyStringToObject("date:2009-12-12");
        assertNull(dateOnlyObject);
    }

    @Test
    public void testCanUseIntShortcutForConvertingIntegers() {
        assertEquals(Integer.valueOf(37), pie.convertPropertyStringToObject("int:37"));
    }

    @Test
    public void testCreateContent() throws Exception {
        final MockNode root = new MockNode("root");

        String content =
            "/parent1/sub1.prop1=one\n" +
            "/parent2/sub2\n" +
            "/parent2/sub2.prop1=two";

        pie.createContent(root, new ByteArrayInputStream(content.getBytes()));
        assertEquals("one", root.getNode("/parent1/sub1").getProperty("prop1").getString());
        assertTrue(root.hasNode("/parent2/sub2"));
        assertEquals("two", root.getNode("/parent2/sub2").getProperty("prop1").getString());

        content =
            "/parent1/sub1.@uuid=1\n" +
            "/parent2/sub2.@uuid=2";

        pie.createContent(root, new ByteArrayInputStream(content.getBytes()));
        assertEquals("1", root.getNode("/parent1/sub1").getIdentifier());
        assertEquals("2", root.getNode("/parent2/sub2").getIdentifier());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateContentFailingBecauseOfEqualsSignWithoutADot() throws Exception {
        final MockNode root = new MockNode("root");
        String content =
            "/parent/sub/prop=2";
        pie.createContent(root, new ByteArrayInputStream(content.getBytes()));
    }


    @Test(expected=IllegalArgumentException.class)
    public void testCreateContentFailingBecauseOfMissingTrailingSlash() throws Exception {
        String content =
            "parent/sub@uuid=1";
        pie.createContent(null, new ByteArrayInputStream(content.getBytes()));
    }

    @Test(expected=Exception.class)
    public void testCreateContentFailingBecauseOfDotAndMonkeyTail() throws Exception {
        String content =
            "/parent/sub@uuid=1";
        pie.createContent(null, new ByteArrayInputStream(content.getBytes()));
    }

    @Test(expected=Exception.class)
    public void testCreateContentFailingBecauseOfDotInPath() throws Exception {
        String content =
            "/parent.sub.@uuid=1";
        pie.createContent(null, new ByteArrayInputStream(content.getBytes()));
    }

}
