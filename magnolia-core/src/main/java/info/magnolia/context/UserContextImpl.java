package info.magnolia.context;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;

import java.util.Map;

public class UserContextImpl extends AbstractContext implements UserContext {
	private static final long serialVersionUID = 222L;

	public void login(User user) {
		setUser(user);
		
	}

	public void logout() {
		setUser(null);

	}

}
