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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of the Context interface
 *
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractContext implements Context {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(AbstractContext.class);

    /**
     * user attached to this context
     */
    protected User user;

    /**
     * The locale for this context
     */
    private Locale locale;
    
    /**
     * Get attribute value
     * @param name to which value is associated to
     * @return attribute value
     */
    public Object getAttribute(String name) {
        Object value = this.getAttribute(name, Context.LOCAL_SCOPE);
        if (null == value) {
            value = this.getAttribute(name, Context.SESSION_SCOPE);
        }
        if (null == value) {
            value = this.getAttribute(name, Context.APPLICATION_SCOPE);
        }
        return value;
    }
    
    /**
     * Merge the scopes maps
     */
    public Map getAttributes(){
        Map map = new HashMap();
        map.putAll(this.getAttributes(Context.LOCAL_SCOPE));
        map.putAll(this.getAttributes(Context.SESSION_SCOPE));
        map.putAll(this.getAttributes(Context.APPLICATION_SCOPE));
        return map;
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
    
    /**
     * Map implemenation
     */
    public Object get(Object key) {
        return this.getAttribute(key.toString());
    }

    /**
     * Map implementation
     */
    public Object put(Object key, Object value) {
        this.setAttribute(key.toString(), value, Context.LOCAL_SCOPE);
        return value;
    }
    
    /**
     * Map implementation
     */
    public void clear() {
        for (Iterator iter = this.getAttributes().keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            this.removeAttribute(key, Context.LOCAL_SCOPE);
            
        }
        throw new UnsupportedOperationException("you can not clear a magnolia context");
    }

    /**
     * Map implementation. This implementation is very slow
     */
    public boolean containsValue(Object value) {
        return this.getAttributes().containsValue(value);
    }

    /**
     * Map implementation
     */
    public Set entrySet() {
        return this.getAttributes().entrySet();
    }

    /**
     * Map implementation
     */
    public boolean isEmpty() {
        return this.getAttributes().isEmpty();
    }

    /**
     * Map implementation
     */
    public Set keySet() {
        return this.getAttributes().keySet();
    }

    /**
     * Map implementation
     */
    public void putAll(Map map) {
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry) iter.next();
            this.setAttribute(entry.getKey().toString(), entry.getValue(), Context.LOCAL_SCOPE);
        }
    }

    /**
     * Map implementation
     */
    public Object remove(Object key) {
        Object obj = this.getAttribute(key.toString());
        this.removeAttribute(key.toString(), Context.LOCAL_SCOPE);
        return obj;
    }

    /**
     * Map implementation
     */
    public Collection values() {
        return this.getAttributes().values();
    }

    /**
     * Map implementation
     */
    public boolean containsKey(Object arg0) {
        return this.getAttributes().containsKey(arg0);
    }
    
    /**
     * Map implementation
     */
    public int size() {
        return this.getAttributes().size();
    }


}
