package info.magnolia.context;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;

import java.util.Map;

public class UserContextImpl extends AbstractContext implements UserContext {

	public void login() {
		// TODO Auto-generated method stub

	}

	public void logout() {
		// TODO Auto-generated method stub

	}

	public AccessManager getAccessManager(String repositoryId,
			String workspaceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getAttribute(String name, int scope) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map getAttributes(int scope) {
		// TODO Auto-generated method stub
		return null;
	}

	public HierarchyManager getHierarchyManager(String repositoryId,
			String workspaceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public QueryManager getQueryManager(String repositoryId, String workspaceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeAttribute(String name, int scope) {
		// TODO Auto-generated method stub

	}

	public void setAttribute(String name, Object value, int scope) {
		// TODO Auto-generated method stub

	}

}
