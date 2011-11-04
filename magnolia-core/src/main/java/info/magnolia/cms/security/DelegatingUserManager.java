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

import java.util.Collection;
import java.util.Map;

import javax.security.auth.Subject;


/**
 * A {@link UserManager} delegating to a set of user managers. The first user manager which does not
 * through an {@link UnsupportedOperationException} will be used.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DelegatingUserManager implements UserManager {
    private final Map<String, UserManager> delegates;

    /**
     * @param delegates <String realm, UserManager delegate>
     */
    public DelegatingUserManager(Map<String, UserManager> delegates) {
        this.delegates = delegates;
    }

    @Override
    public User createUser(final String name, final String pw) throws UnsupportedOperationException {
        final Op<User> op = new Op<User>() {
            @Override
            public User delegate(UserManager um) {
                return um.createUser(name, pw);
            }
        };
        return delegateUntilSupported(op);
    }

    @Override
    public User changePassword(User user, String newPassword) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Please use a specific instance of UserManager to do this.");
    }

    @Override
    public User getAnonymousUser() {
        return delegateUntilSupported(new Op<User>() {
            @Override
            public User delegate(UserManager um) {
                return um.getAnonymousUser();
            }
        });
    }

    @Override
    public User getSystemUser() {
        return delegateUntilSupported(new Op<User>() {
            @Override
            public User delegate(UserManager um) {
                return um.getSystemUser();
            }
        });
    }

    @Override
    public User getUser(final String name) throws UnsupportedOperationException {
        return delegateUntilNotNull(new Op<User>() {
            @Override
            public User delegate(UserManager um) {
                return um.getUser(name);
            }
        });
    }

    @Override
    public User getUser(final Subject subject) throws UnsupportedOperationException {
        return delegateUntilNotNull(new Op<User>() {
            @Override
            public User delegate(UserManager um) {
                return um.getUser(subject);
            }
        });
    }

    // TODO : this should maybe aggregate results, but ExternalUserManager throws an UnsupportedOperationException
    // TODO : also not that this is seemingly never used (or maybe through reflection or other ide-search unfriendly mechanisms)
    @Override
    public Collection<User> getAllUsers() throws UnsupportedOperationException {
        return delegateUntilSupported(new Op<Collection<User>>() {
            @Override
            public Collection<User> delegate(UserManager um) {
                return um.getAllUsers();
            }
        });
    }

    @Override
    public void updateLastAccessTimestamp(final User user) {
        delegateUntilSupported(new Op<Void>() {
            @Override
            public Void delegate(UserManager um) {
                um.updateLastAccessTimestamp(user);
                return null;
            }
        });
    }

    private <RT> RT delegateUntilSupported(Op<RT> op) {
        for (String realmName : delegates.keySet()) {
            final UserManager um = delegates.get(realmName);
            try {
                return op.delegate(um);
            } catch (UnsupportedOperationException e) {
                // try the next delegate
            }
        }
        throw new UnsupportedOperationException("None of the delegate UserManager supports this operation.");
    }

    private <RT> RT delegateUntilNotNull(Op<RT> op) {
        for (String realmName : delegates.keySet()) {
            final UserManager um = delegates.get(realmName);
            final RT result = op.delegate(um);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private interface Op<RT> {
        RT delegate(UserManager um);
    }

    @Override
    public boolean hasAny(final String principal, final String resourceName, final String resourceTypeName) {
        return delegateUntilSupported(new Op<Boolean>() {
            @Override
            public Boolean delegate(UserManager um) {
                return um.hasAny(principal, resourceName, resourceTypeName);
            }
        });
    }

    @Override
    public Map<String,ACL> getACLs(final User user) {
        return delegateUntilSupported(new Op<Map<String,ACL>>() {
            @Override
            public Map<String,ACL> delegate(UserManager um) {
                return um.getACLs(user);
            }
        });
    }

    @Override
    public User addRole(final User user, final String roleName) {
        return delegateUntilSupported(new Op<User>() {
            @Override
            public User delegate(UserManager um) {
                return um.addRole(user, roleName);
            }
        });
    }

    @Override
    public User addGroup(final User user, final String groupName) {
        return delegateUntilSupported(new Op<User>() {
            @Override
            public User delegate(UserManager um) {
                return um.addGroup(user, groupName);
            }
        });
    }
}
