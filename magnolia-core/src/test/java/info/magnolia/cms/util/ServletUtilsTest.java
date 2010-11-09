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
package info.magnolia.cms.util;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.mockrunner.mock.web.MockHttpServletRequest;
import junit.framework.TestCase;

/**
 * Tests for {@link ServletUtils}.
 */
public class ServletUtilsTest extends TestCase {

    public void testServletInitParametersToMap() {
        LinkedHashMap<String, String> initParameters = new LinkedHashMap<String, String>();
        initParameters.put("key1", "value1");
        initParameters.put("key2", "value2");
        initParameters.put("key3", "value3");

        CustomServletConfig servletConfig = new CustomServletConfig("servlet", null, initParameters);

        LinkedHashMap<String, String> map = ServletUtils.initParametersToMap(servletConfig);
        assertEquals(3, map.size());
        assertEquals("value1", map.get("key1"));
        String[] strings = map.keySet().toArray(new String[3]);
        assertEquals("key1", strings[0]);
        assertEquals("key2", strings[1]);
        assertEquals("key3", strings[2]);
    }

    public void testFilterInitParametersToMap() {
        LinkedHashMap<String, String> initParameters = new LinkedHashMap<String, String>();
        initParameters.put("key1", "value1");
        initParameters.put("key2", "value2");
        initParameters.put("key3", "value3");

        CustomFilterConfig servletConfig = new CustomFilterConfig("filter", null, initParameters);

        LinkedHashMap<String, String> map = ServletUtils.initParametersToMap(servletConfig);
        assertEquals(3, map.size());
        assertEquals("value1", map.get("key1"));
        String[] strings = map.keySet().toArray(new String[3]);
        assertEquals("key1", strings[0]);
        assertEquals("key2", strings[1]);
        assertEquals("key3", strings[2]);
    }
    public void testGetWrappedRequest() {
        MockHttpServletRequest mock = new MockHttpServletRequest();
        HttpServletRequest request = new HttpServletRequestWrapper(mock) {};

        assertNotNull(ServletUtils.getWrappedRequest(request, MockHttpServletRequest.class));

        assertNull(ServletUtils.getWrappedRequest(mock, request.getClass()));
    }

    public void testIsMultipart() {
        MockHttpServletRequest mock = new MockHttpServletRequest();

        mock.setContentType("multipart/form-data");
        assertTrue(ServletUtils.isMultipart(mock));

        mock.setContentType("multipart/whatever");
        assertTrue(ServletUtils.isMultipart(mock));

        mock.setContentType("MULTIPART/form-data");
        assertTrue(ServletUtils.isMultipart(mock));

        mock.setContentType("text/html");
        assertFalse(ServletUtils.isMultipart(mock));
    }

    public void testIsForward() {
        MockHttpServletRequest mock = new MockHttpServletRequest();

        assertFalse(ServletUtils.isForward(mock));

        mock.setAttribute(ServletUtils.FORWARD_REQUEST_URI_ATTRIBUTE, "/test.html");
        assertTrue(ServletUtils.isForward(mock));

        mock.setAttribute(ServletUtils.INCLUDE_REQUEST_URI_ATTRIBUTE, "/test.jsp");
        assertFalse(ServletUtils.isForward(mock));
    }

    public void testIsInclude() {
        MockHttpServletRequest mock = new MockHttpServletRequest();

        assertFalse(ServletUtils.isInclude(mock));

        mock.setAttribute(ServletUtils.FORWARD_REQUEST_URI_ATTRIBUTE, "/test.html");
        assertFalse(ServletUtils.isInclude(mock));

        mock.setAttribute(ServletUtils.INCLUDE_REQUEST_URI_ATTRIBUTE, "/test.jsp");
        assertTrue(ServletUtils.isInclude(mock));
    }
}
