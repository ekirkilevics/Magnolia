package info.magnolia.context;

import java.util.ArrayList;
import java.util.List;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.cms.util.WorkspaceAccessUtil;

public class SharedAccessManagerStrategy implements RepositoryAcquringStrategy {

	private static final long serialVersionUID = 222L;
	
	private AccessManager accessManager;
	
	public SharedAccessManagerStrategy() {
	
	}
	
	public AccessManager getAccessManager(String repositoryId,
			String workspaceId) {
		if (accessManager == null) {
			accessManager = WorkspaceAccessUtil.getInstance().createAccessManager(getSystemPermissions()); 
		}
		return accessManager;
	}
		    
    private static List getSystemPermissions() {
        List acl = new ArrayList();
        UrlPattern p = UrlPattern.MATCH_ALL;
        Permission permission = new PermissionImpl();
        permission.setPattern(p);
        permission.setPermissions(Permission.ALL);
        acl.add(permission);
        return acl;
    }

	public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {		
		return ContentRepository.getHierarchyManager(repositoryId, workspaceId);
	}

	public QueryManager getQueryManager(String repositoryId, String workspaceId) {
		return getHierarchyManager(repositoryId, workspaceId).getQueryManager();
	}

}
