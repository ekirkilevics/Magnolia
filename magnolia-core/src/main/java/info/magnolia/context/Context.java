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

import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;
import info.magnolia.cms.core.HierarchyManager;

import java.util.Locale;
import java.util.Map;


/**
 * This interface defines all the methods which should be implemented by any configured magnolia context, implementing
 * class should never be accessible directly but only via MgnlContext static methods which work on a local (Thread) copy
 * of the implementation
 * @author Sameer Charles
 * @version $Revision $ ($Author $)
 */
public interface Context extends org.apache.commons.chain.Context {

    /**
     * Attribute visibility scope
     */
    public static final int LOCAL_SCOPE = 1;

    /**
     * Attribute visibility scope Shared by all requests from this session
     */
    public static final int SESSION_SCOPE = 2;

    /**
     * Attribute visibility scope, its visible to all sessions of this application
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
     * Cast to UserContext
     * @deprecated
     */
    public User getUser();
    
    /**
     * @param locale
     */
    public void setLocale(Locale locale);

    /**
     * Get the current locale
     */
    public Locale getLocale();

    /**
     * Get hierarchy manager initialized for this user
     * @param repositoryId
     * @return hierarchy manager
     */
    public HierarchyManager getHierarchyManager(String repositoryId);

    /**
     * Get hierarchy manager initialized for this user
     * @param repositoryId
     * @param workspaceId
     * @return hierarchy manager
     */
    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId);

    /**
     * Get access manager for the specified repository on default workspace
     * @param repositoryId
     * @return access manager
     */
    public AccessManager getAccessManager(String repositoryId);

    /**
     * Get access manager for the specified repository on the specified workspace
     * @param repositoryId
     * @param workspaceId
     * @return access manager
     */
    public AccessManager getAccessManager(String repositoryId, String workspaceId);

    /**
     * Get QueryManager created for this user on the specified repository
     * @param repositoryId
     * @return query manager
     */
    public QueryManager getQueryManager(String repositoryId);

    /**
     * Get QueryManager created for this user on the specified repository and workspace
     * @param repositoryId
     * @param workspaceId
     * @return query manager
     */
    public QueryManager getQueryManager(String repositoryId, String workspaceId);

    /**
     * Set attribute value, scope of the attribute is defined
     * @param name is used as a key
     * @param value
     * @param scope , highest level of scope from which this attribute is visible
     */
    public void setAttribute(String name, Object value, int scope);

    /**
     * Get attribute value
     * @param name to which value is associated to
     * @param scope the scope (request, session, application)
     * @return attribute value
     */
    public Object getAttribute(String name, int scope);

    /**
     * Get attribute value without passing a scope. the scopes are searched from bottom up (request, session,
     * application)
     * @param name to which value is associated to
     * @return attribute value
     */
    public Object getAttribute(String name);

    /**
     * Get a map of a attributes set in the scope
     * @param scope
     * @return the map
     */
    public Map getAttributes(int scope);

    /**
     * Remove an attribute
     * @param name
     * @param scope
     */
    public void removeAttribute(String name, int scope);

    /**
     * Get an over all map
     * @return the map
     */
    public Map getAttributes();

    /**
     * Get the default messages. It uses the locale set on this context
     */
    public Messages getMessages();

    /**
     * Get the messages of the named bundle. It uses the locale set on this context
     * @param basename name of the bundle
     */
    public Messages getMessages(String basename);

    /**
     * Release any resource used by this Context (e.g. jcr sessions).
     */
    public void release();

}
