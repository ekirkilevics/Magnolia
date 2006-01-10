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
package info.magnolia.cms.beans.runtime;

import info.magnolia.cms.security.User;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.Aggregator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @author Sameer Charles
 * @version $Revision $ ($Author $)
 */
public class WebContextImpl implements Context {

    /**
     * Logger
     * */
    private static Logger log = Logger.getLogger(WebContextImpl.class);

    /**
     * user attached to this context
     * */
    private User user;

    /**
     * http request
     * */
    private HttpServletRequest request;

    /**
     * http session
     * */
    private HttpSession httpSession;

    /**
     * @param request
     * */
    public WebContextImpl(HttpServletRequest request) {
        this.request = request;
        this.httpSession = request.getSession();
    }

    /**
     * Set user instance for this context
     *
     * @param user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Get exiting logged in user instance
     *
     * @return User
     * @see info.magnolia.cms.security.User
     */
    public User getuser() {
        return this.user;
    }

    /**
     * Get hierarchy manager initialized for this user
     *
     * @param repositoryId
     * @return hierarchy manager
     */
    public HierarchyManager getHierarchyManager(String repositoryId) {
        return SessionAccessControl.getHierarchyManager(this.request, repositoryId);
    }

    /**
     * Get hierarchy manager initialized for this user
     *
     * @param repositoryId
     * @param workspaceId
     * @return hierarchy manager
     */
    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        return SessionAccessControl.getHierarchyManager(this.request, repositoryId, workspaceId);
    }

    /**
     * Get access manager for the specified repository on default workspace
     *
     * @param repositoryId
     * @return access manager
     */
    public AccessManager getAccessManager(String repositoryId) {
        return SessionAccessControl.getAccessManager(this.request, repositoryId);
    }

    /**
     * Get access manager for the specified repository on the specified workspace
     *
     * @param repositoryId
     * @param workspaceId
     * @return access manager
     */
    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return SessionAccessControl.getAccessManager(this.request, repositoryId, workspaceId);
    }

    /**
     * Get QueryManager created for this user on the specified repository
     *
     * @param repositoryId
     * @return query manager
     */
    public QueryManager getQueryManager(String repositoryId) {
        try {
            return SessionAccessControl.getQueryManager(this.request, repositoryId);
        } catch (RepositoryException re) {
            log.error(re.getMessage());
            log.debug(re);
            return null;
        }
    }

    /**
     * Get QueryManager created for this user on the specified repository and workspace
     *
     * @param repositoryId
     * @param workspaceId
     * @return query manager
     */
    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
        try {
            return SessionAccessControl.getQueryManager(this.request, repositoryId, workspaceId);
        } catch (RepositoryException re) {
            log.error(re.getMessage());
            log.debug(re);
            return null;
        }
    }

    /**
     * Get currently active page
     *
     * @return content object
     */
    public Content getActivePage() {
        return (Content) this.request.getAttribute(Aggregator.ACTPAGE);
    }

    /**
     * Get aggregated file, its used from image templates to manipulate
     *
     * @return file object
     */
    public File getFile() {
        return (File) this.request.getAttribute(Aggregator.FILE);
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
     * Get parameter value as string
     *
     * @param name
     * @return parameter value
     */
    public String getParameter(String name) {
        return this.request.getParameter(name);
    }

    /**
     * Get parameter value as string
     *
     * @return parameter values
     */
    public Map getParameters() {
        return this.request.getParameterMap();
    }

    /**
     * Set attribute value, scope of the attribute is defined
     *
     * @param name  is used as a key
     * @param value
     */
    public void setAttribute(String name, Object value) {
        this.request.setAttribute(name, value);
    }

    /**
     * Set attribute value, scope of the attribute is defined
     *
     * @param name  is used as a key
     * @param value
     * @param scope , highest level of scope from which this attribute is visible
     */
    public void setAttribute(String name, Object value, int scope) {
        switch (scope) {
            case REQUEST_SCOPE:
                this.request.setAttribute(name, value);
                break;
            case SESSION_SCOPE:
                this.httpSession.setAttribute(name, value);
                break;
            case APPLICATION_SCOPE:
                try {
                    String stringValue = (String) value;
                    SystemProperty.setProperty(name, stringValue);
                } catch (ClassCastException e) {
                    log.error("setAttribute only supports string values in application scope");
                }
                break;
            default:
                this.request.setAttribute(name, value);
                if (log.isDebugEnabled()) {
                    log.debug("Undefined scope, setting attribute [ "+name+" ] in request scope");
                }
        }
    }

    /**
     * Get attribute value
     *
     * @param name to which value is associated to
     * @return attribute value
     */
    public Object getAttribute(String name) {
        Object value = this.request.getAttribute(name);
        if (null == value) {
            value = this.httpSession.getAttribute(name);
        }
        return value;
    }

}
