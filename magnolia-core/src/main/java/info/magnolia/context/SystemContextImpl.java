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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is the system context using the not secured HierarchyManagers. The context uses only one scope.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class SystemContextImpl extends AbstractMapBasedContext implements SystemContext {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(SystemContextImpl.class);

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * DON'T CREATE AN OBJECT. The SystemContext is set by magnolia system itself. Init the scopes
     */
    public SystemContextImpl() {
    }

    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        return ContentRepository.getHierarchyManager(repositoryId, workspaceId);
    }

    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return ContentRepository.getAccessManager();
    }

    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return this.getHierarchyManager(repositoryId, workspaceId).getQueryManager();
    }

    public void setAttribute(String name, Object value, int scope) {
        if(scope == MgnlContext.REQUEST_SCOPE || scope == MgnlContext.SESSION_SCOPE){
            log.warn("you should not set an attribute in the system context in request or session scope. You are setting {}={}", name, value);
        }
        super.setAttribute(name, value, scope);
    }

    public void removeAttribute(String name, Object value, int scope) {
        if(scope == MgnlContext.REQUEST_SCOPE || scope == MgnlContext.SESSION_SCOPE){
            log.warn("you should not manipulate an attribute in the system context in request or session scope. You are setting {}={}", name, value);
        }
        super.removeAttribute(name, scope);
    }

    /**
     * Get System user
     *
     * @return User
     * @see info.magnolia.cms.security.User
     */
    public User getUser() {
        if (this.user == null) {
            log.debug("JAAS Subject is null, returning Anonymous user");
            this.user = Security.getUserManager().getSystemUser();
        }
        return this.user;
    }
}
