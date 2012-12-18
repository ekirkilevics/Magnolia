/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link PropertiesImportExport}.
 */
public class PropertiesImportExportTest {

    private PropertiesImportExport pie;

    @Before
    public void setUp() {
        pie = new PropertiesImportExport() {

            /**
             * Override to allow setting required setting of identifier.
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
    public void testConvertPropertyStringToBinaryObject() throws Exception {
        // WHEN
        Object result = pie.convertPropertyStringToObject("binary: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\n");

        // THEN
        assertTrue(result instanceof InputStream);
        ((InputStream) result).close();
    }

    @Test
    public void testCanUseIntShortcutForConvertingIntegers() {
        assertEquals(Integer.valueOf(37), pie.convertPropertyStringToObject("int:37"));
    }

    @Test
    public void testCreateNodes() throws Exception {
        final MockNode root = new MockNode();

        String content =
                "/parent1/sub1.prop1=one\n" +
                        "/parent2/sub2\n" +
                        "/parent2/sub2.prop1=two";

        pie.createNodes(root, new ByteArrayInputStream(content.getBytes()));
        assertEquals("one", root.getNode("/parent1/sub1").getProperty("prop1").getString());
        assertTrue(root.hasNode("/parent2/sub2"));
        assertEquals("two", root.getNode("/parent2/sub2").getProperty("prop1").getString());

        content = "/parent1/sub1.@uuid=1\n" +
                "/parent2/sub2.@uuid=2";

        pie.createNodes(root, new ByteArrayInputStream(content.getBytes()));
        assertEquals("1", root.getNode("/parent1/sub1").getIdentifier());
        assertEquals("2", root.getNode("/parent2/sub2").getIdentifier());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNodesFailingBecauseOfEqualsSignWithoutADot() throws Exception {
        final MockNode root = new MockNode();
        String content = "/parent/sub/prop=2";
        pie.createNodes(root, new ByteArrayInputStream(content.getBytes()));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCreateNodesFailingBecauseOfMissingTrailingSlash() throws Exception {
        String content = "parent/sub@uuid=1";
        pie.createNodes(null, new ByteArrayInputStream(content.getBytes()));
    }

    @Test(expected = Exception.class)
    public void testCreateNodesFailingBecauseOfDotAndMonkeyTail() throws Exception {
        String content = "/parent/sub@uuid=1";
        pie.createNodes(null, new ByteArrayInputStream(content.getBytes()));
    }

    @Test(expected = Exception.class)
    public void testCreateNodesFailingBecauseOfDotInPath() throws Exception {
        String content = "/parent.sub.@uuid=1";
        pie.createNodes(null, new ByteArrayInputStream(content.getBytes()));
    }
}
