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

import java.util.HashMap;
import java.util.Map;

import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;
import info.magnolia.cms.core.HierarchyManager;


/**
 * Simple context delegating methods to the thread local context. This context should never get used as the threads
 * local context, but is usable in other contexts like for passing it to a command.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class SimpleContext extends AbstractMapBasedContext {

    /**
     * The context used to get hierarchy managers or similar.
     */
    private Context ctx;

    /**
     * Uses current instance of <code>MgnlContext</code> at the time of creation as it's internal reference context.
     */
    public SimpleContext() {
        this(MgnlContext.getInstance());
    }

    /**
     * Decorate a map. If passed map is an instance of the context, this context will be used to obtain HM and check access instead of default context.
     */
    public SimpleContext(Map<String, Object> map) {
        super(map instanceof Context ? new HashMap() : map);
        if (map instanceof Context) {
            this.ctx = (Context) map;
        } else {
            this.ctx = MgnlContext.getInstance();
        }
    }

    /**
     * Delegate to the inner context.
     */
    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return this.ctx.getAccessManager(repositoryId, workspaceId);
    }

    /**
     * Delegate to the inner context.
     */
    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        return this.ctx.getHierarchyManager(repositoryId, workspaceId);
    }

    /**
     * Delegate to the inner context.
     */
    public User getUser() {
        return this.ctx.getUser();
    }

    /**
     * Delegate to the inner context.
     */
    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return this.ctx.getQueryManager(repositoryId, workspaceId);
    }

    public void release() {
        this.ctx.release();
    }

}
