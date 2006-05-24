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

import info.magnolia.cms.Aggregator;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Revision $ ($Author $)
 */
public class WebContextImpl extends AbstractContext implements WebContext {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(WebContextImpl.class);

    /**
     * http request
     */
    private HttpServletRequest request;

    /**
     * Use init to initialize the object
     */
    public WebContextImpl() {
    }

    /**
     * @see info.magnolia.context.WebContext#init(javax.servlet.http.HttpServletRequest)
     */
    public void init(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Create the subject on demand
     * @see info.magnolia.context.AbstractContext#getUser()
     */
    public User getUser() {
        if (this.user == null) {
            if (Authenticator.getSubject(request) == null) {
                log.debug("JAAS Subject is null, returning Anonymous user");
                this.user = Security.getUserManager().getUser(UserManager.ANONYMOUS_USER);
            } else {
                this.user = Security.getUserManager().getUser(Authenticator.getSubject(request));
            }
        }
        return this.user;
    }

    /**
     * Make the language available for JSTL
     */
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        this.setAttribute(Config.FMT_LOCALE + ".session", locale.getLanguage(), MgnlContext.SESSION_SCOPE); //$NON-NLS-1$
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
     * @param scope , highest level of scope from which this attribute is visible
     */
    public void setAttribute(String name, Object value, int scope) {
        switch (scope) {
            case MgnlContext.REQUEST_SCOPE:
                this.request.setAttribute(name, value);
                break;
            case MgnlContext.SESSION_SCOPE:

                HttpSession httpsession = request.getSession(false);
                if (httpsession == null) {
                    log
                        .warn(
                            "Session initialized in oder to setting attribute '{}' to '{}'. You should avoid using session when possible!",
                            name,
                            value);
                    httpsession = request.getSession(true);
                }

                httpsession.setAttribute(name, value);
                break;
            case MgnlContext.APPLICATION_SCOPE:
                MgnlContext.getSystemContext().setAttribute(name, value, MgnlContext.APPLICATION_SCOPE);
                break;
            default:
                this.request.setAttribute(name, value);
                if (log.isDebugEnabled()) {
                    log.debug("Undefined scope, setting attribute [{}] in request scope", name);
                }
        }
    }

    /**
     * Get attribute value
     * @param name to which value is associated to
     * @return attribute value
     */
    public Object getAttribute(String name, int scope) {
        switch (scope) {
            case MgnlContext.REQUEST_SCOPE:
                return this.request.getAttribute(name);
            case MgnlContext.SESSION_SCOPE:
                HttpSession httpsession = request.getSession(false);
                if (httpsession == null) {
                    return null;
                }
                return httpsession.getAttribute(name);
            case MgnlContext.APPLICATION_SCOPE:
                return MgnlContext.getSystemContext().getAttribute(name, MgnlContext.APPLICATION_SCOPE);
            default:
                log.error("no illegal scope passed");
                return null;
        }
    }
    

    /**
     * Remove an attribute.
     */
    public void removeAttribute(String name, int scope) {
        switch (scope) {
            case MgnlContext.REQUEST_SCOPE:
                this.request.removeAttribute(name);
                break;
            case MgnlContext.SESSION_SCOPE:
                HttpSession httpsession = request.getSession(false);
                if (httpsession != null) {
                    httpsession.removeAttribute(name);
                }
                break;
            case MgnlContext.APPLICATION_SCOPE:
                MgnlContext.getSystemContext().removeAttribute(name, MgnlContext.APPLICATION_SCOPE);
                break;
            default:
                log.error("no illegal scope passed");
        }
    }
    
    /**
     * Get a map represenation of the scope
     */
    public final Map getAttributes(int scope){
        Map map = new HashMap();
        Enumeration keysEnum;
        switch (scope) {
            case MgnlContext.REQUEST_SCOPE:
                keysEnum = this.request.getAttributeNames();
                while(keysEnum.hasMoreElements()){
                    String key = (String) keysEnum.nextElement();
                    Object value = getAttribute(key, scope);
                    map.put(key, value);
                }
                return map;
            case MgnlContext.SESSION_SCOPE:
                HttpSession httpsession = request.getSession(false);
                if (httpsession == null) {
                    return null;
                }
                keysEnum = httpsession.getAttributeNames();
                while(keysEnum.hasMoreElements()){
                    String key = (String) keysEnum.nextElement();
                    Object value = getAttribute(key, scope);
                    map.put(key, value);
                }
                return map;
            case MgnlContext.APPLICATION_SCOPE:
                return MgnlContext.getSystemContext().getAttributes(MgnlContext.APPLICATION_SCOPE);
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

    public String getContextPath() {
        return this.request.getContextPath();
    }
}
