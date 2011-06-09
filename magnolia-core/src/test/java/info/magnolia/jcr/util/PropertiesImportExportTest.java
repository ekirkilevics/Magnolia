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

import java.util.Calendar;

import org.junit.After;
import org.junit.Test;

/**
 * @version $Id$
 */
public class PropertiesImportExportTest {

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testConvertsToWrapperType() {
        final PropertiesImportExport pie = new PropertiesImportExport();

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
        final PropertiesImportExport pie = new PropertiesImportExport();

        assertEquals(Integer.valueOf(37), pie.convertPropertyStringToObject("int:37"));
    }
}
