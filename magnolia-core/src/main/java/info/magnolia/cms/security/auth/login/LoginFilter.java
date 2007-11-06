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
import info.magnolia.context.UserContext;
import info.magnolia.context.WebContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * @author Sameer Charles
 * $Id$
 */
public class LoginFilter extends AbstractMgnlFilter {

    private Collection loginHandlers = new ArrayList();

    private ServletContext servletContext;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.servletContext = filterConfig.getServletContext();
    }

    /**
     * todo - temporary fix
     * */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Iterator handlers = this.getLoginHandlers().iterator();
        int status = LoginHandler.STATUS_NOT_HANDLED;
        while (handlers.hasNext()) {
            LoginHandler handler = (LoginHandler) handlers.next();
            int retVal = handler.handle(request, response);
            if (retVal == LoginHandler.STATUS_IN_PROCESS) {
                // special handling to support multi step login mechanisms like ntlm
                // do not continue with the filter chain
                return;
            } else if (retVal == LoginHandler.STATUS_SUCCEDED) {
                status = LoginHandler.STATUS_SUCCEDED;
            }
        }
        // if any of the handlers succeed we have a session and can use WebContext
        if (Authenticator.isAuthenticated(request)) {
            //MgnlContext.initAsWebContext(request, response, servletContext);
        	((UserContext)MgnlContext.getInstance()).login();
            if (status == LoginHandler.STATUS_SUCCEDED) {
                resetSessionAttributes(request.getSession());
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

    /**
     * todo : temporary fix for MAGNOLIA-1598 & MAGNOLIA-1605
     * */
    private void resetSessionAttributes(HttpSession session) {
        Enumeration names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String attributeName = (String) names.nextElement();
            if ("mgnlJAASSubject".equals(attributeName)) {
                continue;
            }
            session.removeAttribute(attributeName);
        }
    }
}
