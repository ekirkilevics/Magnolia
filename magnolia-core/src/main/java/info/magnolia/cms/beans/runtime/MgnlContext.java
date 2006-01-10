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

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.AccessManager;

import org.apache.log4j.Logger;

import java.util.Map;


/**
 * This class enables to get the current Request without passing the request around the world. A ThreadLocal is used to
 * manage this.
 * <p>
 * In a later version this class should not depend on servlets. The core should use the context to get and set
 * attributes instead of using the request or session object directly. Magnolia could run then in a neutral and
 * configurable context.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */

public class MgnlContext {

    /**
     * Logger
     */
    public static Logger log = Logger.getLogger(MgnlContext.class);

    /**
     * The thread local variable holding the current context
     */
    private static ThreadLocal localContext = new ThreadLocal();

    /**
     * Do not instantiate this class. The constructor must be public to use discovery
     */
    public MgnlContext() {
    }

    /**
     * A short cut for the current user.
     * @return the current user
     */
    public static User getUser() {
        return getInstance().getuser();
    }

    /**
     * Set current user
     * @param user
     * */
    public static void setUser(User user) {
        getInstance().setUser(user);
    }

    /**
     * Get hierarchy manager initialized for this user
     * @param repositoryId
     * @return hierarchy manager
     * */
    public static HierarchyManager getHierarchyManager(String repositoryId) {
        return getInstance().getHierarchyManager(repositoryId);
    }

    /**
     * Get hierarchy manager initialized for this user
     * @param repositoryId
     * @param workspaceId
     * @return hierarchy manager
     * */
    public static HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        return getInstance().getHierarchyManager(repositoryId, workspaceId);
    }

    /**
     * Get access manager for the specified repository on default workspace
     * @param repositoryId
     * @return access manager
     * */
    public static AccessManager getAccessManager(String repositoryId) {
        return getInstance().getAccessManager(repositoryId);
    }

    /**
     * Get access manager for the specified repository on the specified workspace
     * @param repositoryId
     * @param workspaceId
     * @return access manager
     * */
    public static AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return getInstance().getAccessManager(repositoryId, workspaceId);
    }

    /**
     * Get QueryManager created for this user on the specified repository
     * @param repositoryId
     * @return query manager
     * */
    public static QueryManager getQueryManager(String repositoryId) {
        return getInstance().getQueryManager(repositoryId);
    }

    /**
     * Get QueryManager created for this user on the specified repository and workspace
     * @param repositoryId
     * @param workspaceId
     * @return query manager
     * */
    public static QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return getInstance().getQueryManager(repositoryId, workspaceId);
    }

    /**
     * Get currently active page
     * @return content object
     * */
    public static Content getActivePage() {
        return getInstance().getActivePage();
    }

    /**
     * Get aggregated file, its used from image templates to manipulate
     * @return file object
     * */
    public static File getFile() {
        return getInstance().getFile();
    }

    /**
     * Get form object assembled by <code>MultipartRequestFilter</code>
     * @return multipart form object
     * */
    public static MultipartForm getPostedForm() {
        return getInstance().getPostedForm();
    }

    /**
     * Get parameter value as string
     * @param name
     * @return parameter value
     * */
    public static String getParameter(String name) {
        return getInstance().getParameter(name);
    }

    /**
     * Get parameter value as string
     * @return parameter values
     * */
    public static Map getParameters() {
        return getInstance().getParameters();
    }

    /**
     * Set attribute value, scope of the attribute is defined
     * @param name is used as a key
     * @param value
     * */
    public static void setAttribute(String name, Object value) {
        getInstance().setAttribute(name, value);
    }

    /**
     * Set attribute value, scope of the attribute is defined
     * @param name is used as a key
     * @param value
     * @param scope , highest level of scope from which this attribute is visible
     * */
    public static void setAttribute(String name, Object value, int scope) {
        getInstance().setAttribute(name, value, scope);
    }

    /**
     * Get attribute value
     * @param name to which value is associated to
     * @return attribute value
     * */
    public static Object getAttribute(String name) {
        return getInstance().getAttribute(name);
    }


    /**
     * Set context implementation instance
     * @param context
     * */
    public static void setInstance(Context context) {
        localContext.set(context);
    }

    /**
     * Get the current context of this thread
     * @return the context
     */
    public static Context getInstance() {
        return (Context) localContext.get();
    }

}
