/**
 * This file Copyright (c) 2003-2008 Magnolia International
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

import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.Security;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Id$
 */
public class WebContextImpl extends UserContextImpl implements WebContext {

    private static final Logger log = LoggerFactory.getLogger(WebContextImpl.class);

    private static final long serialVersionUID = 222L;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private ServletContext servletContext;

    /**
     * the jsp page context.
     */
    private PageContext pageContext;

    protected AggregationState aggregationState;

    /**
     * Use init to initialize the object.
     */
    public WebContextImpl() {
    }

    /**
     * @deprecated Use {@link #init(HttpServletRequest,HttpServletResponse,ServletContext)} instead
     */
    public void init(HttpServletRequest request, HttpServletResponse response) {
        init(request, response, null);
    }

    public void init(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        this.request = request;
        this.response = response;
        this.servletContext = servletContext;
        //reset();
        //setUser(getAnonymousUser());
        setAttributeStrategy(new RequestAttributeStrategy(request));
        setRepositoryStrategy(new DefaultRepositoryStrategy(this));
    }

    /**
     * Get currently active page
     * @return content object
     * @deprecated use getAggregationState().getMainContent();
     */
    public Content getActivePage() {
        return getAggregationState().getMainContent();
    }

    /**
     * Get aggregated file, its used from image templates to manipulate
     * @return file object
     * @deprecated use getAggregationState().getFile();
     */
    public File getFile() {
        return getAggregationState().getFile();
    }

    public AggregationState getAggregationState() {
        if (aggregationState == null) {
            aggregationState = new AggregationState();
        }
        return aggregationState;
    }

    public void resetAggregationState() {
        aggregationState = null;
    }

    /**
     * Get form object assembled by <code>MultipartRequestFilter</code>
     * @return multipart form object
     */
    public MultipartForm getPostedForm() {
        return (MultipartForm) getAttribute(MultipartForm.REQUEST_ATTRIBUTE_NAME, LOCAL_SCOPE);
    }

    /**
     * Get parameter value as string.
     * @return parameter value
     */
    public String getParameter(String name) {
        return this.request.getParameter(name);
    }

    /**
     * Get parameter values as a Map<String, String> (unlike HttpServletRequest.getParameterMap() which returns a Map<String,
     * String[]>, so don't expect to retrieve multiple-valued form parameters here)
     * @return parameter values
     */
    public Map getParameters() {
        Map map = new HashMap();
        Enumeration paramEnum = this.request.getParameterNames();
        while (paramEnum.hasMoreElements()) {
            final String name = (String) paramEnum.nextElement();
            map.put(name, this.request.getParameter(name));
        }
        return map;
    }

    /**
     * Avoid the call to this method where ever possible.
     * @return Returns the request.
     */
    public HttpServletRequest getRequest() {
        return this.request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public String getContextPath() {
        return this.request.getContextPath();
    }

    /**
     * Does an include using the request that was set when setting up this context, or using the
     * request wrapped by the pageContext if existing.
     */
    public void include(final String path, final Writer out) throws ServletException, IOException {
        try {
            final ServletRequest requestToUse = pageContext != null ? pageContext.getRequest() : this.getRequest();
            final HttpServletResponse responseToUse = (pageContext != null && pageContext.getResponse() instanceof HttpServletResponse) ? (HttpServletResponse) pageContext.getResponse() : response;
            final WriterResponseWrapper wrappedResponse = new WriterResponseWrapper(responseToUse, out);
            requestToUse.getRequestDispatcher(path).include(requestToUse, wrappedResponse);
        }
        catch (ServletException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public PageContext getPageContext() {
        return pageContext;
    }

    /**
     * {@inheritDoc}
     */
    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    /**
     * {@inheritDoc}
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    public void login() {
        setRepositoryStrategy(new DefaultRepositoryStrategy(this));
    }

    /**
     * Closes opened JCR sessions and invalidates the current HttpSession.
     * @see #release()
     */
    public void logout() {
        releaseJCRSessions();

        HttpSession session = this.request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        login(Security.getAnonymousUser());
    }

    /**
     * Closes opened JCR sessions.
     */
    public void release() {
        releaseJCRSessions();
        this.request = null;
        this.response = null;
    }

    protected void releaseJCRSessions() {
        getRepositoryStrategy().release();
    }
}
