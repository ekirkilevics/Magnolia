package info.magnolia.context;

import info.magnolia.cms.security.User;

public interface UserContext {
    User getUser();
	void login(User user);
	void logout();
}
