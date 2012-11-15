/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.templating.elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.junit.Test;

/**
 * Test case for MarkupHelper.
 *
 * @version $Id$
 */
public class MarkupHelperTest {

    @Test
    public void testParam() throws Exception {
        final StringWriter out = new StringWriter();
        final MarkupHelper compo = new MarkupHelper(out);
        final String paramName = "param1";
        final String paramValue = "value1";
        compo.attribute(paramName, paramValue);
        assertEquals(out.toString(), " param1=\"value1\"", out.toString());
    }

    @Test
    public void testParamsKeepCamelCaseNotation() throws Exception {
        final StringWriter out = new StringWriter();
        final MarkupHelper compo = new MarkupHelper(out);
        final String paramName = "iAmACamelCaseParamName";
        final String paramValue = "iAmACamelCaseParamValue";
        compo.attribute(paramName, paramValue);
        assertEquals(out.toString(), " iAmACamelCaseParamName=\"iAmACamelCaseParamValue\"", out.toString());
    }

    @Test
    public void testNullContentAsEmptyString() throws Exception{
        final StringWriter out = new StringWriter();
        final MarkupHelper compo = new MarkupHelper(out);
        compo.startContent(null);
        assertTrue(out.toString().contains("cms:begin cms:content=\"\""));
        compo.endContent(null);
        assertTrue(out.toString().contains("cms:end cms:content=\"\""));
    }
}
