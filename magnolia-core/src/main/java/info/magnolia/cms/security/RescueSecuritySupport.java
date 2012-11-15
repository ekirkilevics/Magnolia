/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.security;

import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.login.LoginResult;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * To be used as a replacement of /server/security or SecuritySupportImpl in mgnl-beans.properties
 * in case the configuration is messed up. For instance, edit
 * <code>WEB-INF/config/default/magnolia.properties</code> and add
 * <pre>info.magnolia.cms.security.SecuritySupport=info.magnolia.cms.security.RescueSecuritySupport</pre>
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RescueSecuritySupport extends SecuritySupportBase {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RescueSecuritySupport.class);
    public RescueSecuritySupport() {
        super();
        log.warn("Using RescueSecuritySupport !");
    }

    @Override
    public UserManager getUserManager() {
        log.warn("Using RescueSecuritySupport, will instantiate RescueUserManager, please fix your configuration !");
        SystemUserManager userManager = new RescueUserManager();
        userManager.setRealmName(Realm.REALM_SYSTEM.getName());
        return userManager;
    }

    @Override
    public UserManager getUserManager(String realmName) {
        log.warn("Using RescueSecuritySupport, will instantiate RescueUserManager, please fix your configuration !");
        SystemUserManager userManager = new RescueUserManager();
        userManager.setRealmName(realmName);
        return userManager;
    }

    @Override
    public GroupManager getGroupManager() {
        log.warn("Using RescueSecuritySupport, will instantiate MgnlGroupManager, please fix your configuration !");
        return new MgnlGroupManager();
    }

    @Override
    public RoleManager getRoleManager() {
        log.warn("Using RescueSecuritySupport, will instantiate MgnlRoleManager, please fix your configuration !");
        return new MgnlRoleManager();
    }

    @Override
    public LoginResult authenticate(CredentialsCallbackHandler callbackHandler, String customLoginModule) {
        log.warn("Using RescueSecuritySupport, will force authentication with a fake system user, please fix your configuration !");
        try {
            LoginContext loginContext = createLoginContext(callbackHandler, customLoginModule);
            loginContext.login();

            Subject subject = loginContext.getSubject();
            User user = new RescueUser(UserManager.SYSTEM_USER, UserManager.SYSTEM_PSWD);
            replaceUserPrincipal(subject, user);
            return new LoginResult(LoginResult.STATUS_SUCCEEDED, subject);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }
    }

    private void replaceUserPrincipal(Subject subject, User user) {
        Iterator<Principal> iterator = subject.getPrincipals().iterator();
        while (iterator.hasNext()) {
            Principal next = iterator.next();
            if (next instanceof User) {
                iterator.remove();
                subject.getPrincipals().add(user);
                break;
            }
        }
    }

    /**
     * TODO: extract as top level class? Currently this class is tested implicitly by {@link RescueSecuritySupportTest}. Should this implement directly UserManager and throw UnsupportedMethodException for the methods not implemented?
     * <p>Overrides {@link SystemUserManager#getSystemUser()}, {@link SystemUserManager#getAnonymousUser()} and {@link SystemUserManager#getUser(String)}. All methods return an instance of {@link RescueUser}.
     * @version $Id$
     */
    protected class RescueUserManager extends SystemUserManager {

        @Override
        public User getSystemUser() {
            return new RescueUser(SYSTEM_USER, SYSTEM_PSWD);
        }

        @Override
        public User getAnonymousUser() {
            return new RescueUser(ANONYMOUS_USER, "");
        }
        @Override
        public User getUser(String name) {
            if(SYSTEM_USER.equals(name)){
                return new RescueUser(SYSTEM_USER, SYSTEM_PSWD);
            }
            return new RescueUser(ANONYMOUS_USER, "");
        }
    }

    /**
     * TODO extract as top level class? Currently this class is tested implicitly by {@link RescueSecuritySupportTest}.<p>
     * A <em>"fake"</em> user, that is a user who is created in-memory rather than relying on a working <em>users</em> repository,
     * as the latter may be corrupted and in need of being fixed.
     * <p>See <a href='http://jira.magnolia-cms.com/browse/MAGNOLIA-3561'>MAGNOLIA-3561</a>.
     * @version $Id$
     */
    protected class RescueUser implements User {
        private static final long serialVersionUID = 1L;

        private final String name;

        private final String password;

        private final Collection<String> groups = new ArrayList<String>();

        private final Collection<String> roles = new ArrayList<String>();

        public RescueUser(String name, String password) {
            this.name = name;
            this.password = password;

            if(UserManager.SYSTEM_USER.equals(name)){
                groups.add("publishers");

                roles.add("superuser");
                roles.add("workflow-base");
            }
        }

        @Override
        public boolean hasRole(String roleName) {
            return roles.contains(roleName);
        }

        @Override
        public void removeRole(String roleName) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addRole(String roleName) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean inGroup(String groupName) {
            return groups.contains(groupName);
        }

        @Override
        public void removeGroup(String groupName) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addGroup(String groupName) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void setEnabled(boolean enabled) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getLanguage() {
            return "en";
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getProperty(String propertyName) {
            return null;
        }

        @Override
        public void setProperty(String propertyName, String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getIdentifier() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> getGroups() {
            return Collections.unmodifiableCollection(groups);
        }

        @Override
        public Collection<String> getAllGroups() {
            return Collections.unmodifiableCollection(groups);
        }

        @Override
        public Collection<String> getRoles() {
            return Collections.unmodifiableCollection(roles);
        }

        @Override
        public Collection<String> getAllRoles() {
            return Collections.unmodifiableCollection(roles);
        }
    }
}
