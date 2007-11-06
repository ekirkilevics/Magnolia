package info.magnolia.context;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.callback.PlainTextCallbackHandler;
import info.magnolia.cms.util.ObservationUtil;

import java.util.Map;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserContextImpl extends AbstractContext implements UserContext {
	private static final Logger log = LoggerFactory.getLogger(UserContextImpl.class);
	
	private static final long serialVersionUID = 222L;
	
	private static Subject anonymousSubject;

    private static User anonymousUser;
    
    

	private static Subject getAnonymousSubject() {
        if (null == anonymousSubject) {
            setAnonymousSubject();
        }
        return anonymousSubject;
    }

    private static void setAnonymousSubject() {
        CredentialsCallbackHandler callbackHandler = new PlainTextCallbackHandler(
            getAnonymousUser().getName(),
            getAnonymousUser().getPassword().toCharArray(),
            Realm.REALM_SYSTEM);
        try {
            LoginContext loginContext = new LoginContext("magnolia", callbackHandler);
            loginContext.login();
            anonymousSubject = loginContext.getSubject();
        }
        catch (LoginException le) {
            log.error("Failed to login as anonymous user", le);
        }
    }

    private static User getAnonymousUser() {
        if (null == anonymousUser) {
            setAnonymousUser();
        }
        return anonymousUser;
    }

    private static void setAnonymousUser() {
        anonymousUser = Security.getUserManager().getAnonymousUser();
    }

    private synchronized static void reset() {
        //setAnonymousSubject();
        //setAnonymousUser();
        log.info("Anonymous context reloaded");
    }

	public void login() {
		// TODO Auto-generated method stub
		
	}

	public void logout() {
		// TODO Auto-generated method stub
		
	}
}
