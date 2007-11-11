package info.magnolia.context;

import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.User;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserContextImpl extends AbstractContext implements UserContext {
	private static final Logger log = LoggerFactory.getLogger(UserContextImpl.class);
	
	private static final long serialVersionUID = 222L;
    
    private static final String SESSION_USER = WebContextImpl.class.getName() + ".user";
    
    private User user;
    
    public UserContextImpl() {
        
    }
    
    /**
     * Create the subject on demand.
     * @see info.magnolia.context.AbstractContext#getUser()
     */

    public User getUser() {
        if (user == null) {
            user = (User) getAttribute(SESSION_USER, Context.SESSION_SCOPE);
            if (user == null) {
                user = Authenticator.getAnonymousUser();
            }
        }
        return this.user;
    }
    
	public void login(User user) {
        setLocale(new Locale(user.getLanguage()));
        setAttribute(SESSION_USER, user, Context.SESSION_SCOPE);
	}

	public void logout() {
        removeAttribute(SESSION_USER, Context.SESSION_SCOPE);
		login(Authenticator.getAnonymousUser());
	}
}
