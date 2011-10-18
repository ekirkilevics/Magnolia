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

import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Login module for internal JR authentication - piggybacks on existing Magnolia authentication.
 */
// JR requires login module to be serializable!
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

        try {
            this.callbackHandler.handle(callbacks);
            this.name = ((NameCallback) callbacks[0]).getName();
        } catch (IOException ioe) {
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException ce) {
            throw new LoginException(ce.getCallback().toString() + " not available");
        }

        if (!MgnlContext.hasInstance() || (MgnlContext.getInstance() instanceof SystemContext)) {
            if (isAdmin()) {
                this.subject.getPrincipals().add(new MagnoliaJRAdminPrincipal("admin"));
                log.debug("logged in as admin ... repo init or authentication check");
                return true;
            }
            // TODO: initialization, workflow, scheduled jobs etc ... we need to either delegate to our chain or replay same things here
            return false;
        }

        Subject mgnlChainSubject = MgnlContext.getSubject();
        if (mgnlChainSubject == null) {
            throw new LoginException("invalid setup or deserialization error");
        }
        Set<Principal> mgnlPrincipals = mgnlChainSubject.getPrincipals();
        subject.getPrincipals().addAll(mgnlPrincipals);
        if (isAdmin()) {
            Iterator<Principal> iter = subject.getPrincipals().iterator();
            while (iter.hasNext()) {
                Principal next = iter.next();
                if (next instanceof org.apache.jackrabbit.core.security.UserPrincipal) {
                    iter.remove();
                    log.debug("logged in as admin ... executing system context op on behalf of user " + next.getName());
                    break;
                }
            }
            this.subject.getPrincipals().add(new MagnoliaJRAdminPrincipal("admin"));
        }

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

    private boolean isAdmin() {
        // TODO: make admin user name configurable (read from properties)
        return this.name != null && this.name.equals("admin");
    }
}
