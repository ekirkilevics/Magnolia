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
package info.magnolia.cms.security;

import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.security.auth.GroupList;
import info.magnolia.cms.security.auth.RoleList;
import info.magnolia.context.MgnlContext;

import java.util.Collection;
import java.util.Map;

import javax.jcr.Value;
import javax.security.auth.Subject;

/**
 * Manages the JAAS users.
 * @author philipp
 * @version $Revision:9391 $ ($Author:scharles $)
 */
public class ExternalUserManager implements UserManager {

    @Override
    public User getUser(String name) throws UnsupportedOperationException {
        // we only support accessing current User object
        // - implement source specific UserManager if needed
        if (name.equalsIgnoreCase(MgnlContext.getUser().getName())) {
            return MgnlContext.getUser();
        }
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public User getUserById(final Object id) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Initialize new user using JAAS authenticated/authorized subject.
     * @param subject
     * @throws UnsupportedOperationException
     */
    @Override
    public User getUser(Subject subject) throws UnsupportedOperationException {
        return new ExternalUser(subject);
    }

    public User getUser(Map<String, String> properties, GroupList groupList, RoleList roleList){
        return new ExternalUser(properties, groupList, roleList);
    }

    @Override
    public Collection<User> getAllUsers() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public User createUser(String name, String pw) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public User changePassword(User user, String newPassword) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * SystemUserManager does this.
     */
    @Override
    public User getSystemUser() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * SystemUserManager does this.
     */
    @Override
    public User getAnonymousUser() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateLastAccessTimestamp(User user) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasAny(String name, String roleName, String nodeRoles) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public Map<String,ACL> getACLs(User user) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public User addRole(User user, String roleName) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public User addGroup(User user, String groupName) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public int getLockTimePeriod() {
        throw new UnsupportedOperationException("Not supported by this user manager.");
    }

    @Override
    public int getMaxFailedLoginAttempts() {
        throw new UnsupportedOperationException("Not supported by this user manager.");
    }

    @Override
    public void setLockTimePeriod(int lockTimePeriod){
        throw new UnsupportedOperationException("Not supported by this user manager.");
    }

    @Override
    public void setMaxFailedLoginAttempts(int maxFailedLoginAttempts){
        throw new UnsupportedOperationException("Not supported by this user manager.");
    }

    @Override
    public User setProperty(User user, String propertyName, Value propertyValue) {
        throw new UnsupportedOperationException("Not supported by this user manager.");
    }

    @Override
    public User removeGroup(User user, String groupName) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public User removeRole(User user, String roleName) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
