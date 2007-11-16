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
package info.magnolia.cms.security.auth.login;

import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Sameer Charles
 * $Id$
 */
public class LoginFilter extends AbstractMgnlFilter {

    private Collection loginHandlers = new ArrayList();
    
    /**
     * request attribute holding the login exception
     */
    private static final String ATTRIBUTE_LOGINERROR = "mgnlLoginError";

    /**
     * todo - temporary fix
     * */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Iterator handlers = this.getLoginHandlers().iterator();
        while (handlers.hasNext()) {
            LoginHandler handler = (LoginHandler) handlers.next();
            LoginResult loginResult = handler.handle(request, response);
            if (loginResult.getStatus() == LoginHandler.STATUS_IN_PROCESS) {
                // special handling to support multi step login mechanisms like ntlm
                // do not continue with the filter chain
                return;
            } else if (loginResult.getStatus() == LoginHandler.STATUS_SUCCEDED) {
                MgnlContext.login(loginResult.getUser());
            }
            // we have to pass the error message to the
            else if (loginResult.getStatus() == LoginHandler.STATUS_FAILED){
                MgnlContext.setAttribute(LoginFilter.ATTRIBUTE_LOGINERROR, loginResult);
            }
        }
        // continue even if all login handlers failed
        chain.doFilter(request, response);
    }

    public Collection getLoginHandlers() {
        return loginHandlers;
    }

    public void setLoginHandlers(Collection loginHandlers) {
        this.loginHandlers = loginHandlers;
    }

    public void addLoginHandlers(LoginHandler handler) {
        this.loginHandlers.add(handler);
    }

    public static LoginResult getCurrentLoginResult() {
        LoginResult loginResult =  (LoginResult) MgnlContext.getAttribute(LoginFilter.ATTRIBUTE_LOGINERROR);
        if(loginResult == null){
            loginResult = LoginResult.NO_LOGIN;
        }
        return loginResult;
    }

}
