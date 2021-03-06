/**
 * This file Copyright (c) 2003-2013 Magnolia International
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
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;

import java.util.Locale;
import java.util.Map;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;


/**
 * This interface defines all the methods which should be implemented by any configured magnolia context. Implementing
 * class should never be accessible directly but only via MgnlContext static methods which work on a local (Thread) copy
 * of the implementation.
 * @author Sameer Charles
 * @version $Revision $ ($Author $)
 */
public interface Context extends info.magnolia.commands.chain.Context {

    /**
     * Attribute visibility scope.
     */
    public static final int LOCAL_SCOPE = 1;

    /**
     * Attribute visibility scope Shared by all requests from this session.
     */
    public static final int SESSION_SCOPE = 2;

    /**
     * Attribute visibility scope, its visible to all sessions of this application.
     */
    public static final int APPLICATION_SCOPE = 3;

    final static public String ATTRIBUTE_REPOSITORY = "repository";

    final static public String ATTRIBUTE_PATH = "path";

    final static public String ATTRIBUTE_VERSION = "version";

    final static public String ATTRIBUTE_VERSION_MAP = "versionMap";

    final static public String ATTRIBUTE_UUID = "uuid";

    final static public String ATTRIBUTE_RECURSIVE = "recursive";

    public static final String ATTRIBUTE_COMMENT = "comment";

    public static final String ATTRIBUTE_MESSAGE = "msg";

    public static final String ATTRIBUTE_EXCEPTION = "exception";

    /**
     * If this is not a UserContext this method will very likely return the system user.
     */
    public User getUser();

    public Subject getSubject();

    /**
     * @param locale
     */
    public void setLocale(Locale locale);

    /**
     * Get the current locale.
     */
    public Locale getLocale();

    public Session getJCRSession(String workspaceName) throws LoginException, RepositoryException ;

    /**
     * Get hierarchy manager initialized for this user.
     * @param workspaceName
     * @return hierarchy manager
     *
     * @deprecated since 4.5 - use {@link #getJCRSession(String)}
     */
    @Deprecated
    public HierarchyManager getHierarchyManager(String workspaceName);

    /**
     * Get a an {@link info.magnolia.cms.security.AccessManager} for logical entities. The accesses to the repository are handled by the repository itself.
     */
    public AccessManager getAccessManager(String name);

    /**
     * Get QueryManager created for this user on the specified repository.
     * @deprecated since 4.5 - use {@link #getJCRSession(String)} and acquire the JCR query manager directly from the session.
     */
    @Deprecated
    public QueryManager getQueryManager(String workspaceName);

    /**
     * Set attribute value, scope of the attribute is defined.
     * @param name is used as a key
     * @param scope , highest level of scope from which this attribute is visible
     */
    public void setAttribute(String name, Object value, int scope);

    /**
     * Get attribute value.
     * @param name to which value is associated to
     * @param scope the scope (request, session, application)
     * @return attribute value
     */
    public <T> T getAttribute(String name, int scope);

    /**
     * Get attribute value without passing a scope. the scopes are searched from bottom up (request, session,
     * application)
     * @param name to which value is associated to
     * @return attribute value
     */
    public <T> T getAttribute(String name);

    /**
     * Get a map of a attributes set in the scope.
     * @param scope
     * @return the map
     */
    public Map<String, Object> getAttributes(int scope);

    /**
     * Remove an attribute.
     * @param name
     * @param scope
     */
    public void removeAttribute(String name, int scope);

    /**
     * Get an over all map.
     * @return the map
     */
    public Map<String, Object> getAttributes();

    /**
     * Get the default messages. It uses the locale set on this context
     * TODO: This duplicates methods from MessagesManager : remove either
     */
    public Messages getMessages();

    /**
     * Get the messages of the named bundle. It uses the locale set on this context
     * @param basename name of the bundle
     * TODO: This duplicates methods from MessagesManager : remove either
     */
    public Messages getMessages(String basename);

    /**
     * Release any resource used by this Context (e.g. jcr sessions).
     */
    public void release();

}
