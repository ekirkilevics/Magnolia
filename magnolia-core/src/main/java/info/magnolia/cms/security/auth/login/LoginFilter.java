/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.security.auth.login;

import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.cms.security.Authenticator;
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
                request.setAttribute(Authenticator.ATTRIBUTE_LOGINERROR, loginResult.getLoginException());
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

}
