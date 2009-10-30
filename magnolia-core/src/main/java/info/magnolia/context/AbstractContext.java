/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.context;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;

import java.io.Serializable;
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
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractContext implements Context, Serializable {
    private static final Logger log = LoggerFactory.getLogger(AbstractContext.class);

    public User getUser() {
        return Security.getSystemUser();
    }

    /**
     * The locale for this context
     */
    protected Locale locale;

    private AttributeStrategy attributeStrategy;

    private RepositoryAcquiringStrategy repositoryStrategy;

    public AttributeStrategy getAttributeStrategy() {
        return attributeStrategy;
    }

    public void setAttributeStrategy(AttributeStrategy strategy) {
        this.attributeStrategy = strategy;
    }

    public RepositoryAcquiringStrategy getRepositoryStrategy() {
        return repositoryStrategy;
    }

    public void setRepositoryStrategy(RepositoryAcquiringStrategy strategy) {
        this.repositoryStrategy = strategy;
    }

    public Object getAttribute(String name, int scope) {
        return getAttributeStrategy().getAttribute(name, scope);
    }

    public Map<String, Object> getAttributes(int scope) {
        return getAttributeStrategy().getAttributes(scope);
    }

    public void removeAttribute(String name, int scope) {
        getAttributeStrategy().removeAttribute(name, scope);
    }

    public void setAttribute(String name, Object value, int scope) {
        getAttributeStrategy().setAttribute(name, value, scope);
    }

    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return getRepositoryStrategy().getAccessManager(repositoryId, workspaceId);
    }

    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        return getRepositoryStrategy().getHierarchyManager(repositoryId, workspaceId);
    }

    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return getRepositoryStrategy().getQueryManager(repositoryId, workspaceId);
    }

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
    public Map<String, Object> getAttributes() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.putAll(this.getAttributes(Context.LOCAL_SCOPE));
        map.putAll(this.getAttributes(Context.SESSION_SCOPE));
        map.putAll(this.getAttributes(Context.APPLICATION_SCOPE));
        return map;
    }

    /**
     * If not yet set try to get the locale of the user. Else use the locale of the system context
     * @see Context#getLocale()
     */
    public Locale getLocale() {
        if (locale == null) {            
            locale = MgnlContext.getSystemContext().getLocale();
        }
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * TODO: This duplicates methods from MessagesManager : remove either
     */
    public Messages getMessages() {
        return getMessages(MessagesManager.DEFAULT_BASENAME);
    }

    /**
     * TODO: This duplicates methods from MessagesManager : remove either
     */
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
    public Set<Entry<String, Object>> entrySet() {
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
    public Set<String> keySet() {
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
    public Collection<Object> values() {
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

    /**
     * {@inheritDoc}
     */
    public void release() {
        getRepositoryStrategy().release();
    }
}
