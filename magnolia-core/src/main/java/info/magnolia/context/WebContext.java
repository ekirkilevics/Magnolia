/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.context;

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.AggregationState;

import java.util.Map;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public interface WebContext extends Context {

    /**
     * Attribute name to get the requests character encoding
     * @deprecated use AggregationState
     */
    public static final String ATTRIBUTE_REQUEST_CHARACTER_ENCODING = "characterEncoding";

    /**
     * Attribute name to get the request uri
     * @deprecated use AggregationState
     */
    public static final String ATTRIBUTE_REQUEST_URI = "requestURI";

    /**
     * Method used to initialize the context.
     */
    public void init(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext);

    /**
     * Retrieves the Aggregator instance, which gathers all info regarding the current request (paths, etc)
     * @return
     */
    public AggregationState getAggregationState();

    /**
     * Resets the current aggregator instance.
     */
    void resetAggregationState();

    /**
     * Get form object assembled by <code>MultipartRequestFilter</code>
     * @return multipart form object
     */
    public MultipartForm getPostedForm();

    /**
     * Get parameter value as string
     * @return parameter value
     */
    public String getParameter(String name);

    /**
     * Get parameter value as a Map<String, String>
     * @return parameter values
     */
    public Map getParameters();

    /**
     * Get the current context path.
     */
    public String getContextPath();

    /**
     * Avoid calls to this method wherever possible.
     * @return Returns the request.
     */
    public HttpServletRequest getRequest();

    /**
     * Avoid depending on this as much as possible.
     */
    public HttpServletResponse getResponse();

    /**
     * Returns the current servlet context.
     * @return ServletContext instance
     */
    ServletContext getServletContext();

    /**
     * Includes/render the given path into the given Writer, by wrapping it in the current HttpServletResponse.
     * @see javax.servlet.ServletRequest#getRequestDispatcher(String)
     */
    void include(final String path, final Writer out) throws ServletException, IOException;

    /**
     * Sets the current jsp page context. Callers should take care of appropriately unset it
     * once done with it. Typically a jsp renderer will setPageContext(null) after having rendered
     * a jsp.
     * @param pageContext jsp page context
     */
    void setPageContext(PageContext pageContext);

    /**
     * Returns the current jsp page context, <strong>if any</strong>
     * @return jsp page context or null if it has not been populated by calling setPageContext
     */
    PageContext getPageContext();

    /**
     * @param request
     * @param response
     */
    public void push(HttpServletRequest request, HttpServletResponse response);

    /**
     *
     */
    public void pop();

    /**
     * Get parameter values as string[]
     * @return parameter values
     */
    public String[] getParameterValues(String name);

}
