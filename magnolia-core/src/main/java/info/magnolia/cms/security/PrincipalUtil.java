/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.security;

import java.security.Principal;
import java.util.Iterator;
import javax.security.auth.Subject;

import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.security.auth.PrincipalCollection;

/**
 * Utility methods for handling JAAS principals.
 *
 * @version $Id$
 */
public class PrincipalUtil {

    public static Subject createSubject(User user) {
        Subject subject = new Subject();
        subject.getPrincipals().add(user);
        return subject;
    }

    public static <T extends Principal> T findPrincipal(Iterable<Principal> principals, Class<T> clazz) {
        for (Principal principal : principals) {
            if (clazz.isAssignableFrom(principal.getClass())) {
                return (T) principal;
            }
            if (principal instanceof PrincipalCollection) {
                T t = findPrincipal((PrincipalCollection) principal, clazz);
                if (t != null) {
                    return t;
                }
            }
        }
        return null;
    }

    public static <T extends Principal> T findPrincipal(Subject subject, Class<T> clazz) {
        return findPrincipal(subject.getPrincipals(), clazz);
    }

    public static <T extends Principal> T removePrincipal(Iterable<Principal> principals, Class<T> clazz) {
        for (Iterator<Principal> iterator = principals.iterator(); iterator.hasNext(); ) {
            Principal principal = iterator.next();
            if (clazz.isAssignableFrom(principal.getClass())) {
                iterator.remove();
                return (T) principal;
            }
            if (principal instanceof PrincipalCollection) {
                T t = removePrincipal((PrincipalCollection) principal, clazz);
                if (t != null) {
                    return t;
                }
            }
        }
        return null;
    }

    public static <T extends Principal> T removePrincipal(Subject subject, Class<T> clazz) {
        return removePrincipal(subject.getPrincipals(), clazz);
    }

    /**
     * Extracts magnolia user from the list of principals.
     *
     * @param subject
     * @return
     */
    public static User extractUser(Subject subject) {
        return findPrincipal(subject.getPrincipals(), User.class);
    }

    public static ACL findAccessControlList(Iterable<Principal> principals, String name) {
        for (Principal principal : principals) {
            if (principal instanceof ACL) {
                ACL acl = (ACL) principal;
                if (name.equals(acl.getWorkspace())) {
                    return acl;
                }
            }
            if (principal instanceof PrincipalCollection) {
                ACL acl = findAccessControlList((PrincipalCollection) principal, name);
                if (acl != null) {
                    return acl;
                }
            }
        }

        return null;
    }

    public static ACL findAccessControlList(Subject subject, String name) {
        return findAccessControlList(subject.getPrincipals(), name);
    }
}
