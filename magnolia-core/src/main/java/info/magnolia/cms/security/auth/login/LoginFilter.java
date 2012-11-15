/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.security.auth.login;

import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.context.MgnlContext;
import info.magnolia.logging.AuditLoggingUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.security.auth.Subject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a login operation. Calls all {@link LoginHandler login handlers} until one can handle the request.
 * $Id$
 */
public class LoginFilter extends AbstractMgnlFilter {

    private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);

    private Collection<LoginHandler> loginHandlers = new ArrayList<LoginHandler>();

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        for (LoginHandler handler : this.getLoginHandlers()) {
            LoginResult loginResult = handler.handle(request, response);
            LoginResult.setCurrentLoginResult(loginResult);
            AuditLoggingUtil.log(loginResult, request);
            if (loginResult.getStatus() == LoginResult.STATUS_IN_PROCESS) {
                // special handling to support multi step login mechanisms like ntlm
                // do not continue with the filter chain
                return;
            } else if (loginResult.getStatus() == LoginResult.STATUS_SUCCEEDED) {
                Subject subject = loginResult.getSubject();
                if (subject == null) {
                    log.error("Invalid login result from handler [" + handler.getClass().getName() + "] returned STATUS_SUCCEEDED but no subject");
                    throw new ServletException("Invalid login result");
                }
                if (request.getSession(false) != null) {
                    request.getSession().invalidate();
                }
                MgnlContext.login(subject);
                AuditLoggingUtil.log(loginResult, request);
                // do not continue the login handler chain after a successful login ... otherwise previous success will be invalidated by above session wipeout
                break;
            } else {
                // just log.
                AuditLoggingUtil.log(loginResult, request);
            }

        }
        // continue even if all login handlers failed
        chain.doFilter(request, response);
    }

    public Collection<LoginHandler> getLoginHandlers() {
        return loginHandlers;
    }

    public void setLoginHandlers(Collection<LoginHandler> loginHandlers) {
        this.loginHandlers = loginHandlers;
    }

    public void addLoginHandlers(LoginHandler handler) {
        this.loginHandlers.add(handler);
    }



}
