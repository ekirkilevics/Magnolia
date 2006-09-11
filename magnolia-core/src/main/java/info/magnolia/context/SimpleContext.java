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

import java.util.Map;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;


/**
 * Simple context delegatin methods to the threads locale context. This context should never get used as the threads
 * locale context, but is useable in other contextes like for passing it to a command.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class SimpleContext extends AbstractMapBasedContext {

    /**
     * The context used to get hierarchy managers or similar.
     */
    private Context ctx;
    
    /**
     * Using the threads locale context for getting hierarchy managers or similar
     */
    public SimpleContext() {
        this(MgnlContext.getInstance());
    }
    
    /**
     * Decorate a map
     */
    public SimpleContext(Map map) {
        super(map);
        this.ctx = MgnlContext.getInstance();
    }
    

    /**
     * Use the passed context to get hierarchy managers or similar form
     */
    public SimpleContext(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Delegate to the inner context
     */
    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return this.ctx.getAccessManager(repositoryId, workspaceId);
    }

    /**
     * Delegate to the inner context
     */
    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        return this.ctx.getHierarchyManager(repositoryId, workspaceId);
    }

    /**
     * Delegate to the inner context
     */
    public User getUser() {
        return this.ctx.getUser();
    }

    /**
     * Delegate to the inner context
     */
    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return this.ctx.getQueryManager(repositoryId, workspaceId);
    }

}
