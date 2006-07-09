package info.magnolia.jaas.sp;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;


/**
 * Date: Aug 10, 2005 Time: 5:47:36 PM
 * @author Sameer Charles $Id$
 */
public abstract class AbstractLoginModule implements LoginModule {

    public Subject subject;

    public CallbackHandler callbackHandler;

    public Map sharedState;

    public Map options;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    /**
     * Authenticate against magnolia/jcr user repository
     */
    public abstract boolean login() throws LoginException;

    /**
     * Update subject with ACL and other properties
     */
    public abstract boolean commit() throws LoginException;

    public boolean abort() throws LoginException {
        return this.release();
    }

    public boolean logout() throws LoginException {
        return this.release();
    }

    /**
     * Releases all associated memory
     */
    public abstract boolean release();

    /**
     * checks is the credentials exist in the repository
     * @return boolean
     */
    public abstract boolean isValidUser();

    /**
     * set user details
     */
    public abstract void setEntity();

    /**
     * set access control list from the user, roles and groups
     */
    public abstract void setACL();

}
