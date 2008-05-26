/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.context;

import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.core.HierarchyManager;

import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    public static Messages getMessages() {
        return getInstance().getMessages();
    }

    public static Messages getMessages(String basename) {
        return getInstance().getMessages(basename);
    }

    /**
     * Set current user
     * @param user
     * @deprecated use login insttead
     */
    public static void setUser(User user) {
        login(user);
    }

    public static void login(User user) {
        ((UserContext)getInstance()).login(user);
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
     * @deprecated use WebContext.getAggregationState()
     */
    public static Content getActivePage() {

        WebContext ctx = getWebContextIfExisting(getInstance());
        if (ctx != null) {
            return ctx.getActivePage();
        }
        return null;
    }

    /**
     * Get aggregated file, its used from image templates to manipulate
     * @return file object
     * @deprecated use WebContext.getAggregationState()
     */
    public static File getFile() {
        WebContext ctx = getWebContextIfExisting(getInstance());
        if (ctx != null) {
            return ctx.getFile();
        }
        return null;
    }

    /**
     * Get form object assembled by <code>MultipartRequestFilter</code>
     * @return multipart form object
     * @deprecated use WebContext.getAggregationState() TODO ?
     */
    public static MultipartForm getPostedForm() {
        WebContext ctx = getWebContextIfExisting(getInstance());
        if (ctx != null) {
            return ctx.getPostedForm();
        }
        return null;
    }

    /**
     * Get parameter value as string
     * @param name
     * @return parameter value
     */
    public static String getParameter(String name) {
        WebContext ctx = getWebContextIfExisting(getInstance());
        if (ctx != null) {
            return ctx.getParameter(name);
        }
        return null;

    }

    /**
     * Get parameter value as string
     * @return parameter values
     */
    public static Map getParameters() {
        WebContext ctx = getWebContextIfExisting(getInstance());
        if (ctx != null) {
            return ctx.getParameters();
        }
        return null;
    }

    /**
     * @return the context path
     */
    public static String getContextPath() {
        WebContext ctx = getWebContextIfExisting(getInstance());
        if (ctx != null) {
            return ctx.getContextPath();
        } else {
            throw new IllegalStateException("Can only get the context path within a WebContext.");
        }
    }

    /**
     * Returns the AggregationState if we're in a WebContext, throws an
     * IllegalStateException otherwise.
     */
    public static AggregationState getAggregationState() {
        final WebContext ctx = getWebContextIfExisting(getInstance());
        if (ctx != null) {
            return ctx.getAggregationState();
        } else {
            throw new IllegalStateException("Can only get the aggregation state within a WebContext.");
        }
    }

    /**
     * Resets the current aggregator instance if we're in a WebContext, throws an IllegalStateException otherwise.
     */
    public static void resetAggregationState() {
        final WebContext ctx = getWebContextIfExisting(getInstance());
        if (ctx != null) {
            ctx.resetAggregationState();
        }
        else {
            throw new IllegalStateException("Can only reset the aggregation state within a WebContext.");
        }
    }

    /**
     * Set attribute value, scope of the attribute is defined
     * @param name is used as a key
     * @param value
     */
    public static void setAttribute(String name, Object value) {
        getInstance().setAttribute(name, value, Context.LOCAL_SCOPE);
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
     * Get the attribute from the specified scope
     * @param name
     * @param scope
     * @return the value
     */
    public static Object getAttribute(String name, int scope) {
        return getInstance().getAttribute(name, scope);
    }

    /**
     * Check if this attribute exists in the local scope
     * @param name
     * @return
     */
    public static boolean hasAttribute(String name){
        return getInstance().getAttribute(name, Context.LOCAL_SCOPE) != null;
    }

    /**
     * Remove an attribute in the local scope
     * @param name
     * @return
     */
    public static void removeAttribute(String name){
        getInstance().removeAttribute(name, Context.LOCAL_SCOPE);
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
        Context context = (Context) localContext.get();
        // It should never fall back, We need to fix all false callers instead
        if (context == null) {
            IllegalStateException ise = new IllegalStateException("MgnlContext is not set for this thread");
            log.error("MgnlContext is not initialized, This could happen if the request does not go through magnolia "
                + "default filters.", ise);
            throw ise;
        }
        return context;
    }

    /**
     * Used to check if an instance is already set since getInstance() will always return a context.
     * @return true if an instance was set
     */
    public static boolean hasInstance() {
        return localContext.get() != null;
    }

    public static boolean isSystemInstance() {
        return (localContext.get() instanceof SystemContext);
    }

    /**
     * Get magnolia system context, Note : this context have full rights over all repositories/ workspaces
     * @return system context
     */
    public static Context getSystemContext() {
        return ContextFactory.getInstance().getSystemContext();
    }

    /**
     * Executes the given operation in the system context and sets it back to the original once done
     * (also if an exception is thrown). Also works if there was no context upon calling (sets it back
     * to null in this case)
     */
    public static void doInSystemContext(SystemContextOperation op) {
        doInSystemContext(op, false);
    }

    /**
     * Executes the given operation in the system context and sets it back to the original once done
     * (also if an exception is thrown). Also works if there was no context upon calling (sets it back
     * to null in this case)
     * @param releaseAfterExecution TODO
     */
    public static void doInSystemContext(SystemContextOperation op, boolean releaseAfterExecution) {
        final Context originalCtx = MgnlContext.hasInstance() ? MgnlContext.getInstance() : null;
        try {
            MgnlContext.setInstance(MgnlContext.getSystemContext());
            op.exec();
            if(releaseAfterExecution){
                MgnlContext.release();
            }
        } finally {
            MgnlContext.setInstance(originalCtx);
        }
    }

    public static interface SystemContextOperation {
        void exec();
    }

    /**
     * Sets this context as a web context.
     * @param request
     * @deprecated Use {@link #initAsWebContext(HttpServletRequest,HttpServletResponse,ServletContext)} instead
     */
    public static void initAsWebContext(HttpServletRequest request, HttpServletResponse response) {
        initAsWebContext(request, response, null);
    }

    /**
     * Sets this context as a web context.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param servletContext ServletContext instance
     */
    public static void initAsWebContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        WebContext ctx = ContextFactory.getInstance().createWebContext(request, response, servletContext);
        setInstance(ctx);
    }

    /**
     * Returns the web context, also if eventually wrapped in a ContextDecorator.
     * @param ctx
     * @return WebContext instance or null if no web context is set
     */
    private static WebContext getWebContextIfExisting(Context ctx) {
        if (ctx instanceof WebContext) {
            return (WebContext) ctx;
        }
        else if (ctx instanceof ContextDecorator) {
            return getWebContextIfExisting(((ContextDecorator) ctx).getWrappedContext());
        }
        return null;
    }

    /**
     * Releases the current thread (if not a system context) and calls the releaseThread() method of the system context
     *
     */
    public static void release() {
        if(hasInstance() && !(getInstance() instanceof SystemContext)){
            getInstance().release();
        }
        SystemContext systemContext = (SystemContext) getSystemContext();
        if(systemContext instanceof ThreadReleasingSystemContext){
            ((ThreadReleasingSystemContext)systemContext).releaseThread();
        }
    }
}
