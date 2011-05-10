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

import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;

import java.util.Map;


/**
 * Subclass this context if you like to decorate an other context.
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
     * Delegates call to the original context.
     */
    @Override
    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return this.ctx.getAccessManager(repositoryId, workspaceId);
    }

    /**
     * Delegates call to the original context.
     */
    @Override
    public Object getAttribute(String name, int scope) {
        return this.ctx.getAttribute(name, scope);
    }

    /**
     * Delegates call to the original context.
     */
    @Override
    public Map<String, Object> getAttributes(int scope) {
        return this.ctx.getAttributes(scope);
    }

    /**
     * Delegates call to the original context.
     */
    @Override
    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        return this.ctx.getHierarchyManager(repositoryId, workspaceId);
    }

    /**
     * Delegates call to the original context.
     */
    @Override
    public User getUser() {
        return this.ctx.getUser();
    }

    /**
     * Delegates call to the original context.
     */
    @Override
    public void setAttribute(String name, Object value, int scope) {
        this.ctx.setAttribute(name, value, scope);
    }

    /**
     * Delegates call to the original context.
     */
    @Override
    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return this.ctx.getQueryManager(repositoryId, workspaceId);
    }

    /**
     * Delegates call to the original context.
     */
    @Override
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

    @Override
    public void release() {
        this.ctx.release();
    }

}
