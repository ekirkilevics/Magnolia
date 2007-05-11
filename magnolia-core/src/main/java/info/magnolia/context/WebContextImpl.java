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

import info.magnolia.api.HierarchyManager;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Aggregator;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.DumperUtil;
import info.magnolia.cms.util.WorkspaceAccessUtil;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Sameer Charles
 * @version $Id$
 */
public class WebContextImpl extends AbstractContext implements WebContext {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebContextImpl.class);

    private static final long serialVersionUID = 222L;

    private static final String ATTRIBUTE_REPOSITORY_SESSION_PREFIX = "mgnlRepositorySession_";

    private static final String ATTRIBUTE_HM_PREFIX = "mgnlHMgr_";

    private static final String ATTRIBUTE_AM_PREFIX = "mgnlAccessMgr_";

    private static final String ATTRIBUTE_QM_PREFIX = "mgnlQueryMgr_";

    private HttpServletRequest request;

    private HttpServletResponse response;

    /**
     * Use init to initialize the object.
     */
    public WebContextImpl() {
    }

    public void init(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * Create the subject on demand.
     *
     * @see info.magnolia.context.AbstractContext#getUser()
     */
    public User getUser() {
        if (this.user == null) {
            this.user = Security.getUserManager().getUser(Authenticator.getSubject(request));
        }
        return this.user;
    }

    /**
     * Make the locale available for JSTL
     */
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        this.setAttribute(Config.FMT_LOCALE + ".session", locale, Context.SESSION_SCOPE); //$NON-NLS-1$
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
        if (hm == null) {
            WorkspaceAccessUtil util = WorkspaceAccessUtil.getInstance();
            try {
                hm = util.createHierarchyManager(this.getUser().getName(),
                        getRepositorySession(repositoryName, workspaceName),
                        getAccessManager(repositoryName, workspaceName),
                        getQueryManager(repositoryName, workspaceName));
                if (httpSession != null) {
                    httpSession.setAttribute(hmAttrName, hm); //$NON-NLS-1$
                }
            } catch (Throwable t) {
                log.error("Failed to create HierarchyManager", t);
            }
        }

        //TODO remove this after we found the session refreshing issues
        // check only once per session
        if (this.request.getAttribute("jcr.session. " + repositoryName + ".checked") == null) {
            this.request.setAttribute("jcr.session. " + repositoryName + ".checked", "true");
            try {
                if (hm.getWorkspace().getSession().hasPendingChanges()) {
                    log.error("the current jcr session has pending changes but shouldn't please set to debug level to see the dumped details");
                    if (log.isDebugEnabled()) {
                        DumperUtil.dumpChanges(hm);
                    }
                    log.warn("will refresh (cleanup) the session");
                    hm.getWorkspace().getSession().refresh(false);
                }
            }
            catch (RepositoryException e) {
                log.error("wasn't able to check pending changes on the session", e);
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

        if (accessManager == null) {
            Subject subject = Authenticator.getSubject(request);
            accessManager = WorkspaceAccessUtil.getInstance().createAccessManager(subject, repositoryName, workspaceName);
            if (httpSession != null) {
                httpSession.setAttribute(amAttrName, accessManager);
            }
        }

        return accessManager;
    }

    /**
     * Get QueryManager created for this user on the specified repository and workspace.
     */
    public QueryManager getQueryManager(String repositoryName, String workspaceName) {
        QueryManager queryManager = null;

        HttpSession httpSession = request.getSession(false);
        final String qmAttrName = ATTRIBUTE_QM_PREFIX + repositoryName + "_" + workspaceName;
        if (httpSession != null) {
            queryManager = (QueryManager) httpSession.getAttribute(qmAttrName);
        }
        if (queryManager == null) {
            try {
                queryManager = WorkspaceAccessUtil.getInstance().createQueryManager(
                        getRepositorySession(repositoryName, workspaceName),
                        getAccessManager(repositoryName, workspaceName));
            } catch (Throwable t) {
                log.error("Failed to create QueryManager", t);
            }
            if (httpSession != null) {
                httpSession.setAttribute(qmAttrName, queryManager);
            }
        }

        return queryManager;
    }

    /**
     * Get currently active page
     *
     * @return content object
     */
    public Content getActivePage() {
        return Aggregator.getMainContent();
    }

    /**
     * Get aggregated file, its used from image templates to manipulate
     *
     * @return file object
     */
    public File getFile() {
        return Aggregator.getFile();
    }

    /**
     * Get form object assembled by <code>MultipartRequestFilter</code>
     *
     * @return multipart form object
     */
    public MultipartForm getPostedForm() {
        return (MultipartForm) this.request.getAttribute("multipartform"); //$NON-NLS-1$
    }

    /**
     * Get parameter value as string.
     *
     * @return parameter value
     */
    public String getParameter(String name) {
        return this.request.getParameter(name);
    }

    /**
     * Get parameter values as a Map<String, String>
     * (unlike HttpServletRequest.getParameterMap() which returns a Map<String, String[]>,
     * so don't expect to retrieve multiple-valued form parameters here)
     *
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
     *
     * @param name  is used as a key
     * @param value
     * @param scope , highest level of scope from which this attribute is visible
     */
    public void setAttribute(String name, Object value, int scope) {
        switch (scope) {
            case Context.LOCAL_SCOPE:
                this.request.setAttribute(name, value);
                break;
            case Context.SESSION_SCOPE:
                HttpSession httpsession = request.getSession(false);
                if (httpsession == null) {
                    log.warn("Session initialized in order to setting attribute '{}' to '{}'. You should avoid using session when possible!", name, value);
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
     *
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
                    } else if (ATTRIBUTE_REQUEST_URI.equals(name)) {
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
     * Get repository session
     */
    private Session getRepositorySession(String repositoryName, String workspaceName) throws LoginException, RepositoryException {
        Session jcrSession = null;
        HttpSession httpSession = request.getSession(false);

        final String repoSessAttrName = ATTRIBUTE_REPOSITORY_SESSION_PREFIX + repositoryName + "_" + workspaceName;
        if (httpSession != null) {
            jcrSession = (Session) httpSession.getAttribute(repoSessAttrName);
        }
        if (jcrSession == null) {
            WorkspaceAccessUtil util = WorkspaceAccessUtil.getInstance();
            jcrSession = util.createRepositorySession(util.getDefaultCredentials(), repositoryName, workspaceName);
            if (httpSession != null) {
                httpSession.setAttribute(repoSessAttrName, jcrSession);
            }

        }
        return jcrSession;
    }

    /**
     * Avoid the call to this method where ever possible.
     *
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
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
