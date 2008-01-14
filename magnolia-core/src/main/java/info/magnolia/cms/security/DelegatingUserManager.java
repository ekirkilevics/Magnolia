/**
 * This file Copyright (c) 2003-2008 Magnolia International
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

import javax.security.auth.Subject;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DelegatingUserManager implements UserManager {
    private final Map delegates;

    /**
     * @param delegates <String realm, UserManager delegate>
     */
    public DelegatingUserManager(Map delegates) {
        this.delegates = delegates;
    }

    public User createUser(final String name, final String pw) throws UnsupportedOperationException {
        final Op op = new Op() {
            public Object delegate(UserManager um) {
                return um.createUser(name, pw);
            }
        };
        return (User) delegateUntilSupported(op);
    }

    public void changePassword(User user, String newPassword) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Please use a specific instance of UserManager to do this.");
    }

    public User getAnonymousUser() {
        return (User) delegateUntilSupported(new Op() {
            public Object delegate(UserManager um) {
                return um.getAnonymousUser();
            }
        });
    }

    public User getSystemUser() {
        return (User) delegateUntilSupported(new Op() {
            public Object delegate(UserManager um) {
                return um.getSystemUser();
            }
        });
    }

    public User getUser(final String name) throws UnsupportedOperationException {
        return (User) delegateUntilNotNull(new Op() {
            public Object delegate(UserManager um) {
                return um.getUser(name);
            }
        });
    }

    public User getUser(final Subject subject) throws UnsupportedOperationException {
        return (User) delegateUntilNotNull(new Op() {
            public Object delegate(UserManager um) {
                return um.getUser(subject);
            }
        });
    }

    // TODO : this should maybe aggregate results, but ExternalUserManager throws an UnsupportedOperationException
    // TODO : also not that this is seemingly never used (or maybe through reflection or other ide-search unfriendly mechanisms)
    public Collection getAllUsers() throws UnsupportedOperationException {
        return (Collection) delegateUntilSupported(new Op() {
            public Object delegate(UserManager um) {
                return um.getAllUsers();
            }
        });
    }

    private Object delegateUntilSupported(Op op) {
        final Iterator it = delegates.keySet().iterator();
        while (it.hasNext()) {
            final String realmName = (String) it.next();
            final UserManager um = (UserManager) delegates.get(realmName);
            try {
                return op.delegate(um);
            } catch (UnsupportedOperationException e) {
                // try the next delegate
            }
        }
        throw new UnsupportedOperationException("None of the delegate UserManager supports this operation.");
    }

    private Object delegateUntilNotNull(Op op) {
        final Iterator it = delegates.keySet().iterator();
        while (it.hasNext()) {
            final String realmName = (String) it.next();
            final UserManager um = (UserManager) delegates.get(realmName);
            final Object result = op.delegate(um);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private interface Op {
        Object delegate(UserManager um);
    }
}
