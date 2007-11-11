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

import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;

import java.util.Map;


/**
 * Subclass this context if you like to decorate an other context
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ContextDecorator extends AbstractContext {

    protected Context ctx;

    /**
     * @param ctx the context to decorate
     */
    public ContextDecorator(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Delegate
     */
    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return this.ctx.getAccessManager(repositoryId, workspaceId);
    }

    /**
     * Delegate
     */
    public Object getAttribute(String name, int scope) {
        return this.ctx.getAttribute(name, scope);
    }

    /**
     * Delegate
     */
    public Map getAttributes(int scope) {
        return this.ctx.getAttributes(scope);
    }

    /**
     * Delegate
     */
    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        return this.ctx.getHierarchyManager(repositoryId, workspaceId);
    }

    /**
     * Delegate
     */
    public User getUser() {
        return this.ctx.getUser();
    }

    /**
     * Delegate
     */
    public void setAttribute(String name, Object value, int scope) {
        this.ctx.setAttribute(name, value, scope);
    }

    /**
     * Delegate
     */
    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return this.ctx.getQueryManager(repositoryId, workspaceId);
    }

    /**
     * Delegate
     */
    public void removeAttribute(String name, int scope) {
        this.ctx.removeAttribute(name, scope);
    }

    /**
     * Returns the context wrapped by this decorator.
     * @return wrapped context
     */
    public Context getWrappedContext() {
        return this.ctx;
    }

}
