package info.magnolia.jaas.sp;


import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.auth.callback.RealmCallback;

import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
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

    public static final Logger log = LoggerFactory.getLogger(AbstractLoginModule.class);

    // magnolia specific option to define if "this" module needs to be
    // skipped based on previous (in JAAS module chain) module status
    public static final String OPTION_SKIP_ON_PREVIOUS_SUCCESS = "skip_on_previous_success";

    public static final String OPTION_REALM = "realm";

    public static final String OPTION_USE_REALM_CALLBACK= "use_realm_callback";
    
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

    /**
     * The realm we login into. Initialized by the option realm.
     */
    protected String realm = Realm.REALM_ALL;

    /**
     * Allow the client to define the realm he logs into. Default value is false
     */
    protected boolean useRealmCallback;

    // this status is sent back to the LoginModule chain
    public boolean success;

    private boolean skipOnPreviousSuccess;


    /**
     *
     */
    public AbstractLoginModule() {

    }

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        this.sharedState.put("groupNames",new LinkedHashSet());
        this.sharedState.put("roleNames",new LinkedHashSet());
        this.realm = StringUtils.defaultIfEmpty((String) options.get(OPTION_REALM), Realm.DEFAULT_REALM);

        // null --> false
        this.useRealmCallback = BooleanUtils.toBoolean(StringUtils.defaultIfEmpty((String) options.get(OPTION_USE_REALM_CALLBACK), "false"));
        this.skipOnPreviousSuccess = BooleanUtils.toBoolean(StringUtils.defaultIfEmpty((String) options.get(OPTION_SKIP_ON_PREVIOUS_SUCCESS), "false"));
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

        // if the realm is not defined in the jaas configuration
        // we ask use a callback to get the value
        if(this.useRealmCallback){
            callbacks = (Callback[]) ArrayUtils.add(callbacks, new RealmCallback());
        }

        this.success = false;
        try {
            this.callbackHandler.handle(callbacks);
            this.name = ((NameCallback) callbacks[0]).getName();
            this.pswd = ((PasswordCallback) callbacks[1]).getPassword();
            if(this.useRealmCallback){
                this.realm = StringUtils.defaultIfEmpty(((RealmCallback)callbacks[2]).getRealm(), this.realm);
            }
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
        this.setACL();
        return true;
    }

    public boolean abort() throws LoginException {
        return this.release();
    }

    public boolean logout() throws LoginException {
        return this.release();
    }

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

    /**
     * test if the option skip_on_previous_success is set to true
     * and preceding LoginModule was successful
     * */
    private boolean getSkip() {
        return skipOnPreviousSuccess && this.getSharedStatus() == STATUS_SUCCEDED;
    }

    public void setGroupNames(Set names) {
        this.getGroupNames().addAll(names);
    }

    public void addGroupName(String groupName) {
        getGroupNames().add(groupName);
    }

    public Set getGroupNames() {
        return (Set) this.sharedState.get("groupNames");
    }

    public void setRoleNames(Set names) {
        this.getRoleNames().addAll(names);
    }

    public void addRoleName(String roleName) {
        getRoleNames().add(roleName);
    }

    public Set getRoleNames() {
        return (Set) this.sharedState.get("roleNames");
    }

    /**
     * Releases all associated memory
     */
    public abstract boolean release();

    /**
     * checks if the credentials exist in the repository
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
