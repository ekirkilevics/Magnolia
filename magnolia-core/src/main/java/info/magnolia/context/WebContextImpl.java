/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.context;

import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.DumperUtil;
import info.magnolia.cms.util.WorkspaceAccessUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.UnhandledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Id$
 */
public class WebContextImpl extends UserContextImpl implements WebContext {

    private static final Logger log = LoggerFactory.getLogger(WebContextImpl.class);

    private static final long serialVersionUID = 222L;

    private static final String REQUEST_JCRSESSION_PREFIX = WebContextImpl.class.getName() + ".mgnlRepositorySession_";

    private static final String REQUEST_HIERARCHYMANAGER_PREFIX = WebContextImpl.class.getName() + ".mgnlHMgr_";

    private static final String REQUEST_AGGREGATIONSTATE = AggregationState.class.getName();

    private static final String SESSION_ACCESSMANAGER_PREFIX = WebContextImpl.class.getName() + ".mgnlAccessMgr_";

    private static final String SESSION_USER = WebContextImpl.class.getName() + ".user";

    private HttpServletRequest request;

    private HttpServletResponse response;

    private ServletContext servletContext;

    /**
     * the jsp page context.
     */
    private PageContext pageContext;

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
        setAttributeStrategy(new RequestAttributeStrategy(request));  
        setRepositoryStrategy(new AuthRepositoryStrategy(this));
    }

    /**
     * Create the subject on demand.
     * @see info.magnolia.context.AbstractContext#getUser()
     */

    public User getUser() {
        if (user == null) {
            user = (User) getAttribute(SESSION_USER, Context.SESSION_SCOPE);
            if (user == null) {
                user = Security.getUserManager().getUser(Authenticator.getSubject(request));
                setUser(user);
            }
        }
        return this.user;
    }

    /**
     * In addition to the field user we put the user object into the session as well.
     * @param user magnolia User
     */
    public void setUser(User user) {
        super.setUser(user);
        setAttribute(SESSION_USER, user, Context.SESSION_SCOPE);
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
        AggregationState aggregationState = (AggregationState) request.getAttribute(REQUEST_AGGREGATIONSTATE);
        if (aggregationState == null) {
            aggregationState = new AggregationState();
            setAttribute(REQUEST_AGGREGATIONSTATE, aggregationState, LOCAL_SCOPE);
        }
        return aggregationState;
    }

    public void resetAggregationState() {
        removeAttribute(REQUEST_AGGREGATIONSTATE, LOCAL_SCOPE);
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

    public void include(final String path, final Writer out) throws ServletException, IOException {
        try {
            final WriterResponseWrapper wrappedResponse = new WriterResponseWrapper(response, out);
            request.getRequestDispatcher(path).include(request, wrappedResponse);
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

    /**
     * Closes opened JCR sessions and invalidates the current HttpSession.
     * @see #release()
     */
    public void logout() {
        release();

        HttpSession session = this.request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Closes opened JCR sessions.
     */
    public void release() {

        Enumeration attributes = request.getAttributeNames();
        while (attributes.hasMoreElements()) {
            String key = (String) attributes.nextElement();

            if (key.startsWith(REQUEST_JCRSESSION_PREFIX)) {
                final Object objSession = request.getAttribute(key);

                // don't leave dead jcr sessions around
                request.removeAttribute(key);

                if (objSession instanceof Session) {
                    final Session jcrSession = (Session) objSession;
                    try {
                        if (jcrSession.isLive()) {

                            if (jcrSession.hasPendingChanges()) {
                                log
                                    .error("the current jcr session has pending changes but shouldn't please set to debug level to see the dumped details");
                                if (log.isDebugEnabled()) {
                                    PrintWriter pw = new PrintWriter(System.out);
                                    DumperUtil.dumpChanges(jcrSession, pw);
                                    pw.flush();
                                }

                                log.warn("will refresh (cleanup) the session {}", key);
                                jcrSession.refresh(false);
                            }

                            jcrSession.logout();
                            log.debug("logging out from session {}", key);
                        }
                    }
                    catch (Throwable t) {
                        log.warn("Failed to close JCR session " + key, t);
                    }
                }
            }
            else if (key.startsWith(REQUEST_HIERARCHYMANAGER_PREFIX)) {
                // session is already closed by the above check
                request.removeAttribute(key);
            }
        }
    }
}
