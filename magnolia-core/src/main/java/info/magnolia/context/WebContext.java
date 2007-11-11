/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.context;

import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
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
     * Method used to initialize the context
     * @deprecated Use {@link #init(HttpServletRequest,HttpServletResponse,ServletContext)} instead
     */
    public void init(HttpServletRequest request, HttpServletResponse response);

    /**
     * Method used to initialize the context
     * @param servletContext
     * @todo
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
     * Get currently active page
     * @return content object
     * @deprecated use getAggregator().getMainContent()
     */
    public Content getActivePage();

    /**
     * Get aggregated file, its used from image templates to manipulate
     * @return file object
     * @deprecated use getAggregator().getFile()
     */
    public File getFile();

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
     * Sets the current jsp page context.
     * @param pageContext jsp page context
     */
    void setPageContext(PageContext pageContext);

    /**
     * Returns the current jsp page context, <strong>if any</strong>
     * @return jsp page context or null if it has not been populated by calling setPageContext
     */
    PageContext getPageContext();

}