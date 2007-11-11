/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.context;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.util.WorkspaceAccessUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.UnhandledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public abstract class AbstractRepositoryStrategy implements RepositoryAcquringStrategy{
    
    private static Logger log = LoggerFactory.getLogger(AbstractRepositoryStrategy.class);
    
    private Map jcrSessions = new HashMap();
    
    private Map hierarchyManagers = new HashMap();

    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
    	final String hmAttrName = repositoryId + "_" + workspaceId;
    	ThreadLocal localManager = (ThreadLocal) hierarchyManagers.get(hmAttrName);
        
        if (localManager == null) {
            WorkspaceAccessUtil util = WorkspaceAccessUtil.getInstance();
            try {
                // FIXME handle the cast a bit nicer
            	HierarchyManager hm = util.createHierarchyManager(getUserId(), 
                		getRepositorySession(repositoryId, workspaceId), 
                		getAccessManager(repositoryId, workspaceId), 
                		getQueryManager(repositoryId, workspaceId));
            	localManager = new ThreadLocal();
            	localManager.set(hm);
            }
            catch (RepositoryException e) {
                throw new UnhandledException(e);
            }
            hierarchyManagers.put(hmAttrName, localManager);
        }
        
        HierarchyManager hm = (HierarchyManager) localManager.get();
    
        return hm;
    }

    abstract protected String getUserId();
    
    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
    	QueryManager queryManager = null;
        try {
            queryManager = WorkspaceAccessUtil.getInstance().createQueryManager(
                getRepositorySession(repositoryId, workspaceId),
                getAccessManager(repositoryId, workspaceId));
        }
        catch (Exception t) {
            log.error("Failed to create QueryManager", t);
        }
    
        return queryManager;
    }

    protected Session getRepositorySession(String repositoryName, String workspaceName) throws LoginException, RepositoryException {
        Session jcrSession = null;
    
        final String repoSessAttrName = repositoryName + "_" + workspaceName;
        
        ThreadLocal localSession = (ThreadLocal) jcrSessions.get(repoSessAttrName);
    	   	
        if (localSession == null) {
            WorkspaceAccessUtil util = WorkspaceAccessUtil.getInstance();
            jcrSession = util.createRepositorySession(util.getDefaultCredentials(), repositoryName, workspaceName);
            localSession = new ThreadLocal();
            localSession.set(jcrSession);
            jcrSessions.put(repoSessAttrName, localSession);
        }
        
        jcrSession = (Session) localSession.get();
        return jcrSession;    
    }

    public void release() {
        for (Iterator iter = jcrSessions.values().iterator(); iter.hasNext();) {
            Session session = (Session) iter.next();
            if(session != null && session.isLive()){
                session.logout();
            }
        }
        hierarchyManagers.clear();
        jcrSessions.clear();
    }

}