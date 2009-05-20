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
package info.magnolia.context;

import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.LocaleUtils;

public class UserContextImpl extends AbstractContext implements UserContext {
    private static final Logger log = LoggerFactory.getLogger(UserContextImpl.class);

    private static final long serialVersionUID = 222L;

    private static final String SESSION_USER = UserContextImpl.class.getName() + ".user";

    private User user;

    public UserContextImpl() {

    }

    public Locale getLocale() {
        if(this.locale == null){
            // despite this being called "language", it sometimes contains the country (i.e. fr_CH), so we need to parse it
            final String userLanguage = getUser().getLanguage();
            this.locale = LocaleUtils.toLocale(userLanguage);
        }
        return locale;
    }

    /**
     * Create the subject on demand.
     * @see info.magnolia.context.AbstractContext#getUser()
     */
    public User getUser() {
        if (user == null) {
            user = (User) getAttribute(SESSION_USER, Context.SESSION_SCOPE);
            if (user == null) {
                user = Security.getAnonymousUser();
            }
        }
        return this.user;
    }

    public void login(User user) {
        setLocale(new Locale(user.getLanguage()));
        setAttribute(SESSION_USER, user, Context.SESSION_SCOPE);
        this.user = null;
    }

    public void logout() {
        removeAttribute(SESSION_USER, Context.SESSION_SCOPE);
        user = null;
        locale = null;
        login(Security.getAnonymousUser());
    }
}
