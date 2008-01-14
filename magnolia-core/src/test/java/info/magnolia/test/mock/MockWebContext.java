/**
 * This file Copyright (c) 2007-2008 Magnolia International
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

package info.magnolia.test.mock;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.context.WebContext;

/**
 * Implementation of mock context that also implements WebContext interface. Only methods needed for testing are implemented here.
 * @author had
 *
 */
public class MockWebContext extends MockContext implements WebContext {

    private PageContext pageContext;
    private AggregationState aggregationState = new AggregationState();
    

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#getActivePage()
     */
    public Content getActivePage() {
        return (Content) (pageContext == null ? null : pageContext.getAttribute("actPage"));
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#getAggregationState()
     */
    public AggregationState getAggregationState() {
        
        return aggregationState ;
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#getContextPath()
     */
    public String getContextPath() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#getFile()
     */
    public File getFile() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#getPageContext()
     */
    public PageContext getPageContext() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#getParameter(java.lang.String)
     */
    public String getParameter(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#getParameters()
     */
    public Map getParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#getPostedForm()
     */
    public MultipartForm getPostedForm() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#getRequest()
     */
    public HttpServletRequest getRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#getResponse()
     */
    public HttpServletResponse getResponse() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#getServletContext()
     */
    public ServletContext getServletContext() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#include(java.lang.String, java.io.Writer)
     */
    public void include(String path, Writer out) throws ServletException,
            IOException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#init(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void init(HttpServletRequest request, HttpServletResponse response) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#init(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.ServletContext)
     */
    public void init(HttpServletRequest request, HttpServletResponse response,
            ServletContext servletContext) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#resetAggregationState()
     */
    public void resetAggregationState() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#setPageContext(javax.servlet.jsp.PageContext)
     */
    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    /**
     * Set the AggregationState (required by some tests)
     * @param agState An AggregationState to use
     */
    public void setAggregationState(AggregationState agState) {
        this.aggregationState = agState;
    }
}
