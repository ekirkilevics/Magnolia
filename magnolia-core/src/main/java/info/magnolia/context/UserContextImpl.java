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

import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;

import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;

/**
 * User aware context implementation able to release and discard all kept info upon user logout. Also changes the locale from default one to the one prefered by the logged in user.
 * @author had
 * @version $Id: $
 */
public class UserContextImpl extends AbstractContext implements UserContext {

    private static final long serialVersionUID = 222L;

    private static final String SESSION_USER = UserContextImpl.class.getName() + ".user";

    private User user;

    public UserContextImpl() {

    }

    @Override
    public Locale getLocale() {
        if(this.locale == null){
            setLocaleFor(getUser());
        }
        return locale;
    }

    /**
     * Create the subject on demand.
     * @see info.magnolia.context.AbstractContext#getUser()
     */
    @Override
    public User getUser() {
        //TODO: is this correct? such a user will stay set even when session attribute expires
        if (user == null) {
            user = (User) getAttribute(SESSION_USER, Context.SESSION_SCOPE);
            if (user == null) {
                user = Security.getAnonymousUser();
            }
        }
        return this.user;
    }

    @Override
    public void login(User user) {
        setLocaleFor(user);
        if(!user.getName().equals(Security.getAnonymousUser().getName())){
            setAttribute(SESSION_USER, user, Context.SESSION_SCOPE);
        }
        this.user = null;
    }

    @Override
    public void logout() {
        removeAttribute(SESSION_USER, Context.SESSION_SCOPE);
        user = null;
        locale = null;
        login(Security.getAnonymousUser());
    }

    protected void setLocaleFor(User user) {
        // despite this property being called "language", it sometimes contains the country (i.e. fr_CH), so we need to parse it
        final String userLanguage = user.getLanguage();
        setLocale(LocaleUtils.toLocale(userLanguage));
    }

}
