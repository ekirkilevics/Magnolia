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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.Subject;

import info.magnolia.cms.security.auth.PrincipalCollectionImpl;

/**
 * Get the current role or user manager.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class Security {

    /**
     * Returns the configured RoleManager.
     */
    public static RoleManager getRoleManager() {
        return getSecuritySupport().getRoleManager();
    }

    /**
     * Returns the configured UserManager.
     */
    public static UserManager getUserManager() {
        return getSecuritySupport().getUserManager();
    }

    /**
     * Returns the configured GroupManager.
     */
    public static GroupManager getGroupManager() {
        return getSecuritySupport().getGroupManager();
    }

    public static SecuritySupport getSecuritySupport() {
        return SecuritySupport.Factory.getInstance();
    }

    public static User getAnonymousUser() {
        return getSecuritySupport().getUserManager(Realm.REALM_SYSTEM.getName()).getAnonymousUser();
    }

    public static User getSystemUser() {
        return getSecuritySupport().getUserManager(Realm.REALM_SYSTEM.getName()).getSystemUser();
    }

    public static Subject getSystemSubject() {
        return createSubjectAndPopulate(Security.getSystemUser());
    }

    public static Subject getAnonymousSubject() {
        return createSubjectAndPopulate(Security.getAnonymousUser());
    }

    private static Subject createSubjectAndPopulate(User user) {

        RoleManager roleManager = getRoleManager();

        List<Principal> acls = new ArrayList<Principal>();
        for (String role : user.getAllRoles()) {
            acls.addAll(roleManager.getACLs(role).values());
        }

        PrincipalCollectionImpl principalCollection = new PrincipalCollectionImpl();
        principalCollection.addAll(acls);

        Subject subject = new Subject();
        subject.getPrincipals().add(user);
        subject.getPrincipals().add(principalCollection);
        return subject;
    }

}
