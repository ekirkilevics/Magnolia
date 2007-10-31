package info.magnolia.context;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;

public interface RepositoryAcquringStrategy {
	HierarchyManager getHierarchyManager(String repositoryId, String workspaceId);
	AccessManager getAccessManager(String repositoryId, String workspaceId);
	QueryManager getQueryManager(String repositoryId, String workspaceId);
}
