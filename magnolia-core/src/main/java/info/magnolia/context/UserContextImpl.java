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
package info.magnolia.context;

import info.magnolia.cms.security.PrincipalUtil;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;

import java.util.Locale;

import javax.security.auth.Subject;

import org.apache.commons.lang.LocaleUtils;

/**
 * User aware context implementation able to release and discard all kept info upon user logout. Also changes the locale
 * from default one to the one preferred by the logged in user. Sets the Subject for the logged in user in session for
 * subsequent requests to pick up. When no user has logged in this class returns the anonymous user.
 *
 * @version $Id$
 */
public class UserContextImpl extends AbstractContext implements UserContext {

    private static final long serialVersionUID = 222L;

    private static final String SESSION_SUBJECT = Subject.class.getName();

    private User user;
    private Subject subject;

    public UserContextImpl() {

    }

    @Override
    public Locale getLocale() {
        if (locale == null) {
            setLocaleFor(getUser());
        }
        return locale;
    }

    @Override
    public User getUser() {
        if (user != null) {
            return user;
        }

        user = PrincipalUtil.findPrincipal(getSubject(), User.class);
        if (user == null) {
            throw new IllegalStateException("Subject must have a info.magnolia.cms.security.User principal.");
        }
        return user;
    }

    @Override
    public Subject getSubject() {
        if (subject != null) {
            return this.subject;
        }

        // were we logged in by a previous request?
        subject = (Subject) getAttribute(SESSION_SUBJECT, Context.SESSION_SCOPE);
        if (subject != null) {
            return this.subject;
        }

        // default to anonymous user
        login(Security.getAnonymousSubject());
        return subject;
    }

    @Override
    public void login(Subject subject) {
        User user = PrincipalUtil.findPrincipal(subject, User.class);
        if (user == null) {
            throw new IllegalArgumentException("When logging in the Subject must have a info.magnolia.cms.security.User principal.");
        }
        this.subject = subject;
        this.user = user;
        setLocaleFor(user);
        // set the subject in session for subsequent requests
        if (!user.getName().equals(UserManager.ANONYMOUS_USER)) {
            setAttribute(SESSION_SUBJECT, subject, Context.SESSION_SCOPE);
        }
    }

    @Override
    public void logout() {
        subject = null;
        user = null;
        locale = null;
        removeAttribute(SESSION_SUBJECT, Context.SESSION_SCOPE);
    }

    protected void setLocaleFor(User user) {
        // despite this property being called "language", it sometimes contains the country (i.e. fr_CH), so we need to parse it
        final String userLanguage = user.getLanguage();
        setLocale(LocaleUtils.toLocale(userLanguage));
    }

}
