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
package info.magnolia.cms.util;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;

/**
 * Utility methods for operations related to Servlet API.
 *
 * @author tmattsson
 * @see info.magnolia.cms.util.RequestHeaderUtil
 */
public abstract class ServletUtils {

    public static final String FORWARD_REQUEST_URI_ATTRIBUTE = "javax.servlet.forward.request_uri";

    public static final String INCLUDE_REQUEST_URI_ATTRIBUTE = "javax.servlet.include.request_uri";

    public static final String ERROR_REQUEST_STATUS_CODE_ATTRIBUTE = "javax.servlet.error.status_code";

    /**
     * Returns the init parameters for a {@link javax.servlet.ServletConfig} object as a Map, preserving the order in which they are exposed
     * by the {@link javax.servlet.ServletConfig} object.
     */
    public static LinkedHashMap<String, String> initParametersToMap(ServletConfig config) {
        LinkedHashMap<String, String> initParameters = new LinkedHashMap<String, String>();
        Enumeration parameterNames = config.getInitParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = (String) parameterNames.nextElement();
            initParameters.put(parameterName, config.getInitParameter(parameterName));
        }
        return initParameters;
    }

    /**
     * Returns the init parameters for a {@link javax.servlet.FilterConfig} object as a Map, preserving the order in which they are exposed
     * by the {@link javax.servlet.FilterConfig} object.
     */
    public static LinkedHashMap<String, String> initParametersToMap(FilterConfig config) {
        LinkedHashMap<String, String> initParameters = new LinkedHashMap<String, String>();
        Enumeration parameterNames = config.getInitParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = (String) parameterNames.nextElement();
            initParameters.put(parameterName, config.getInitParameter(parameterName));
        }
        return initParameters;
    }

    /**
     * Finds a request wrapper object inside the chain of request wrappers.
     */
    public static <T extends ServletRequest> T getWrappedRequest(ServletRequest request, Class<T> clazz) {
        while (request != null) {
            if (clazz.isAssignableFrom(request.getClass())) {
                return (T) request;
            }
            request = (request instanceof ServletRequestWrapper) ? ((ServletRequestWrapper) request).getRequest() : null;
        }
        return null;
    }

    /**
     * Returns true if the request has a content type that indicates that is a multipart request.
     */
    public static boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    /**
     * Returns true if the request is currently processing a forward operation. This method will return false after
     * an include operation has begun and will return true after that include operations has completed.
     */
    public static boolean isForward(HttpServletRequest request) {
        return request.getAttribute(FORWARD_REQUEST_URI_ATTRIBUTE) != null && !isInclude(request);
    }

    /**
     * Returns true if the request is currently processing an include operation.
     */
    public static boolean isInclude(HttpServletRequest request) {
        return request.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE) != null;
    }

    /**
     * Returns true if the request is rendering an error page, either due to a call to sendError() or an exception
     * being thrown in a filter or a servlet that reached the container. Will return true also after an include() or
     * forward() while rendering the error page.
     */
    public static boolean isError(HttpServletRequest request) {
        return request.getAttribute(ERROR_REQUEST_STATUS_CODE_ATTRIBUTE) != null;
    }

    /**
     * Returns the dispatcher type of the request, the dispatcher type is defined to be identical to the semantics of
     * filter mappings in web.xml.
     */
    public static DispatcherType getDispatcherType(HttpServletRequest request) {
        // The order of these tests is important.
        if (request.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE) != null) {
            return DispatcherType.INCLUDE;
        }
        if (request.getAttribute(FORWARD_REQUEST_URI_ATTRIBUTE) != null) {
            return DispatcherType.FORWARD;
        }
        if (request.getAttribute(ERROR_REQUEST_STATUS_CODE_ATTRIBUTE) != null) {
            return DispatcherType.ERROR;
        }
        return DispatcherType.REQUEST;
    }
}
