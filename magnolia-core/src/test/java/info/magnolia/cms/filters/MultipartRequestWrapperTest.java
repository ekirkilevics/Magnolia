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
package info.magnolia.cms.filters;

import java.util.Enumeration;

import org.apache.commons.lang.ArrayUtils;

import com.mockrunner.mock.web.MockHttpServletRequest;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.test.MgnlTestCase;

public class MultipartRequestWrapperTest extends MgnlTestCase {

    public void testWillNotHideNewParametersAfterDoingForward() {

        MultipartForm form = new MultipartForm();
        addParameter(form, "key1", "value1");
        addParameter(form, "key2", "value2");
        addParameter(form, "key3", "value3-1");
        addParameter(form, "key3", "value3-2");

        MockHttpServletRequest mock = new MockHttpServletRequest();
        MultipartRequestWrapper request = new MultipartRequestWrapper(mock, form);

        assertEquals("value1", request.getParameter("key1"));
        assertEquals("value2", request.getParameter("key2"));
        assertNotNull(request.getParameter("key3"));
        assertEquals(3, request.getParameterMap().size());
        assertEquals("value2", ((String[])request.getParameterMap().get("key2"))[0]);
        assertEquals(1, request.getParameterValues("key1").length);
        assertEquals(2, request.getParameterValues("key3").length);
        assertEquals(3, sizeOfEnumeration(request.getParameterNames()));
        assertNull(request.getParameter("key4"));
        assertNull(request.getParameterValues("key4"));

        mock.setupAddParameter("key4", "value4");

        assertEquals("value4", request.getParameter("key4"));
        assertEquals(4, request.getParameterMap().size());
        assertEquals("value4", ((String[])request.getParameterMap().get("key4"))[0]);
        assertEquals(1, request.getParameterValues("key4").length);
        assertEquals(4, sizeOfEnumeration(request.getParameterNames()));
    }

    private int sizeOfEnumeration(Enumeration<?> e) {
        int n = 0;
        while (e.hasMoreElements()) {
            e.nextElement();
            n++;
        }
        return n;
    }

    private void addParameter(MultipartForm form, String name, String value) {
        form.addParameter(name, value);

        String[] values = form.getParameterValues(name);
        if (values == null) {
            form.addparameterValues(name, new String[]{value});
        }
        else {
            form.addparameterValues(name, (String[]) ArrayUtils.add(values, value));
        }
    }
}
