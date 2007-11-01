package info.magnolia.context;

import java.util.*;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;

import org.apache.commons.lang.UnhandledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.util.WorkspaceAccessUtil;

public class AuthRepositoryStrategy implements RepositoryAcquringStrategy {
	private static final Logger log = LoggerFactory.getLogger(AuthRepositoryStrategy.class);

	private Map jcrSessions = new HashMap();
	private Map hierarchyManagers = new HashMap();
	private Map accessManagers = new HashMap();
	
	private WebContext context;	
	
	public AuthRepositoryStrategy() {
		
	}
	
	public AuthRepositoryStrategy(WebContext context) {
		this.context = context;
	}
	
	public AccessManager getAccessManager(String repositoryId,
			String workspaceId) {
		AccessManager accessManager = null;

        final String amAttrName = repositoryId + "_" + workspaceId;
        accessManager = (AccessManager) accessManagers.get(amAttrName);
        

        if (accessManager == null) {
            accessManager = WorkspaceAccessUtil.getInstance().createAccessManager(
                getSubject(), repositoryId, workspaceId);
            accessManagers.put(amAttrName, accessManager);            
        }

        return accessManager;
	}

	public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
		final String hmAttrName = repositoryId + "_" + workspaceId;

        HierarchyManager hm = (HierarchyManager) hierarchyManagers.get(hmAttrName);

        if (hm == null) {
            WorkspaceAccessUtil util = WorkspaceAccessUtil.getInstance();
            try {
                hm = util.createHierarchyManager(context.getUser().getName(), 
                		getRepositorySession(repositoryId, workspaceId), 
                		getAccessManager(repositoryId, workspaceId), 
                		getQueryManager(repositoryId, workspaceId));
            }
            catch (RepositoryException e) {
                throw new UnhandledException(e);
            }
            hierarchyManagers.put(hmAttrName, hm);
        }

        return hm;
	}

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
	
	    jcrSession = (Session) jcrSessions.get(repoSessAttrName);
	
	    if (jcrSession == null) {
	        WorkspaceAccessUtil util = WorkspaceAccessUtil.getInstance();
	        jcrSession = util.createRepositorySession(util.getDefaultCredentials(), repositoryName, workspaceName);
	        jcrSessions.put(repoSessAttrName, jcrSession);
	    }
	    return jcrSession;    
	}
	
	protected Subject getSubject() {
        Subject subject = Authenticator.getSubject(context.getRequest());
        return subject;
    }

}
