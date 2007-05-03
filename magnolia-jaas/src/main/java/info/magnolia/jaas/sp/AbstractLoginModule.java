package info.magnolia.jaas.sp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.io.IOException;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.spi.LoginModule;


/**
 * @author Sameer Charles
 * $Id$
 */
public abstract class AbstractLoginModule implements LoginModule {

    public static Logger log = LoggerFactory.getLogger(AbstractLoginModule.class);

    // magnolia specific option to define if "this" module needs to be
    // skipped based on previous (in JAAS module chain) module status
    public static final String SKIP_ON_PREVIOUS_SUCCESS = "skip_on_previous_success";

    public static final String STATUS = "statusValue";

    public static final int STATUS_SUCCEDED = 1;

    public static final int STATUS_FAILED = 2;

    public static final int STATUS_SKIPPED = 3;

    public static final int STATUS_UNAVAILABLE = 4;

    /**
     * todo implement the following commonly supported flags to allow single signon with third party modules
     * */

    //If true, the first LoginModule in the stack saves the password entered,
    // and subsequent LoginModules also try to use it. If authentication fails,
    // the LoginModules prompt for a new password and retry the authentication.
    public static final String TRY_FIRST_PASS = "try_first_pass";

    //If true, the first LoginModule in the stack saves the password entered,
    // and subsequent LoginModules also try to use it.
    // LoginModules do not prompt for a new password if authentication fails (authentication simply fails).
    public static final String USE_FIRST_PASS = "use_first_pass";

    //If true, the first LoginModule in the stack saves the password entered,
    // and subsequent LoginModules attempt to map it into their service-specific password.
    // If authentication fails, the LoginModules prompt for a new password and retry the authentication.
    public static final String TRY_MAPPED_PASS = "try_mapped_pass";

    //If true, the first LoginModule in the stack saves the password entered,
    // and subsequent LoginModules attempt to map it into their service-specific password.
    // LoginModules do not prompt for a new password if authentication fails (authentication simply fails).
    public static final String USE_MAPPED_PASS = "use_mapped_pass";

    public Subject subject;

    public CallbackHandler callbackHandler;

    public Map sharedState;

    public Map options;

    public String name;

    public char[] pswd;

    // this status is sent back to the LoginModule chain
    public boolean success;


    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    public boolean login() throws LoginException {

        if (this.getSkip()) {
            return true;
        }

        if (this.callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available");
        }

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("name");
        callbacks[1] = new PasswordCallback("pswd", false);

        this.success = false;
        try {
            this.callbackHandler.handle(callbacks);
            this.name = ((NameCallback) callbacks[0]).getName();
            this.pswd = ((PasswordCallback) callbacks[1]).getPassword();
            this.success = this.validateUser();
        } catch (IOException ioe) {
            if (log.isDebugEnabled()) {
                log.debug("Exception caught", ioe);
            }
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException ce) {
            if (log.isDebugEnabled()) {
                log.debug(ce.getMessage(), ce);
            }
            throw new LoginException(ce.getCallback().toString() + " not available");
        }

        if (!this.success) {
            throw new FailedLoginException("failed to authenticate " + this.name);
        }

        this.setSharedStatus(STATUS_SUCCEDED);
        return this.success;
    }


    /**
     * Update subject with ACL and other properties
     */
    public boolean commit() throws LoginException {
        if (this.getSkip()) {
            return true;
        }
        if (!this.success) {
            throw new LoginException("failed to authenticate " + this.name);
        }
        this.setEntity();
        return true;
    }

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
     * @return shared status value as set by this LoginModule
     * */
    public int getSharedStatus() {
        Integer status = (Integer) this.sharedState.get(STATUS);
        if (null != status) {
            return status.intValue();
        }
        return STATUS_UNAVAILABLE;
    }

    /**
     * Set shared status value to be used by subsequent LoginModule(s)
     * */
    public void setSharedStatus(int status) {
        this.sharedState.put(STATUS, new Integer(status));
    }

    public String getOptionValue(String attribute) {
        String value = (String) this.options.get(attribute);
        if (null != value) {
            return value;
        }
        return "";
    }


    /**
     * private utility to test if the option skip_on_previous_success is set to true
     * and preceding LoginModule was successful
     * */
    public boolean getSkip() {
        if ("true".equalsIgnoreCase(getOptionValue(SKIP_ON_PREVIOUS_SUCCESS))) {
            if (this.getSharedStatus() == STATUS_SUCCEDED) {
                return true;
            }
        }
        return false;
    }


    /**
     * checks is the credentials exist in the repository
     * @return boolean
     */
    public abstract boolean validateUser() throws FailedLoginException, LoginException;

    /**
     * set user details
     */
    public abstract void setEntity();

    /**
     * set access control list from the user, roles and groups
     */
    public abstract void setACL();

}
