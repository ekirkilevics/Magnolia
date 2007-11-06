package info.magnolia.context;

import info.magnolia.cms.security.User;

public interface UserContext {
	void login();
	void logout();
}
