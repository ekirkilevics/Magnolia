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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.Security;

import java.util.HashMap;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of the Context interface
 *
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class ContextImpl extends HashMap implements Context {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(ContextImpl.class);

    /**
     * user attached to this context
     */
    protected User user;

    /**
     * The locale for this context
     */
    private Locale locale;
    
    /**
     * FIXME: we need a better solution
     */
    public Object get(Object key) {
        Object obj = super.get(key);
        if(obj != null){
            return obj;
        }
        return this.getAttribute(key.toString(), MgnlContext.REQUEST_SCOPE);
    }

    /**
     * FIXME: we need a better solution
     */
    public Object put(Object key, Object value) {
        this.setAttribute(key.toString(), value, MgnlContext.REQUEST_SCOPE);
        return super.put(key, value);
    }

    

    /**
     * Set user instance for this context
     *
     * @param user
     */
    public void setUser(User user) {
        this.user = user;
        setLocale(new Locale(user.getLanguage()));
    }

    /**
     * Get user as initialized
     *
     * @return User
     * @see info.magnolia.cms.security.User
     */
    public User getUser() {
        if (this.user == null) {
            log.debug("JAAS Subject is null, returning Anonymous user");
            this.user = Security.getUserManager().getAnonymousUser();
        }
        return this.user;
    }

    /**
     * If not yet set try to get the locale of the user. Else use the locale of the system context
     *
     * @see Context#getLocale()
     */
    public Locale getLocale() {
        if (locale == null) {
            User user = this.getUser();
            if (user != null) {
                locale = new Locale(user.getLanguage());
            }
            if (locale == null) {
                locale = MgnlContext.getSystemContext().getLocale();
            }
        }

        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Messages getMessages() {
        return getMessages(MessagesManager.DEFAULT_BASENAME);
    }

    public Messages getMessages(String basename) {
        return MessagesManager.getMessages(basename, getLocale());
    }

    public HierarchyManager getHierarchyManager(String repositoryId) {
        return this.getHierarchyManager(repositoryId, ContentRepository.getDefaultWorkspace(repositoryId));
    }

    public AccessManager getAccessManager(String repositoryId) {
        return this.getAccessManager(repositoryId, ContentRepository.getDefaultWorkspace(repositoryId));
    }

    public QueryManager getQueryManager(String repositoryId) {
        return this.getQueryManager(repositoryId, ContentRepository.getDefaultWorkspace(repositoryId));
    }

}
