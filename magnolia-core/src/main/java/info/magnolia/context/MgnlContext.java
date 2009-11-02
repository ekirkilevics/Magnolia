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

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
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
    private static final Logger log = LoggerFactory.getLogger(MgnlContext.class);

    /**
     * The thread local variable holding the current context
     */
    private static ThreadLocal<Context> localContext = new ThreadLocal<Context>();

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

    public static void login(User user) {
        ((UserContext)getInstance()).login(user);
    }

    /**
     * Get hierarchy manager initialized for this user.
     */
    public static HierarchyManager getHierarchyManager(String repositoryId) {
        return getInstance().getHierarchyManager(repositoryId);
    }

    /**
     * Get hierarchy manager initialized for this user.
     */
    public static HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        return getInstance().getHierarchyManager(repositoryId, workspaceId);
    }

    /**
     * Get access manager for the specified repository on default workspace.
     */
    public static AccessManager getAccessManager(String repositoryId) {
        return getInstance().getAccessManager(repositoryId);
    }

    /**
     * Get access manager for the specified repository on the specified workspace.
     */
    public static AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return getInstance().getAccessManager(repositoryId, workspaceId);
    }

    /**
     * Get QueryManager created for this user on the specified repository.
     */
    public static QueryManager getQueryManager(String repositoryId) {
        return getInstance().getQueryManager(repositoryId);
    }

    /**
     * Get QueryManager created for this user on the specified repository and workspace.
     */
    public static QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return getInstance().getQueryManager(repositoryId, workspaceId);
    }

    /**
     * Get form object assembled by <code>MultipartRequestFilter</code>
     * @return multipart form object
     * TODO - move to getAggregationState() ?
     */
    public static MultipartForm getPostedForm() {
        WebContext ctx = getWebContextOrNull();
        if (ctx != null) {
            return ctx.getPostedForm();
        }
        return null;
    }

    /**
     * Get parameter value as string.
     */
    public static String getParameter(String name) {
        WebContext ctx = getWebContextOrNull();
        if (ctx != null) {
            return ctx.getParameter(name);
        }
        return null;

    }

    public static String[] getParameterValues(String name) {
        WebContext ctx = getWebContextOrNull();
        if (ctx != null) {
            return ctx.getParameterValues(name);
        }
        return null;

    }

    /**
     * Get parameter value as a Map&lt;String, String&gt;.
     */
    public static Map<String, String> getParameters() {
        WebContext ctx = getWebContextOrNull();
        if (ctx != null) {
            return ctx.getParameters();
        }
        return null;
    }

    /**
     * @return the context path.
     */
    public static String getContextPath() {
        WebContext ctx = getWebContextOrNull();
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
        final WebContext ctx = getWebContextOrNull();
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
        final WebContext ctx = getWebContextOrNull();
        if (ctx != null) {
            ctx.resetAggregationState();
        }
        else {
            throw new IllegalStateException("Can only reset the aggregation state within a WebContext.");
        }
    }

    /**
     * Set attribute value, scope of the attribute is defined.
     */
    public static void setAttribute(String name, Object value) {
        getInstance().setAttribute(name, value, Context.LOCAL_SCOPE);
    }

    /**
     * Set attribute value, scope of the attribute is defined.
     * @param scope , highest level of scope from which this attribute is visible.
     */
    public static void setAttribute(String name, Object value, int scope) {
        getInstance().setAttribute(name, value, scope);
    }

    /**
     * Get attribute value.
     */
    public static Object getAttribute(String name) {
        return getInstance().getAttribute(name);
    }

    /**
     * Get the attribute from the specified scope.
     */
    public static Object getAttribute(String name, int scope) {
        return getInstance().getAttribute(name, scope);
    }

    /**
     * Check if this attribute exists in the local scope.
     */
    public static boolean hasAttribute(String name){
        return getInstance().getAttribute(name, Context.LOCAL_SCOPE) != null;
    }

    /**
     * Remove an attribute in the local scope.
     */
    public static void removeAttribute(String name){
        getInstance().removeAttribute(name, Context.LOCAL_SCOPE);
    }

    /**
     * Set context implementation instance.
     */
    public static void setInstance(Context context) {
        localContext.set(context);
    }

    /**
     * Get the current context of this thread.
     */
    public static Context getInstance() {
        Context context = localContext.get();
        // It should never fall back, We need to fix all false callers instead
        if (context == null) {
            IllegalStateException ise = new IllegalStateException("MgnlContext is not set for this thread");
            log.error("MgnlContext is not initialized. This could happen if the request does not go through the Magnolia default filters.", ise);
            throw ise;
        }
        return context;
    }

    /**
     * Throws an IllegalStateException if the current context is not set, or if it is not an instance of WebContext.
     * @see #getWebContext(String)
     */
    public static WebContext getWebContext() {
        return getWebContext(null);
    }

    /**
     * Throws an IllegalStateException if the current context is not set, or if it is not an instance of WebContext.
     * Yes, you can specify the exception message if you want. This is useful if you're calling this from a component
     * which only supports WebContext and still care enough to actually throw an exception with a meaningful message.
     * @see #getWebContext()
     */
    public static WebContext getWebContext(String exceptionMessage) {
        final WebContext wc = getWebContextIfExisting(getInstance());
        if (wc == null) {
            throw new IllegalStateException(exceptionMessage == null ? "The current context is not an instance of WebContext (" + localContext.get() + ")" : exceptionMessage);
        }
        return wc;
    }

    /**
     * Returns the current context if it is set and is an instance of WebContext, returns null otherwise.
     * @return
     */
    public static WebContext getWebContextOrNull() {
        return hasInstance() ? getWebContextIfExisting(getInstance()) : null;
    }

    /**
     * Used to check if an instance is already set since getInstance() will always return a context.
     * @return true if an instance was set.
     */
    public static boolean hasInstance() {
        return localContext.get() != null;
    }

    public static boolean isSystemInstance() {
        return localContext.get() instanceof SystemContext;
    }

    /**
     * Returns true if the current context is set and is an instance of WebContext. (it might be wrapped in a ContextDecorator)
     */
    public static boolean isWebContext() {
        return hasInstance() && getWebContextIfExisting(getInstance()) != null;
    }

    /**
     * Get Magnolia system context. This context has full permissions over all repositories/ workspaces.
     */
    public static SystemContext getSystemContext() {
        return ContextFactory.getInstance().getSystemContext();
    }

    /**
     * @deprecated since 4.2 - use the Op interface, which can return values, or extend VoidOp.
     */
    public static Void doInSystemContext(final SystemContextOperation op) {
        return doInSystemContext(op, false);
    }

    /**
     * Executes the given operation in the system context and sets it back to the original once done
     * (also if an exception is thrown). Also works if there was no context upon calling. (sets it back
     * to null in this case)
     */
    public static <T, E extends Throwable> T doInSystemContext(final Op<T, E> op) throws E {
        return doInSystemContext(op, false);
    }

    /**
     * @deprecated since 4.2 - use the Op interface, which can return values, or extend VoidOp.
     */
    public static Void doInSystemContext(final SystemContextOperation op, boolean releaseAfterExecution) {
        return doInSystemContext(new VoidOp() {
            public void doExec() {
                op.exec();
            }
        }, releaseAfterExecution);
    }

    /**
     * Executes the given operation in the system context and sets it back to the original once done
     * (also if an exception is thrown). Also works if there was no context upon calling (sets it back
     * to null in this case)
     * @param releaseAfterExecution TODO document this parameter
     */
    public static <T, E extends Throwable> T doInSystemContext(final Op<T, E> op, boolean releaseAfterExecution) throws E {
        final Context originalCtx = MgnlContext.hasInstance() ? MgnlContext.getInstance() : null;
        T result;
        try {
            MgnlContext.setInstance(MgnlContext.getSystemContext());
            result = op.exec();
            if (releaseAfterExecution) {
                MgnlContext.release();
            }
        } finally {
            MgnlContext.setInstance(originalCtx);
        }
        return result;
    }

    /**
     * @deprecated since 4.2 - use the Op interface, which can return values, or extend VoidOp.
     * @see info.magnolia.context.MgnlContext.Op
     * @see info.magnolia.context.MgnlContext.VoidOp
     */
    public static interface SystemContextOperation {
        void exec();
    }

    /**
     * A simple execution interface to be used with the doInSystemContext method.
     * If no return value is necessary, return null (for semantic's sake, declare T as <Void>)
     * If no checked exception need to be thrown, declare E as <RuntimeException>)
     *
     * @see MgnlContext#doInSystemContext(Op op)
     * @param <T> the return type of this operation
     * @param <E> an exception this operation can throw
     */
    public static interface Op<T, E extends Throwable> {
        T exec() throws E;
    }

    /**
     * An Op that does not return values and can only throw RuntimeExceptions.
     */
    public abstract static class VoidOp implements Op<Void, RuntimeException> {
        public Void exec() {
            doExec();
            return null;
        }

        abstract public void doExec();
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
        SystemContext systemContext = getSystemContext();
        if(systemContext instanceof ThreadDependentSystemContext){
            ((ThreadDependentSystemContext)systemContext).releaseThread();
        }
    }

    public static void push(HttpServletRequest request, HttpServletResponse response) {
        if (isWebContext()) {
            WebContext wc = getWebContext();
            wc.push(request,response);
        }
    }

    public static void pop() {
        if (isWebContext()) {
            WebContext wc = getWebContext();
            wc.pop();
        }
    }
}
