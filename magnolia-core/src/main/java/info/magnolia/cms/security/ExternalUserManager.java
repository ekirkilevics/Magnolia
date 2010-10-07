/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import info.magnolia.context.MgnlContext;

import java.util.Collection;

import javax.security.auth.Subject;

/**
 * Manages the JAAS users.
 * @author philipp
 * @version $Revision:9391 $ ($Author:scharles $)
 */
public class ExternalUserManager implements UserManager {

    public User getUser(String name) throws UnsupportedOperationException {
        // we only support accessing current User object
        // - implement source specific UserManager if needed
        if (name.equalsIgnoreCase(MgnlContext.getUser().getName())) {
            return MgnlContext.getUser();
        }
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Initialize new user using JAAS authenticated/authorized subject.
     * @param subject
     * @throws UnsupportedOperationException
     */
    public User getUser(Subject subject) throws UnsupportedOperationException {
        return new ExternalUser(subject);
    }

    public Collection<User> getAllUsers() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public User createUser(String name, String pw) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public void changePassword(User user, String newPassword) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * SystemUserManager does this.
     */
    public User getSystemUser() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * SystemUserManager does this.
     */
    public User getAnonymousUser() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
