/**
 * This file Copyright (c) 2010-2012 Magnolia International
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

import static org.junit.Assert.*;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.junit.Test;

import com.mockrunner.mock.web.MockHttpServletRequest;

/**
 * Tests for {@link ServletUtil}.
 */
public class ServletUtilTest {

    @Test
    public void testServletInitParametersToMap() {
        LinkedHashMap<String, String> initParameters = new LinkedHashMap<String, String>();
        initParameters.put("key1", "value1");
        initParameters.put("key2", "value2");
        initParameters.put("key3", "value3");

        CustomServletConfig servletConfig = new CustomServletConfig("servlet", null, initParameters);

        LinkedHashMap<String, String> map = ServletUtil.initParametersToMap(servletConfig);
        assertEquals(3, map.size());
        assertEquals("value1", map.get("key1"));
        String[] strings = map.keySet().toArray(new String[3]);
        assertEquals("key1", strings[0]);
        assertEquals("key2", strings[1]);
        assertEquals("key3", strings[2]);
    }

    @Test
    public void testFilterInitParametersToMap() {
        LinkedHashMap<String, String> initParameters = new LinkedHashMap<String, String>();
        initParameters.put("key1", "value1");
        initParameters.put("key2", "value2");
        initParameters.put("key3", "value3");

        CustomFilterConfig servletConfig = new CustomFilterConfig("filter", null, initParameters);

        LinkedHashMap<String, String> map = ServletUtil.initParametersToMap(servletConfig);
        assertEquals(3, map.size());
        assertEquals("value1", map.get("key1"));
        String[] strings = map.keySet().toArray(new String[3]);
        assertEquals("key1", strings[0]);
        assertEquals("key2", strings[1]);
        assertEquals("key3", strings[2]);
    }
    @Test
    public void testGetWrappedRequest() {
        MockHttpServletRequest mock = new MockHttpServletRequest();
        HttpServletRequest request = new HttpServletRequestWrapper(mock) {};

        assertNotNull(ServletUtil.getWrappedRequest(request, MockHttpServletRequest.class));

        assertNull(ServletUtil.getWrappedRequest(mock, request.getClass()));
    }

    @Test
    public void testIsMultipart() {
        MockHttpServletRequest mock = new MockHttpServletRequest();

        mock.setContentType("multipart/form-data");
        assertTrue(ServletUtil.isMultipart(mock));

        mock.setContentType("multipart/whatever");
        assertTrue(ServletUtil.isMultipart(mock));

        mock.setContentType("MULTIPART/form-data");
        assertTrue(ServletUtil.isMultipart(mock));

        mock.setContentType("text/html");
        assertFalse(ServletUtil.isMultipart(mock));
    }

    @Test
    public void testIsForward() {
        MockHttpServletRequest mock = new MockHttpServletRequest();

        assertFalse(ServletUtil.isForward(mock));

        mock.setAttribute(ServletUtil.FORWARD_REQUEST_URI_ATTRIBUTE, "/test.html");
        assertTrue(ServletUtil.isForward(mock));

        mock.setAttribute(ServletUtil.INCLUDE_REQUEST_URI_ATTRIBUTE, "/test.jsp");
        assertFalse(ServletUtil.isForward(mock));
    }

    @Test
    public void testIsInclude() {
        MockHttpServletRequest mock = new MockHttpServletRequest();

        assertFalse(ServletUtil.isInclude(mock));

        mock.setAttribute(ServletUtil.FORWARD_REQUEST_URI_ATTRIBUTE, "/test.html");
        assertFalse(ServletUtil.isInclude(mock));

        mock.setAttribute(ServletUtil.INCLUDE_REQUEST_URI_ATTRIBUTE, "/test.jsp");
        assertTrue(ServletUtil.isInclude(mock));
    }

    @Test
    public void testIsError() {
        MockHttpServletRequest mock = new MockHttpServletRequest();

        assertFalse(ServletUtil.isError(mock));
        assertFalse(ServletUtil.isForward(mock));
        assertFalse(ServletUtil.isInclude(mock));

        mock.setAttribute(ServletUtil.ERROR_REQUEST_STATUS_CODE_ATTRIBUTE, 500);
        assertTrue(ServletUtil.isError(mock));
        assertFalse(ServletUtil.isForward(mock));
        assertFalse(ServletUtil.isInclude(mock));

        mock.setAttribute(ServletUtil.FORWARD_REQUEST_URI_ATTRIBUTE, "/test.html");
        assertTrue(ServletUtil.isError(mock));
        assertTrue(ServletUtil.isForward(mock));
        assertFalse(ServletUtil.isInclude(mock));

        mock.setAttribute(ServletUtil.INCLUDE_REQUEST_URI_ATTRIBUTE, "/test.jsp");
        assertTrue(ServletUtil.isError(mock));
        assertFalse(ServletUtil.isForward(mock));
        assertTrue(ServletUtil.isInclude(mock));
    }

    @Test
    public void testGetDispatcherType() {

        MockHttpServletRequest mock = new MockHttpServletRequest();

        assertEquals(DispatcherType.REQUEST, ServletUtil.getDispatcherType(mock));

        mock.setAttribute(ServletUtil.FORWARD_REQUEST_URI_ATTRIBUTE, "/test.html");
        assertEquals(DispatcherType.FORWARD, ServletUtil.getDispatcherType(mock));

        mock.setAttribute(ServletUtil.INCLUDE_REQUEST_URI_ATTRIBUTE, "/test.jsp");
        assertEquals(DispatcherType.INCLUDE, ServletUtil.getDispatcherType(mock));

        mock.removeAttribute(ServletUtil.INCLUDE_REQUEST_URI_ATTRIBUTE);
        mock.removeAttribute(ServletUtil.FORWARD_REQUEST_URI_ATTRIBUTE);
        mock.setAttribute(ServletUtil.ERROR_REQUEST_STATUS_CODE_ATTRIBUTE, 500);
        assertEquals(DispatcherType.ERROR, ServletUtil.getDispatcherType(mock));

        mock.setAttribute(ServletUtil.FORWARD_REQUEST_URI_ATTRIBUTE, "/error.html");
        assertEquals(DispatcherType.FORWARD, ServletUtil.getDispatcherType(mock));

        mock.setAttribute(ServletUtil.INCLUDE_REQUEST_URI_ATTRIBUTE, "/error.jsp");
        assertEquals(DispatcherType.INCLUDE, ServletUtil.getDispatcherType(mock));
    }

    @Test
    public void testGetOriginalRequestUri() {
        MockHttpServletRequest mock = new MockHttpServletRequest();
        mock.setRequestURI("/some/path/and/some.file");

        assertEquals("/some/path/and/some.file", ServletUtil.getOriginalRequestURI(mock));

        mock.setAttribute(ServletUtil.FORWARD_REQUEST_URI_ATTRIBUTE, mock.getRequestURI());
        mock.setRequestURI("/forwarded/to/test/path");
        assertEquals("/some/path/and/some.file", ServletUtil.getOriginalRequestURI(mock));
    }

    @Test
    public void testGetOriginalRequestUrlIncludingQueryString() {
        MockHttpServletRequest mock = new MockHttpServletRequest();
        mock.setRequestURL("http://some.domain/foo/bar.html");
        mock.setRequestURI("/foo/bar.html");
        mock.setQueryString("a=5&b=6");

        assertEquals("http://some.domain/foo/bar.html?a=5&b=6", ServletUtil.getOriginalRequestURLIncludingQueryString(mock));

        mock.setAttribute(ServletUtil.FORWARD_REQUEST_URI_ATTRIBUTE, mock.getRequestURI());
        mock.setAttribute(ServletUtil.FORWARD_QUERY_STRING_ATTRIBUTE, mock.getQueryString());
        mock.setScheme("http");
        mock.setServerName("some.domain");
        mock.setServerPort(80);

        mock.setRequestURL("/forwarded/to/test/path");
        mock.setQueryString("qwerty=yes");
        assertEquals("http://some.domain/foo/bar.html?a=5&b=6", ServletUtil.getOriginalRequestURLIncludingQueryString(mock));

        mock.setServerPort(8080);
        assertEquals("http://some.domain:8080/foo/bar.html?a=5&b=6", ServletUtil.getOriginalRequestURLIncludingQueryString(mock));
    }
}
