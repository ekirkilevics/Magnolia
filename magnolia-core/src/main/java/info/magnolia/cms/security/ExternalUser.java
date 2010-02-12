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
package info.magnolia.cms.security;

import info.magnolia.cms.security.auth.Entity;
import info.magnolia.cms.security.auth.GroupList;
import info.magnolia.cms.security.auth.RoleList;
import info.magnolia.context.MgnlContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.io.Serializable;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author philipp
 * @author Sameer Charles
 * @version $Revision:2558 $ ($Author:scharles $)
 */
public class ExternalUser extends AbstractUser implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(ExternalUser.class);

    /**
     * user properties
     */
    private Entity userDetails;

    /**
     * user roles
     */
    private RoleList roleList;

    /**
     * user groups
     */
    private GroupList groupList;

    /**
     * @param subject as created by login module
     */
    protected ExternalUser(Subject subject) {
        final Set<Entity> principalDetails = subject.getPrincipals(Entity.class);
        final Iterator<Entity> entityIterator = principalDetails.iterator();
        this.userDetails = entityIterator.next();

        final Set<RoleList> principalRoles = subject.getPrincipals(RoleList.class);
        final Iterator<RoleList> roleListIterator = principalRoles.iterator();
        this.roleList = roleListIterator.next();

        final Set<GroupList> principalGroups = subject.getPrincipals(GroupList.class);
        final Iterator<GroupList> groupListIterator = principalGroups.iterator();
        this.groupList = groupListIterator.next();
    }

    public boolean hasRole(String roleName) {
        return this.roleList.has(roleName);
    }

    public void removeRole(String roleName) {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    public void addRole(String roleName) {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    /**
     * Is this user in a specified group?
     * @return true if in group
     */
    public boolean inGroup(String groupName) {
        return this.groupList.has(groupName);
    }

    /**
     * Remove a group. Implementation is optional
     */
    public void removeGroup(String groupName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    /**
     * Adds this user to a group. Implementation is optional
     */
    public void addGroup(String groupName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    public boolean isEnabled() {
        return true;
    }

    public void setEnabled(boolean enabled) {
    }

    public String getLanguage() {
        String language = (String) this.userDetails.getProperty(Entity.LANGUAGE);
        if (null == language) {
            language = MgnlContext.getSystemContext().getLocale().getLanguage();
        }
        return language;
    }

    public String getName() {
        return (String) this.userDetails.getProperty(Entity.NAME);
    }

    /**
     * get user password
     * @return password string
     */
    public String getPassword() {
        return (String) this.userDetails.getProperty(Entity.PASSWORD);
    }

    public String getProperty(String propertyName) {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    public void setProperty(String propertyName, String value) {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    public Collection<String> getGroups() {
        return this.groupList.getList();
    }

    public Collection<String> getAllGroups() {
        return this.getGroups();
    }

    public Collection<String> getRoles() {
        return this.roleList.getList();
    }

    public Collection<String> getAllRoles() {
        return this.getRoles();
    }
}
