/**
 * This file Copyright (c) 2003-2007 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.callback.PlainTextCallbackHandler;
import info.magnolia.cms.util.ObservationUtil;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible to handle system users like anonymous and superuser.
 * @author philipp
 * @version $Id$
 */
public class SystemUserManager extends MgnlUserManager {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SystemUserManager.class);

    /**
     * kept as static for performance reasons on live instance. reinitialized on any modification event on anonymous
     * role
     */
    private User anonymousUser;

    public SystemUserManager() {

        EventListener anonymousListener = new EventListener() {

            public void onEvent(EventIterator events) {
                anonymousUser = null;
                log.info("Anonymous user reloaded");
            }

        };

        final String anonymousUserPath = "/" + Realm.REALM_SYSTEM + "/" + UserManager.ANONYMOUS_USER;
        ObservationUtil.registerChangeListener(
            ContentRepository.USERS,
            anonymousUserPath,
            true,
            "mgnl:user",
            anonymousListener);

        ObservationUtil.registerChangeListener(
            ContentRepository.USER_GROUPS,
            "/",
            true,
            "mgnl:group",
            anonymousListener);

        ObservationUtil.registerDefferedChangeListener(
            ContentRepository.USER_ROLES,
            "/",
            true,
            "mgnl:role",
            anonymousListener,
            1000,
            5000);
    }

    public User getSystemUser() {
        return getOrCreateUser(UserManager.SYSTEM_USER, UserManager.SYSTEM_PSWD);
    }

    public User getAnonymousUser() {
        if(anonymousUser == null){
            anonymousUser = getOrCreateUser(UserManager.ANONYMOUS_USER, "");
            Subject subject = getSubject(UserManager.ANONYMOUS_USER, "");
            anonymousUser.setSubject(subject);
        }
        return anonymousUser;
    }

    protected User getOrCreateUser(String userName, String password) {
        User user = getUser(userName);
        if (user == null) {
            log.error("Failed to get system or anonymous user [{}], will try to create new system user with default password", userName);
            user = this.createUser(userName, password);
        }
        return user;
    }
    
    protected Subject getAnonymousSubject() {
    	return getSubject(getAnonymousUser().getName(), getAnonymousUser().getPassword());        
    }

    private Subject getSubject(String userName, String password) {
    	CredentialsCallbackHandler callbackHandler = new PlainTextCallbackHandler(
            userName, password.toCharArray(), Realm.REALM_SYSTEM);
        try {
            LoginContext loginContext = new LoginContext("magnolia", callbackHandler);
            loginContext.login();
            return loginContext.getSubject();
        }
        catch (LoginException le) {
            log.error("Failed to login as anonymous user", le);
        }
        return null;
    }
}
