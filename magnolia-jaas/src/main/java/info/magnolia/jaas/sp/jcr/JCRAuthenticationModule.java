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

import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.jaas.sp.AbstractLoginModule;
import info.magnolia.jaas.sp.UserAwareLoginModule;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.UserPrincipal;
import org.apache.jackrabbit.core.security.principal.AdminPrincipal;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.AccountLockedException;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication module implementation using JCR to retrieve the users.
 * @author Sameer Charles $Id$
 */
// JR requires login module to be serializable!
public class JCRAuthenticationModule extends AbstractLoginModule implements UserAwareLoginModule, Serializable {

    private static final boolean logAdmin = false;
    protected User user;

    /**
     * As silly as it seems this class sole purpose of existence is to implement Serializable required by JR itself.
     * @author had
     * @version $Id$
     */
    public class MagnoliaJRAdminPrincipal extends AdminPrincipal implements Principal, Serializable {
        public MagnoliaJRAdminPrincipal(String name) {
            super(name);
        }
    }


    /**
     * Releases all associated memory.
     */
    @Override
    public boolean release() {
        return true;
    }

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

        matchPassword();

        if (!this.user.isEnabled()) {
            throw new AccountLockedException("User account " + this.name + " is locked.");
        }

        if (!UserManager.ANONYMOUS_USER.equals(user.getName()) && !isAdmin()) {
            // update last access date for all non anonymous users
            getUserManager().updateLastAccessTimestamp(user);
        }
    }

    private UserManager getUserManager() {
        // can't get the factory upfront and can't use IoC as this class is instantiated by JCR/JAAS before anything else is ready.
        if (logAdmin || !"admin".equals(name)) {
            log.debug("getting user manager for realm " + realm.getName());
        }
        return SecuritySupport.Factory.getInstance().getUserManager(realm.getName());
    }



    protected void initUser() throws LoginException {
        if (logAdmin || !"admin".equals(name)) {
            log.debug("initializing user {}", name);
        }
        // we do not verify admin user, against our user base (which is in repo we are trying to access with this user), but we still check the password.
        // This user has assigned no privileges, just dummy near empty user object (with name and pwd only).
        if (isAdmin()) {
            Map<String, String> props = new HashMap<String, String>();
            //TODO: double check if we need to reset pwd here or if getAdminSession() is enough!!!
            props.put(MgnlUserManager.PROPERTY_PASSWORD, new String(Base64.encodeBase64("admin".getBytes())));
            MgnlUser user = new MgnlUser(name, null, Collections.EMPTY_LIST, Collections.EMPTY_LIST,props);
            this.user = user;
            // admin user is used by system and have no access to regular user stuff
            return;
        }

        long start = System.currentTimeMillis();
        this.user = getUserManager().getUser(name);
        if (logAdmin || !"admin".equals(name)) {
            log.debug("initialized user {} in {}ms", name, (System.currentTimeMillis() - start));
        }
    }

    protected void matchPassword() throws LoginException {
        String serverPassword = user.getPassword();

        if (StringUtils.isEmpty(serverPassword)) {
            throw new FailedLoginException("we do not allow users with no password");
        }

        if (!StringUtils.equals(serverPassword, new String(this.pswd))) {
            throw new FailedLoginException("passwords do not match");
        }
    }

    /**
     * Set user details.
     */
    @Override
    public void setEntity() {
        if (isAdmin()) {
            // JR specific principal for repo only authentication. This part will be probably different per repo.
            this.subject.getPrincipals().add(new MagnoliaJRAdminPrincipal(name));
            // admin user is used by system and have no access to regular user stuff
            return;
        } else if ("superuser".equals(name)) {
            // add admin principal also for our superuser to make sure system doesn't deny him any access
            this.subject.getPrincipals().add(new MagnoliaJRAdminPrincipal(name));
        } else {
            // TODO: make JR dependency pluggable!!!
            // and ordinary JR User principal for everybody else
            this.subject.getPrincipals().add(new UserPrincipal(name));

        }

        // our user has our principal!!!!
        this.subject.getPrincipals().add(this.user);
        this.subject.getPrincipals().add(this.realm);


        collectGroupNames();
        collectRoleNames();
    }

    private boolean isAdmin() {
        // TODO: make admin user name configurable (read from properties)
        return this.name != null && this.name.equals("admin");
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
