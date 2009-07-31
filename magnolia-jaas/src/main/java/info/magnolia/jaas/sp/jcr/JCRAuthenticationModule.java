/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.security.auth.Entity;
import info.magnolia.jaas.principal.EntityImpl;
import info.magnolia.jaas.sp.AbstractLoginModule;
import info.magnolia.jaas.sp.UserAwareLoginModule;
import org.apache.commons.lang.StringUtils;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.AccountLockedException;
import java.util.Iterator;


/**
 * @author Sameer Charles $Id$
 */
public class JCRAuthenticationModule extends AbstractLoginModule implements UserAwareLoginModule {
    protected User user;

    /**
     * Releases all associated memory.
     */
    public boolean release() {
        return true;
    }

    /**
     * Checks is the credentials exist in the repository.
     * @throws LoginException or specific subclasses (which will be handled further for user feedback)
     */
    public void validateUser() throws LoginException {
        initUser();

        if (this.user == null) {
            throw new AccountNotFoundException("User account " + this.name + " not found.");
        }

        matchPassword();

        if (!this.user.isEnabled()) {
            throw new AccountLockedException("User account " + this.name + " is locked.");
        }
    }

    protected void initUser() {
        user = getUserManager().getUser(name);
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
     * Override this to support any configured/non-configured user manager.
     */
    public UserManager getUserManager() {
        SecuritySupport securitySupport = SecuritySupport.Factory.getInstance();
        return securitySupport.getUserManager(this.realm);
    }

    /**
     * Set user details.
     */
    public void setEntity() {
        EntityImpl entity = new EntityImpl();
        entity.addProperty(Entity.LANGUAGE, this.user.getLanguage());
        entity.addProperty(Entity.NAME, this.user.getName());

        String fullName = this.user.getProperty("title");
        if(fullName != null){
            entity.addProperty(Entity.FULL_NAME, fullName);
        }
        entity.addProperty(Entity.PASSWORD, new String(this.pswd));
        this.subject.getPrincipals().add(entity);

        collectGroupNames();
        collectRoleNames();
    }

    /**
     * Set access control list from the user, roles and groups.
     */
    public void setACL() {
    }

    /**
     * Extract all the configured roles from the given node. (which can be the user node or a group node)
     */
    public void collectRoleNames() {
        for (Iterator iter = this.user.getAllRoles().iterator(); iter.hasNext();) {
            addRoleName((String)iter.next());
        }
    }

    /**
     * Extract all the configured groups from the given node. (which can be the user node or a group node)
     */
    public void collectGroupNames() {
        for (Iterator iter = this.user.getAllGroups().iterator(); iter.hasNext();) {
            addGroupName((String) iter.next());
        }
    }

    public User getUser() {
        return user;
    }

}
