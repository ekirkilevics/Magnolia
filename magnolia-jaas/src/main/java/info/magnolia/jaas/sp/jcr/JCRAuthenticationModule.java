/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.jaas.sp.jcr;

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.security.auth.Entity;
import info.magnolia.jaas.principal.EntityImpl;
import info.magnolia.jaas.sp.AbstractLoginModule;

import java.util.Iterator;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.AccountNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles $Id$
 */
public class JCRAuthenticationModule extends AbstractLoginModule {

    private static final Logger log = LoggerFactory.getLogger(JCRAuthenticationModule.class);

    protected User user;

    /**
     * Releases all associated memory
     */
    public boolean release() {
        return true;
    }

    /**
     * checks is the credentials exist in the repository
     * @throws LoginException or specific subclasses (which will be handled further for user feedback)
     */
    public User validateUser() throws LoginException {
        initUser();

        if(this.user == null){
            throw new AccountNotFoundException("user " + this.name + " not found");
        }

        matchPassword();

        if (!this.user.isEnabled()) {
            throw new AccountLockedException();
        }
        return user;
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
     * override this to support any configured/non-configured user manager
     * */
    public UserManager getUserManager() {
        SecuritySupport securitySupport = SecuritySupport.Factory.getInstance();
        return securitySupport.getUserManager(this.realm);
    }

    /**
     * set user details
     */
    public void setEntity() {
        EntityImpl entity = new EntityImpl();
        entity.addProperty(Entity.LANGUAGE, this.user.getLanguage());
        entity.addProperty(Entity.NAME, this.user.getName());
        entity.addProperty(Entity.FULL_NAME, this.user.getProperty("title"));
        entity.addProperty(Entity.PASSWORD, new String(this.pswd));
        this.subject.getPrincipals().add(entity);

        collectGroupNames();
        collectRoleNames();
    }

    /**
     * set access control list from the user, roles and groups
     */
    public void setACL() {
    }

    /**
     * Extract all the configured roles from the given node (which can be the user node or a group node)
     */
    public void collectRoleNames() {
        for (Iterator iter = this.user.getAllRoles().iterator(); iter.hasNext();) {
            addRoleName((String)iter.next());
        }
    }

    /**
     * Extract all the configured groups from the given node (which can be the user node or a group node)
     */
    public void collectGroupNames() {
        for (Iterator iter = this.user.getAllGroups().iterator(); iter.hasNext();) {
            addGroupName((String) iter.next());
        }
    }

}
