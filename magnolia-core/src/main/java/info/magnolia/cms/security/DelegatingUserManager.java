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
        return null;
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
