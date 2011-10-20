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
package info.magnolia.jaas.sp.jcr;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.jackrabbit.core.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.security.PrincipalUtil;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

/**
 * Login module for internal Jackrabbit authentication, validates the JackRabbit 'admin' user and uses the Subject
 * provided by the magnolia context.
 *
 * Note that Jackrabbit requires the login module to be serializable.
 *
 * @version $Id$
 */
public class JackrabbitAuthenticationModule implements LoginModule, Serializable {

    private static final Logger log = LoggerFactory.getLogger(JackrabbitAuthenticationModule.class);

    private Subject subject;
    private CallbackHandler callbackHandler;
    private String name;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {

        if (this.callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available");
        }

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("name");
        callbacks[1] = new PasswordCallback("pswd", false);

        char[] password;
        try {
            this.callbackHandler.handle(callbacks);
            this.name = ((NameCallback) callbacks[0]).getName();
            password = ((PasswordCallback) callbacks[1]).getPassword();
        } catch (IOException ioe) {
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException ce) {
            throw new LoginException(ce.getCallback().toString() + " not available");
        }

        // When we log in to register workspaces and node types we do it as 'admin', we do this in SystemContext but we
        // can't use the context here because it's bound to the system user which is configured in the repository and
        // attempting to access it would fail. More specifically calling MgnlContext.getSubject() fails as a result of
        // trying to use SecuritySupport.
        if (ContentRepository.REPOSITORY_USER.equals(this.name)) {
            if (!Arrays.equals(password, ContentRepository.REPOSITORY_PSWD.toCharArray())) {
                throw new FailedLoginException();
            }
            compileAdminPrincipals();
            return true;
        }

        Context context = MgnlContext.hasInstance() ? MgnlContext.getInstance() : null;
        if (context == null) {
            throw new FailedLoginException("Cannot login, magnolia context is not set");
        }

        Subject magnoliaSubject = context.getSubject();
        if (magnoliaSubject == null) {
            throw new FailedLoginException("Cannot login, invalid setup or deserialization error");
        }

        if (isSuperuser(magnoliaSubject)) {
            compileAdminPrincipals();
            return true;
        }

        compileUserPrincipals(magnoliaSubject);
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        callbackHandler = null;
        name = null;
        return true;
    }

    private void compileUserPrincipals(Subject magnoliaSubject) {
        subject.getPrincipals().addAll(magnoliaSubject.getPrincipals());
        subject.getPrincipals().add(new UserPrincipal(name));
    }

    private void compileAdminPrincipals() {
        this.subject.getPrincipals().add(new MagnoliaJRAdminPrincipal(ContentRepository.REPOSITORY_USER));
    }

    /**
     * Returns true if the subject has a principal that represents the magnolia superuser.
     */
    private boolean isSuperuser(Subject magnoliaSubject) {
        User user = PrincipalUtil.findPrincipal(magnoliaSubject, User.class);
        return user != null && UserManager.SYSTEM_USER.equals(user.getName());
    }
}
