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

import info.magnolia.cms.beans.config.ContentRepository;
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
import java.util.Iterator;
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

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Id$
 */
public class WebContextImpl extends AbstractContext implements WebContext {

    private static final Logger log = LoggerFactory.getLogger(WebContextImpl.class);

    private static final long serialVersionUID = 222L;

    private static final String ATTRIBUTE_REPOSITORY_REQUEST_PREFIX = "mgnlRepositorySession_";

    private static final String ATTRIBUTE_HM_PREFIX = "mgnlHMgr_";

    private static final String ATTRIBUTE_AM_PREFIX = "mgnlAccessMgr_";

    private static final String ATTRIBUTE_AGGREGATIONSTATE = AggregationState.class.getName();

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
    }

    /**
     * Create the subject on demand.
     * @see info.magnolia.context.AbstractContext#getUser()
     */

    public User getUser() {
        if (user == null) {
            user = (User) getAttribute("user", Context.SESSION_SCOPE);
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
        setAttribute("user", user, Context.SESSION_SCOPE);
    }

    /**
     * Get repository session
     */
    protected Session getRepositorySession(String repositoryName, String workspaceName) throws LoginException,
        RepositoryException {
        Session jcrSession = null;

        final String repoSessAttrName = ATTRIBUTE_REPOSITORY_REQUEST_PREFIX + repositoryName + "_" + workspaceName;

        // don't use httpsession, jcr session is not serializable at all
        jcrSession = (Session) getAttribute(repoSessAttrName, LOCAL_SCOPE);

        log.debug("getRepositorySession {} (from request? {})", repoSessAttrName, BooleanUtils.toBooleanObject(jcrSession != null));

        if (jcrSession == null) {
            long time = System.currentTimeMillis();
            WorkspaceAccessUtil util = WorkspaceAccessUtil.getInstance();
            jcrSession = util.createRepositorySession(util.getDefaultCredentials(), repositoryName, workspaceName);
            log.debug("creating a new session took {} ms", new Long(System.currentTimeMillis() - time));

            setAttribute(repoSessAttrName, jcrSession, LOCAL_SCOPE);
        }
        return jcrSession;
    }

    /**
     * Get hierarchy manager initialized for this user.
     */
    public HierarchyManager getHierarchyManager(String repositoryName, String workspaceName) {
        HttpSession httpSession = request.getSession(false);
        HierarchyManager hm = null;
        final String hmAttrName = ATTRIBUTE_HM_PREFIX + repositoryName + "_" + workspaceName;
        if (httpSession != null) {
            hm = (HierarchyManager) httpSession.getAttribute(hmAttrName);
        }

        log.debug("getHierarchyManager (from session? {})", BooleanUtils.toBooleanObject(hm != null));
        if (hm == null) {
            WorkspaceAccessUtil util = WorkspaceAccessUtil.getInstance();
            try {
                hm = util.createHierarchyManager(this.getUser().getName(), getRepositorySession(
                    repositoryName,
                    workspaceName), getAccessManager(repositoryName, workspaceName), getQueryManager(
                    repositoryName,
                    workspaceName));
                if (httpSession != null) {
                    setAttribute("hmAttrName", hm, Context.SESSION_SCOPE);//$NON-NLS-1$
                }
            }
            catch (Throwable t) {
                log.error("Failed to create HierarchyManager", t);
            }
        }

        return hm;
    }

    /**
     * Get access manager for the specified repository on the specified workspace.
     */
    public AccessManager getAccessManager(String repositoryName, String workspaceName) {
        HttpSession httpSession = request.getSession(false);
        AccessManager accessManager = null;

        final String amAttrName = ATTRIBUTE_AM_PREFIX + repositoryName + "_" + workspaceName;
        if (httpSession != null) {
            accessManager = (AccessManager) httpSession.getAttribute(amAttrName);
        }

        log.debug("getAccessManager (from session? {})", BooleanUtils.toBooleanObject(accessManager != null));

        if (accessManager == null) {
            accessManager = WorkspaceAccessUtil.getInstance().createAccessManager(
                getSubject(),
                repositoryName,
                workspaceName);
            if (httpSession != null) {
                setAttribute(amAttrName, accessManager, SESSION_SCOPE);
            }
        }

        return accessManager;
    }

    protected Subject getSubject() {
        Subject subject = Authenticator.getSubject(request);
        return subject;
    }

    /**
     * Get QueryManager created for this user on the specified repository and workspace.
     */
    public QueryManager getQueryManager(String repositoryName, String workspaceName) {
        QueryManager queryManager = null;

        log.debug("getQueryManager");

        try {
            queryManager = WorkspaceAccessUtil.getInstance().createQueryManager(
                getRepositorySession(repositoryName, workspaceName),
                getAccessManager(repositoryName, workspaceName));
        }
        catch (Throwable t) {
            log.error("Failed to create QueryManager", t);
        }

        return queryManager;
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
        AggregationState aggregationState = (AggregationState) request.getAttribute(ATTRIBUTE_AGGREGATIONSTATE);
        if (aggregationState == null) {
            aggregationState = new AggregationState();
            setAttribute(ATTRIBUTE_AGGREGATIONSTATE, aggregationState, LOCAL_SCOPE);
        }
        return aggregationState;
    }

    public void resetAggregationState() {
        removeAttribute(ATTRIBUTE_AGGREGATIONSTATE, LOCAL_SCOPE);
    }

    /**
     * Get form object assembled by <code>MultipartRequestFilter</code>
     * @return multipart form object
     */
    public MultipartForm getPostedForm() {
        return (MultipartForm) this.request.getAttribute("multipartform"); //$NON-NLS-1$
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
     * Set attribute value, scope of the attribute is defined.
     * @param name is used as a key
     * @param value
     * @param scope , highest level of scope from which this attribute is visible
     */
    public void setAttribute(String name, Object value, int scope) {

        if (value == null) {
            removeAttribute(name, scope);
            return;
        }

        switch (scope) {
            case Context.LOCAL_SCOPE:
                this.request.setAttribute(name, value);
                break;
            case Context.SESSION_SCOPE:
                if (!(value instanceof Serializable)) {
                    log.warn("Trying to store a non-serializable attribute in session: "
                        + name
                        + ". Object type is "
                        + value.getClass().getName(), new Throwable(
                        "This stacktrace has been added to provide debugging information"));
                    return;
                }

                HttpSession httpsession = request.getSession(false);
                if (httpsession == null) {
                    log
                        .warn(
                            "Session initialized in order to setting attribute '{}' to '{}'. You should avoid using session when possible!",
                            name,
                            value);
                    httpsession = request.getSession(true);
                }

                httpsession.setAttribute(name, value);
                break;
            case Context.APPLICATION_SCOPE:
                MgnlContext.getSystemContext().setAttribute(name, value, Context.APPLICATION_SCOPE);
                break;
            default:
                this.request.setAttribute(name, value);
                if (log.isDebugEnabled()) {
                    log.debug("Undefined scope, setting attribute [{}] in request scope", name);
                }
        }
    }

    /**
     * Get attribute value.
     * @param name to which value is associated to
     * @return attribute value
     */
    public Object getAttribute(String name, int scope) {
        switch (scope) {
            case Context.LOCAL_SCOPE:
                Object obj = this.request.getAttribute(name);
                if (obj == null) {
                    obj = this.getParameter(name);
                }
                if (obj == null) {
                    // we also expose some of the request properties as attributes
                    if (ATTRIBUTE_REQUEST_CHARACTER_ENCODING.equals(name)) {
                        obj = request.getCharacterEncoding();
                    }
                    else if (ATTRIBUTE_REQUEST_URI.equals(name)) {
                        obj = request.getRequestURI();
                    }
                }
                return obj;
            case Context.SESSION_SCOPE:
                HttpSession httpsession = request.getSession(false);
                if (httpsession == null) {
                    return null;
                }
                return httpsession.getAttribute(name);
            case Context.APPLICATION_SCOPE:
                return MgnlContext.getSystemContext().getAttribute(name, Context.APPLICATION_SCOPE);
            default:
                log.error("illegal scope passed");
                return null;
        }
    }

    /**
     * Remove an attribute.
     */
    public void removeAttribute(String name, int scope) {
        switch (scope) {
            case Context.LOCAL_SCOPE:
                this.request.removeAttribute(name);
                break;
            case Context.SESSION_SCOPE:
                HttpSession httpsession = request.getSession(false);
                if (httpsession != null) {
                    httpsession.removeAttribute(name);
                }
                break;
            case Context.APPLICATION_SCOPE:
                MgnlContext.getSystemContext().removeAttribute(name, Context.APPLICATION_SCOPE);
                break;
            default:
                log.error("no illegal scope passed");
        }
    }

    /**
     * Get a map represenation of the scope.
     */
    public final Map getAttributes(int scope) {
        Map map = new HashMap();
        Enumeration keysEnum;
        switch (scope) {
            case Context.LOCAL_SCOPE:
                // add parameters
                map.putAll(this.getParameters());
                // attributes have higher priority
                keysEnum = this.request.getAttributeNames();
                while (keysEnum.hasMoreElements()) {
                    String key = (String) keysEnum.nextElement();
                    Object value = getAttribute(key, scope);
                    map.put(key, value);
                }
                return map;
            case Context.SESSION_SCOPE:
                HttpSession httpsession = request.getSession(false);
                if (httpsession == null) {
                    return null;
                }
                keysEnum = httpsession.getAttributeNames();
                while (keysEnum.hasMoreElements()) {
                    String key = (String) keysEnum.nextElement();
                    Object value = getAttribute(key, scope);
                    map.put(key, value);
                }
                return map;
            case Context.APPLICATION_SCOPE:
                return MgnlContext.getSystemContext().getAttributes(Context.APPLICATION_SCOPE);
            default:
                log.error("no illegal scope passed");
                return null;
        }

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

            if (key.startsWith(WebContextImpl.ATTRIBUTE_REPOSITORY_REQUEST_PREFIX)) {
                final Object objSession = request.getAttribute(key);

                // don't leave dead jcr sessions around
                request.removeAttribute(key);

                if (objSession instanceof Session) {
                    final Session jcrSession = (Session) objSession;
                    try {
                        if (jcrSession.isLive()) {

                            if (jcrSession.hasPendingChanges()) {
                                log.error("the current jcr session has pending changes but shouldn't please set to debug level to see the dumped details");
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
        }
    }
}
