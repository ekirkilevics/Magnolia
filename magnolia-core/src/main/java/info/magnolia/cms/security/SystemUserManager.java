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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.context.MgnlContext;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.commons.lang.StringUtils;
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
                log.debug("Anonymous user reloaded");
            }

        };

        final String anonymousUserPath = "/" + Realm.REALM_SYSTEM.getName() + "/" + UserManager.ANONYMOUS_USER;
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

        ObservationUtil.registerDeferredChangeListener(
                ContentRepository.USER_ROLES,
                "/",
                true,
                "mgnl:role",
                anonymousListener,
                1000,
                5000);
    }

    @Override
    public String getRealmName() {
        String name = super.getRealmName();
        // attempt to fix: MAGNOLIA-1839
        if (StringUtils.isEmpty(name)) {
            log.error("realm of system user manager is not set!");
            return Realm.REALM_SYSTEM.getName();
        }
        return name;
    }

    @Override
    public User getSystemUser() {
        return getOrCreateUser(UserManager.SYSTEM_USER, UserManager.SYSTEM_PSWD);
    }

    @Override
    public User getAnonymousUser() {
        if (anonymousUser == null) {
            // see MAGNOLIA-2029
            anonymousUser = getRequiredSystemUser(UserManager.ANONYMOUS_USER, UserManager.ANONYMOUS_USER);
        }
        return anonymousUser;
    }

    /**
     * Load a system user from the repository, but don't try to create it if missing.
     */
    private User getRequiredSystemUser(final String username, String password) {
        return MgnlContext.doInSystemContext(new SilentSessionOp<User>(getRepositoryName()) {

            @Override
            public User doExec(Session session) throws RepositoryException {
                User user = null;
                Node node;
                try {
                    node = session.getNode("/" + Realm.REALM_SYSTEM.getName() + "/" + username);
                }
                catch (RepositoryException e) {
                    log.error("Error caught while loading the system user "
                            + username
                            + ": "
                            + e.getClass().getName()
                            + ": "
                            + e.getMessage(), e);
                    return null;
                }
                if (node == null) {
                    log.error("User not found: {}.", username);
                    return null;
                }

                user = newUserInstance(node);
                return user;
            }});
    }

    protected User getOrCreateUser(String userName, String password) {
        User user = getUser(userName);
        if (user == null) {
            log.error(
                    "Failed to get system user [{}], will try to create new system user with default password",
                    userName);
            user = this.createUser(userName, password);
        }
        return user;
    }
}
