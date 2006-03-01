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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.FactoryUtil;

import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    public static Logger log = LoggerFactory.getLogger(MgnlContext.class);

    private static SystemContext systemContext = (SystemContext) FactoryUtil.getInstance(SystemContext.class);
    
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
        return getInstance().getUser();
    }

    /**
     * Set the locale for the current context.
     * @param locale
     */
    public static void setLocale(Locale locale) {
        getInstance().setLocale(locale);
     }

    /**
     * Get the contexts locale object
     * @return the current locale
     */
    public static Locale getLocale() {
        return getInstance().getLocale();
    }
    
    public static Messages getMessages(){
        return getInstance().getMessages();
    }

    public static Messages getMessages(String basename){
        return getInstance().getMessages(basename);
    }
    
    /**
     * Set current user
     * @param user
     */
    public static void setUser(User user) {
        getInstance().setUser(user);
    }

    /**
     * Get hierarchy manager initialized for this user
     * @param repositoryId
     * @return hierarchy manager
     */
    public static HierarchyManager getHierarchyManager(String repositoryId) {
        return getInstance().getHierarchyManager(repositoryId);
    }

    /**
     * Get hierarchy manager initialized for this user
     * @param repositoryId
     * @param workspaceId
     * @return hierarchy manager
     */
    public static HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        return getInstance().getHierarchyManager(repositoryId, workspaceId);
    }

    /**
     * Get access manager for the specified repository on default workspace
     * @param repositoryId
     * @return access manager
     */
    public static AccessManager getAccessManager(String repositoryId) {
        return getInstance().getAccessManager(repositoryId);
    }

    /**
     * Get access manager for the specified repository on the specified workspace
     * @param repositoryId
     * @param workspaceId
     * @return access manager
     */
    public static AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return getInstance().getAccessManager(repositoryId, workspaceId);
    }

    /**
     * Get QueryManager created for this user on the specified repository
     * @param repositoryId
     * @return query manager
     */
    public static QueryManager getQueryManager(String repositoryId) {
        return getInstance().getQueryManager(repositoryId);
    }

    /**
     * Get QueryManager created for this user on the specified repository and workspace
     * @param repositoryId
     * @param workspaceId
     * @return query manager
     */
    public static QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return getInstance().getQueryManager(repositoryId, workspaceId);
    }

    /**
     * Get currently active page
     * @return content object
     */
    public static Content getActivePage() {
        Context ctx = getInstance();
        if(ctx instanceof WebContext){
            return ((WebContext)ctx).getActivePage();
        }
        return null;
    }

    /**
     * Get aggregated file, its used from image templates to manipulate
     * @return file object
     */
    public static File getFile() {
        Context ctx = getInstance();
        if(ctx instanceof WebContext){
            return ((WebContext)ctx).getFile();
        }
        return null;
    }

    /**
     * Get form object assembled by <code>MultipartRequestFilter</code>
     * @return multipart form object
     */
    public static MultipartForm getPostedForm() {
        Context ctx = getInstance();
        if(ctx instanceof WebContext){
            return ((WebContext)ctx).getPostedForm();
        }
        return null;
    }

    /**
     * Get parameter value as string
     * @param name
     * @return parameter value
     */
    public static String getParameter(String name) {
        Context ctx = getInstance();
        if(ctx instanceof WebContext){
            return ((WebContext)ctx).getParameter(name);
        }
        return null;

    }

    /**
     * Get parameter value as string
     * @return parameter values
     */
    public static Map getParameters() {
        Context ctx = getInstance();
        if(ctx instanceof WebContext){
            return ((WebContext)ctx).getParameters();
        }
        return null;
    }
 
    /**
     * @return the context path
     */
    public static String getContextPath() {
        Context ctx = getInstance();
        if(ctx instanceof WebContext){
            return ((WebContext)ctx).getContextPath();
        }
        return "";
    }

    /**
     * Set attribute value, scope of the attribute is defined
     * @param name is used as a key
     * @param value
     */
    public static void setAttribute(String name, Object value) {
        getInstance().setAttribute(name, value);
    }

    /**
     * Set attribute value, scope of the attribute is defined
     * @param name is used as a key
     * @param value
     * @param scope , highest level of scope from which this attribute is visible
     */
    public static void setAttribute(String name, Object value, int scope) {
        getInstance().setAttribute(name, value, scope);
    }

    /**
     * Get attribute value
     * @param name to which value is associated to
     * @return attribute value
     */
    public static Object getAttribute(String name) {
        return getInstance().getAttribute(name);
    }

    /**
     * Set context implementation instance
     * @param context
     */
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

    /**
     * Get magnolia system context, Note : this context have full rights over all repositories/ workspaces
     * @return system context
     * */
    public static Context getSystemContext() {
        return systemContext;
    }
}