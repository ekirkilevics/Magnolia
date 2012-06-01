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
package info.magnolia.jaas.sp.jcr;

import info.magnolia.cms.security.SecurityUtil;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.jaas.sp.AbstractLoginModule;
import info.magnolia.jaas.sp.UserAwareLoginModule;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication module implementation using JCR to retrieve the users.
 * @version $Id$
 */
public class JCRAuthenticationModule extends AbstractLoginModule implements UserAwareLoginModule, Serializable {

    private static final Logger log = LoggerFactory.getLogger(JCRAuthenticationModule.class);
    protected User user;

    /**
     * Get number of failed login attempts before locking account.
     */
    public int getMaxAttempts() {
        String realm;
        if (this.user instanceof MgnlUser) {
            realm = ((MgnlUser) user).getRealm();
            //If not supported by user manager then lockout is disabled.
        } else {
            return 0;
        }
        MgnlUserManager manager = (MgnlUserManager) SecuritySupport.Factory.getInstance().getUserManager(realm);
        return manager.getMaxFailedLoginAttempts();
    }

    /**
     * Get time period for time lockout.
     */
    public long getTimeLock(){
        String realm;
        if (this.user instanceof MgnlUser) {
            realm = ((MgnlUser) user).getRealm();
        } else {
            //If not supported by user manager then lockout is disabled.
            return 0;
        }
        MgnlUserManager manager = (MgnlUserManager) SecuritySupport.Factory.getInstance().getUserManager(realm);
        return manager.getLockTimePeriod();
    }

    /**

    /**
     * Checks is the credentials exist in the repository.
     * @throws LoginException or specific subclasses (which will be handled further for user feedback)
     */
    @Override
    public void validateUser() throws LoginException {
        initUser();

        if (this.user == null) {
            throw new AccountNotFoundException("User account " + this.name + " not found.");
        }

        if (!this.user.isEnabled()) {
            throw new AccountLockedException("User account " + this.name + " is locked.");
        }

        matchPassword();

        if (!UserManager.ANONYMOUS_USER.equals(user.getName())) {
            // update last access date for all non anonymous users
            getUserManager().updateLastAccessTimestamp(user);
        }
    }

    private UserManager getUserManager() {
        // can't get the factory upfront and can't use IoC as this class is instantiated by JCR/JAAS before anything else is ready.
        log.debug("getting user manager for realm " + realm.getName());
        return SecuritySupport.Factory.getInstance().getUserManager(realm.getName());
    }

    protected void initUser() throws LoginException {
        log.debug("initializing user {}", name);

        long start = System.currentTimeMillis();
        this.user = getUserManager().getUser(name);
        log.debug("initialized user {} in {}ms", name, (System.currentTimeMillis() - start));
    }

    protected void matchPassword() throws LoginException {
        if(getMaxAttempts() > 0 && !UserManager.ANONYMOUS_USER.equals(user.getName()) && getTimeLock() > 0){
            //Only MgnlUser is able to use lockout for time period like hard-lock (timeLock is higher than 0).
            Calendar currentTime = new GregorianCalendar(TimeZone.getDefault());
            Calendar lockTime = new GregorianCalendar(TimeZone.getDefault());
            MgnlUser mgnlUser = (MgnlUser) user;
            if(mgnlUser.getReleaseTime() != null){
                lockTime.clear();
                lockTime.setTime(mgnlUser.getReleaseTime().getTime());
            }
            if(lockTime.after(currentTime) && mgnlUser.getReleaseTime() != null){
                throw new LoginException("User account " + this.name + " is locked until " + mgnlUser.getReleaseTime().getTime() + ".");
            }
        }
        String serverPassword = user.getPassword();

        if (StringUtils.isEmpty(serverPassword)) {
            throw new FailedLoginException("Magnolia CMS does not allow login to users with no password.");
        }

        boolean match = false;
        if (Base64.isArrayByteBase64(serverPassword.getBytes())) {
            match = Arrays.equals(Base64.decodeBase64(serverPassword), new String(this.pswd).getBytes());
        } else {
            match = SecurityUtil.matchBCrypted(new String(this.pswd), serverPassword);
        }
        if (!match) {
            if (getMaxAttempts() > 0 && !UserManager.ANONYMOUS_USER.equals(user.getName())){
                //Only MgnlUser is able to use lockout i.e. has maxAttempts higher than 0.
                UserManager userManager = getUserManager();
                MgnlUser mgnlUser = (MgnlUser) user;
                userManager.setProperty(mgnlUser, "failedLoginAttempts", ValueFactoryImpl.getInstance().createValue((mgnlUser.getFailedLoginAttempts() + 1)));

                //Hard lock
                if (mgnlUser.getFailedLoginAttempts() >= getMaxAttempts() && getTimeLock() <= 0){
                    userManager.setProperty(mgnlUser, "enabled", ValueFactoryImpl.getInstance().createValue(false));
                    userManager.setProperty(mgnlUser, "failedLoginAttempts", ValueFactoryImpl.getInstance().createValue(0));
                    log.warn("Account " + this.name + " was locked due to high number of failed login attempts.");

                    //Lock for time period
                }else if (mgnlUser.getFailedLoginAttempts() >= getMaxAttempts() && getTimeLock() > 0){
                    userManager.setProperty(mgnlUser, "failedLoginAttempts", ValueFactoryImpl.getInstance().createValue(0));
                    Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
                    calendar.add(Calendar.MINUTE, (int)getTimeLock());
                    userManager.setProperty(mgnlUser, "releaseTime", ValueFactoryImpl.getInstance().createValue(calendar));
                    log.warn("Account " + this.name + " was locked for " + getTimeLock() + " minute(s) due to high number of failed login attempts.");
                }
            }
            if(user instanceof MgnlUser){
                MgnlUser mgnlUser = (MgnlUser) user;
                UserManager userManager = getUserManager();
                if (getMaxAttempts() > 0 && !UserManager.ANONYMOUS_USER.equals(mgnlUser.getName()) && mgnlUser.getFailedLoginAttempts() > 0){
                    userManager.setProperty(mgnlUser, "failedLoginAttempts", ValueFactoryImpl.getInstance().createValue(0));
                }
            }
            throw new FailedLoginException("Passwords do not match");
        }
    }

    /**
     * Set user details.
     */
    @Override
    public void setEntity() {

        this.subject.getPrincipals().add(this.user);
        this.subject.getPrincipals().add(this.realm);

        collectGroupNames();
        collectRoleNames();
    }

    /**
     * Set access control list from the user, roles and groups.
     */
    @Override
    public void setACL() {
    }

    /**
     * Extract all the configured roles from the given node. (which can be the user node or a group node)
     */
    public void collectRoleNames() {
        for (String role : this.user.getAllRoles()) {
            addRoleName(role);
        }
    }

    /**
     * Extract all the configured groups from the given node. (which can be the user node or a group node)
     */
    public void collectGroupNames() {
        for (String group : this.user.getAllGroups()) {
            addGroupName(group);
        }
    }

    @Override
    public User getUser() {
        return user;
    }
}
