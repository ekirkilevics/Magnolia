/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.AccessManagerImpl;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionUtil;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.HierarchyManagerUtil;
import info.magnolia.jcr.RuntimeRepositoryException;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of the Context interface.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractContext implements Context, Serializable {
    private static final Logger log = LoggerFactory.getLogger(AbstractContext.class);

    @Override
    public User getUser() {
        return Security.getSystemUser();
    }

    @Override
    public Subject getSubject() {
        return Security.getSystemSubject();
    }

    /**
     * The locale for this context.
     */
    protected Locale locale;

    private AttributeStrategy attributeStrategy;

    private JCRSessionStrategy repositoryStrategy;

    public AttributeStrategy getAttributeStrategy() {
        return attributeStrategy;
    }

    public void setAttributeStrategy(AttributeStrategy strategy) {
        this.attributeStrategy = strategy;
    }

    public JCRSessionStrategy getRepositoryStrategy() {
        return repositoryStrategy;
    }

    public void setRepositoryStrategy(JCRSessionStrategy strategy) {
        this.repositoryStrategy = strategy;
    }

    @Override
    public Object getAttribute(String name, int scope) {
        return getAttributeStrategy().getAttribute(name, scope);
    }

    @Override
    public Map<String, Object> getAttributes(int scope) {
        return getAttributeStrategy().getAttributes(scope);
    }

    @Override
    public void removeAttribute(String name, int scope) {
        getAttributeStrategy().removeAttribute(name, scope);
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        getAttributeStrategy().setAttribute(name, value, scope);
    }

    @Override
    public AccessManager getAccessManager(String name) {
        Subject subject = getSubject();
        List<Permission> availablePermissions = PermissionUtil.getPermissions(subject, name);
        if (availablePermissions == null) {
            log.warn("no permissions found for " + getUser().getName());
        }
        // TODO: use provider instead of fixed impl
        AccessManagerImpl ami = new AccessManagerImpl();
        ami.setPermissionList(availablePermissions);
        return ami;
    }

    @Override
    public Session getJCRSession(String workspaceName) throws LoginException, RepositoryException {
        return getRepositoryStrategy().getSession(workspaceName);
    }

    /**
     * Get the attribute value.
     * @param name to which value is associated to
     * @return attribute value
     */
    @Override
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
     * Merge the scopes maps.
     */
    @Override
    public Map<String, Object> getAttributes() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.putAll(this.getAttributes(Context.LOCAL_SCOPE));
        map.putAll(this.getAttributes(Context.SESSION_SCOPE));
        map.putAll(this.getAttributes(Context.APPLICATION_SCOPE));
        return map;
    }

    /**
     * If not yet set try to get the locale of the user. Else use the locale of the system context.
     * @see Context#getLocale()
     */
    @Override
    public Locale getLocale() {
        if (locale == null) {
            final SystemContext sysctx = MgnlContext.getSystemContext();
            if (this == sysctx) {
                // do not fall in the endless loop
                return null;
            }
            locale = sysctx.getLocale();
        }
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * TODO: This duplicates methods from MessagesManager : remove either.
     */
    @Override
    public Messages getMessages() {
        return getMessages(MessagesManager.DEFAULT_BASENAME);
    }

    /**
     * TODO: This duplicates methods from MessagesManager : remove either.
     */
    @Override
    public Messages getMessages(String basename) {
        return MessagesManager.getMessages(basename, getLocale());
    }

    @Override
    public HierarchyManager getHierarchyManager(String workspaceName) {
        try {
            return HierarchyManagerUtil.asHierarchyManager(getJCRSession(workspaceName));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public QueryManager getQueryManager(String workspaceName) {
        return this.getHierarchyManager(workspaceName).getQueryManager();
    }

    // ------ Map interface methods -------
    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(Object key) {
        return this.getAttribute(key.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(Object key, Object value) {
        this.setAttribute(key.toString(), value, Context.LOCAL_SCOPE);
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        for (String key : this.getAttributes().keySet()) {
            this.removeAttribute(key, Context.LOCAL_SCOPE);
        }
        throw new UnsupportedOperationException("you can not clear a magnolia context");
    }

    /**
     * This implementation is very slow!
     */
    @Override
    public boolean containsValue(Object value) {
        return this.getAttributes().containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.getAttributes().entrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.getAttributes().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> keySet() {
        return this.getAttributes().keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map map) {
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry) iter.next();
            this.setAttribute(entry.getKey().toString(), entry.getValue(), Context.LOCAL_SCOPE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object remove(Object key) {
        Object obj = this.getAttribute(key.toString());
        this.removeAttribute(key.toString(), Context.LOCAL_SCOPE);
        return obj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Object> values() {
        return this.getAttributes().values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object arg0) {
        return this.getAttributes().containsKey(arg0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.getAttributes().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        getRepositoryStrategy().release();
    }
}
