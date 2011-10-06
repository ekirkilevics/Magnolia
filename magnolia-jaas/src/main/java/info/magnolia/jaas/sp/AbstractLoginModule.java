/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.jaas.sp;


import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.auth.callback.RealmCallback;
import info.magnolia.cms.util.BooleanUtil;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract implementation of the <code>LoginModule</code> providing common methods and constants implementation.
 * @author Sameer Charles
 * $Id$
 */
public abstract class AbstractLoginModule implements LoginModule {

    // magnolia specific option to define if "this" module needs to be
    // skipped based on previous (in JAAS module chain) module status
    public static final String OPTION_SKIP_ON_PREVIOUS_SUCCESS = "skip_on_previous_success";

    public static final String OPTION_REALM = "realm";

    public static final String OPTION_USE_REALM_CALLBACK= "use_realm_callback";

    public static final String STATUS = "statusValue";

    public static final int STATUS_SUCCEEDED = 1;

    /**
     * @deprecated use STATUS_SUCCEEDED
     */
    @Deprecated
    public static final int STATUS_SUCCEDED = STATUS_SUCCEEDED;

    public static final int STATUS_FAILED = 2;

    public static final int STATUS_SKIPPED = 3;

    public static final int STATUS_UNAVAILABLE = 4;

    // TODO: implement the following commonly supported flags to allow single signon with third party modules

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

    public Map<String, Object> sharedState;

    public Map<String, Object> options;

    public String name;

    public char[] pswd;

    /**
     * The realm we login into. Initialized by the option realm.
     */
    protected Realm realm = Realm.REALM_ALL;

    /**
     * Allow the client to define the realm he logs into. Default value is false
     */
    protected boolean useRealmCallback;

    // this status is sent back to the LoginModule chain
    public boolean success;

    protected Logger log = LoggerFactory.getLogger(getClass());

    private boolean skipOnPreviousSuccess;


    /**
     *
     */
    public AbstractLoginModule() {

    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, final Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        // don't overwrite group and roles set in the shared state
        if (this.sharedState.get("groupNames") == null) {
            this.sharedState.put("groupNames", new LinkedHashSet<String>());
        }
        if (this.sharedState.get("roleNames") == null) {
            this.sharedState.put("roleNames", new LinkedHashSet<String>());
        }
        String realmName = (String) options.get(OPTION_REALM);
        this.realm = StringUtils.isBlank(realmName) ? Realm.DEFAULT_REALM : new Realm.RealmImpl(realmName);

        this.useRealmCallback = BooleanUtil.toBoolean((String) options.get(OPTION_USE_REALM_CALLBACK), true);
        this.skipOnPreviousSuccess = BooleanUtil.toBoolean((String) options.get(OPTION_SKIP_ON_PREVIOUS_SUCCESS), false);
    }

    @Override
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
                String aRealm = ((RealmCallback) callbacks[2]).getRealm();
                this.realm = StringUtils.isBlank(aRealm) ? this.realm : new Realm.RealmImpl(aRealm);
            }

            this.validateUser();
        } catch (IOException ioe) {
            log.debug("Exception caught", ioe);
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException ce) {
            log.debug(ce.getMessage(), ce);
            throw new LoginException(ce.getCallback().toString() + " not available");
        }

        // TODO: should not we set success BEFORE calling validateUser to give it chance to decide whether to throw an exception or reset the value to false?
        this.success = true;
        this.setSharedStatus(STATUS_SUCCEEDED);
        return this.success;
    }


    /**
     * Updates subject with ACL and other properties.
     */
    @Override
    public boolean commit() throws LoginException {
        /**
         * If login module failed to authenticate then this method should simply return false
         * instead of throwing an exception - refer to specs for more details
         * */
        if (!this.success) {
            return false;
        }
        this.setEntity();
        this.setACL();
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return this.release();
    }

    @Override
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
     * Sets shared status value to be used by subsequent LoginModule(s).
     * */
    public void setSharedStatus(int status) {
        this.sharedState.put(STATUS, new Integer(status));
    }

    /**
     * Tests if the option skip_on_previous_success is set to true and preceding LoginModule was successful.
     * */
    protected boolean getSkip() {
        return skipOnPreviousSuccess && this.getSharedStatus() == STATUS_SUCCEEDED;
    }

    public void setGroupNames(Set<String> names) {
        this.getGroupNames().addAll(names);
    }

    public void addGroupName(String groupName) {
        getGroupNames().add(groupName);
    }

    public Set<String> getGroupNames() {
        return (Set<String>) this.sharedState.get("groupNames");
    }

    public void setRoleNames(Set<String> names) {
        this.getRoleNames().addAll(names);
    }

    public void addRoleName(String roleName) {
        getRoleNames().add(roleName);
    }

    public Set<String> getRoleNames() {
        return (Set<String>) this.sharedState.get("roleNames");
    }

    /**
     * Releases all associated memory.
     */
    public boolean release() {
        return true;
    }

    /**
     * Checks if the credentials exist in the repository.
     * @throws LoginException or specific subclasses to report failures.
     */
    public abstract void validateUser() throws LoginException;

    /**
     * Sets user details.
     */
    public abstract void setEntity();

    /**
     * Sets access control list from the user, roles and groups.
     */
    public abstract void setACL();

}
