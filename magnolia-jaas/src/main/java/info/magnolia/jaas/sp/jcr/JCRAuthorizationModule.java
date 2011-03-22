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

import info.magnolia.cms.security.ACLImpl;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.security.auth.GroupList;
import info.magnolia.cms.security.auth.PrincipalCollection;
import info.magnolia.cms.security.auth.RoleList;
import info.magnolia.jaas.principal.GroupListImpl;
import info.magnolia.jaas.principal.PrincipalCollectionImpl;
import info.magnolia.jaas.principal.RoleListImpl;
import info.magnolia.jaas.sp.AbstractLoginModule;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a default login module for magnolia, it uses initialized repository as defined by the provider interface.
 * @author Sameer Charles
 * @version $Id$
 */
public class JCRAuthorizationModule extends AbstractLoginModule {

    @Override
    public void validateUser() throws LoginException {
    }

    private static final Logger log = LoggerFactory.getLogger(JCRAuthorizationModule.class);

    // do nothing here, we are only responsible for authorization, not authentication!
    @Override
    public boolean login() throws LoginException
    {
        this.success = true;
        this.setSharedStatus(STATUS_SUCCEEDED);
        return this.success;
    }

    /**
     * Sets access control list from the user, roles and groups.
     */
    @Override
    public void setACL() {
        String[] roles = getRoleNames().toArray(new String[getRoleNames().size()]);
        String[] groups = getGroupNames().toArray(new String[getGroupNames().size()]);

        if (log.isDebugEnabled()) {
            log.debug("Roles: {}", ArrayUtils.toString(roles));
            log.debug("Groups: {}", ArrayUtils.toString(groups));
        }

        addRoles(roles);
        addGroups(groups);

        PrincipalCollection principalList = new PrincipalCollectionImpl();
        setACLForRoles(roles, principalList);
        setACLForGroups(groups, principalList);
        User user = null;

        // can't obtain SS instance at creation time as repo is not initialized yet and class can't be instantiated.
        SecuritySupport securitySupport = SecuritySupport.Factory.getInstance();
        user = securitySupport.extractUser(subject);
        // not all jaas modules will support magnolia users
        if(user == null) {
            user = securitySupport.getUserManager().getUser(subject);
        }

        if (user instanceof MgnlUser) {
            setACLForUser(principalList, user, securitySupport);
        }

        if (log.isDebugEnabled()) {
            for (Iterator<Principal> iterator =  principalList.iterator(); iterator.hasNext();) {
                Principal principal = iterator.next();
                log.debug("ACL: {}", principal);
            }
        }

        // set principal list, a set of info.magnolia.jaas.principal.ACL
        this.subject.getPrincipals().add(principalList);
    }

    protected void setACLForUser(PrincipalCollection principalList, User user, SecuritySupport securitySupport) {
        Collection<ACL> principals = securitySupport.getUserManager(((MgnlUser) user).getRealm()).getACLs(user).values();
        mergePrincipals(principalList, principals);
    }

    private void mergePrincipals(PrincipalCollection principalList, Collection<ACL> principals) {
        for (ACL princ : principals) {
            if (principalList.contains(princ.getName())) {
                ACL oldACL = (ACL) principalList.get(princ.getName());
                Collection<Permission> permissions = new HashSet<Permission>(oldACL.getList());
                permissions.addAll(princ.getList());
                principalList.remove(oldACL);
                princ = new ACLImpl(princ.getName(), princ.getRepository(), princ.getWorkspace(), new ArrayList<Permission>(permissions));
            }
            principalList.add(princ);
        }
    }

    // do nothing here, we are only responsible for adding ACL passed on via shared state
    @Override
    public void setEntity() {}

    /**
     * Sets the list of groups, <code>info.magnolia.jaas.principal.GroupList</code>.
     * @param groups array of group names
     */
    protected void addGroups(String[] groups) {
        GroupList groupList = new GroupListImpl();
        for (Iterator<String> iterator = getGroupNames().iterator(); iterator.hasNext();) {
            String group = iterator.next();
            groupList.add(group);
        }
        this.subject.getPrincipals().add(groupList);
    }

    /**
     * Sets the list of roles, <code>info.magnolia.jaas.principal.RoleList</code>.
     * @param roles array of role names
     */
    protected void addRoles(String[] roles) {
        RoleList roleList = new RoleListImpl();
        for (Iterator<String> iterator = getRoleNames().iterator(); iterator.hasNext();) {
            String role = iterator.next();
            roleList.add(role);
        }
        this.subject.getPrincipals().add(roleList);
    }

    /**
     * Looks for role configured in magnolia repository with the given name, and configures ACLs for it.
     * @param roles array of role names.
     * @param principalList PrincipalCollection
     */
    protected void setACLForRoles(String[] roles, PrincipalCollection principalList) {
        SecuritySupport securitySupport = SecuritySupport.Factory.getInstance();
        for (String role : roles) {
            mergePrincipals(principalList, securitySupport.getRoleManager().getACLs(role).values());
        }
    }

    /**
     * Looks for groups configured in magnolia repository with the given name, and configures ACLs for it.
     * @param groups array of group names.
     * @param principalList PrincipalCollection
     */
    protected void setACLForGroups(String[] groups, PrincipalCollection principalList) {
        SecuritySupport securitySupport = SecuritySupport.Factory.getInstance();

        for (String group : groups) {
            mergePrincipals(principalList, securitySupport.getGroupManager().getACLs(group).values());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean release() {
        return true;
    }
}
