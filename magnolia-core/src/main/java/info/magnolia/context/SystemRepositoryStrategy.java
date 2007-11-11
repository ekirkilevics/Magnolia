package info.magnolia.context;

import java.util.ArrayList;
import java.util.List;

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.security.SystemUserManager;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.cms.util.WorkspaceAccessUtil;

public class SystemRepositoryStrategy extends AbstractRepositoryStrategy {

	private static final long serialVersionUID = 222L;
	
	private AccessManager accessManager;
	
	public SystemRepositoryStrategy(SystemContext context) {
	}
	
	public AccessManager getAccessManager(String repositoryId,
			String workspaceId) {
		if (accessManager == null) {
			accessManager = WorkspaceAccessUtil.getInstance().createAccessManager(getSystemPermissions()); 
		}
		return accessManager;
	}
		    
    protected List getSystemPermissions() {
        List acl = new ArrayList();
        UrlPattern p = UrlPattern.MATCH_ALL;
        Permission permission = new PermissionImpl();
        permission.setPattern(p);
        permission.setPermissions(Permission.ALL);
        acl.add(permission);
        return acl;
    }

    protected String getUserId() {
        return SystemUserManager.SYSTEM_USER;
    }

}
