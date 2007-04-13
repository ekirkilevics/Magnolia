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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public interface WebContext extends Context {

    /**
     * Method used to initialize the context
     */
    public void init(HttpServletRequest request, HttpServletResponse response);

    /**
     * Get currently active page
     * @return content object
     */
    public Content getActivePage();

    /**
     * Get aggregated file, its used from image templates to manipulate
     * @return file object
     */
    public File getFile();

    /**
     * Get form object assembled by <code>MultipartRequestFilter</code>
     * @return multipart form object
     */
    public MultipartForm getPostedForm();

    /**
     * Get parameter value as string
     * @param name
     * @return parameter value
     */
    public String getParameter(String name);

    /**
     * Get parameter value as string
     * @return parameter values
     */
    public Map getParameters();

    /**
     * Get the current context path
     * @return
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
}