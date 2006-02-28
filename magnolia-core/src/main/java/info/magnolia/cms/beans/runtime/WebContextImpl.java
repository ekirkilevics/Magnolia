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
package info.magnolia.cms.beans.runtime;

import info.magnolia.cms.Aggregator;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Security;

import java.util.Locale;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Revision $ ($Author $)
 */
public class WebContextImpl extends ContextImpl implements WebContext{

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(WebContextImpl.class);

    /**
     * http request
     */
    private HttpServletRequest request;

    /**
     * @param request
     */
    public WebContextImpl(HttpServletRequest request) {
        this.request = request;
        this.setUser(Security.getUserManager().getUserObject(Authenticator.getSubject(request)));
    }

    /**
     * Make the language available for JSTL 
     */
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        this.setAttribute(Config.FMT_LOCALE + ".session", locale.getLanguage(), Context.SESSION_SCOPE); //$NON-NLS-1$
    }
    
    /**
     * Get hierarchy manager initialized for this user
     * @param repositoryId
     * @param workspaceId
     * @return hierarchy manager
     */
    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        return SessionStore.getHierarchyManager(this.request, repositoryId, workspaceId);
    }

    /**
     * Get access manager for the specified repository on the specified workspace
     * @param repositoryId
     * @param workspaceId
     * @return access manager
     */
    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return SessionStore.getAccessManager(this.request, repositoryId, workspaceId);
    }

    /**
     * Get QueryManager created for this user on the specified repository and workspace
     * @param repositoryId
     * @param workspaceId
     * @return query manager
     */
    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
        try {
            return SessionStore.getQueryManager(this.request, repositoryId, workspaceId);
        }
        catch (RepositoryException re) {
            log.error(re.getMessage());
            log.debug("Exception caught", re);
            return null;
        }
    }

    /**
     * Get currently active page
     * @return content object
     */
    public Content getActivePage() {
        return (Content) this.request.getAttribute(Aggregator.ACTPAGE);
    }

    /**
     * Get aggregated file, its used from image templates to manipulate
     * @return file object
     */
    public File getFile() {
        return (File) this.request.getAttribute(Aggregator.FILE);
    }

    /**
     * Get form object assembled by <code>MultipartRequestFilter</code>
     * @return multipart form object
     */
    public MultipartForm getPostedForm() {
        return (MultipartForm) this.request.getAttribute("multipartform"); //$NON-NLS-1$
    }

    /**
     * Get parameter value as string
     * @param name
     * @return parameter value
     */
    public String getParameter(String name) {
        return this.request.getParameter(name);
    }

    /**
     * Get parameter value as string
     * @return parameter values
     */
    public Map getParameters() {
        return this.request.getParameterMap();
    }

    /**
     * Set attribute value, scope of the attribute is defined
     * @param name is used as a key
     * @param value
     */
    public void setAttribute(String name, Object value) {
        this.request.setAttribute(name, value);
    }

    /**
     * Set attribute value, scope of the attribute is defined
     * @param name is used as a key
     * @param value
     * @param scope , highest level of scope from which this attribute is visible
     */
    public void setAttribute(String name, Object value, int scope) {
        switch (scope) {
            case REQUEST_SCOPE:
                this.request.setAttribute(name, value);
                break;
            case SESSION_SCOPE:
                this.request.getSession().setAttribute(name, value);
                break;
            case APPLICATION_SCOPE:
                try {
                    String stringValue = (String) value;
                    SystemProperty.setProperty(name, stringValue);
                }
                catch (ClassCastException e) {
                    log.error("setAttribute only supports string values in application scope");
                }
                break;
            default:
                this.request.setAttribute(name, value);
                if (log.isDebugEnabled()) {
                    log.debug("Undefined scope, setting attribute [ " + name + " ] in request scope");
                }
        }
    }

    /**
     * Get attribute value
     * @param name to which value is associated to
     * @return attribute value
     */
    public Object getAttribute(String name) {
        Object value = this.request.getAttribute(name);
        if (null == value) {
            value = this.request.getSession().getAttribute(name);
        }
        return value;
    }

    
    /**
     * Avoid the call to this method where ever possible.
     * @return Returns the request.
     */
    public HttpServletRequest getRequest() {
        return this.request;
    }

    public String getContextPath() {
        return this.request.getContextPath();
    }

}
