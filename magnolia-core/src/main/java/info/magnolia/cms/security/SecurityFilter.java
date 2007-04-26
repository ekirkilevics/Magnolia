/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.security;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.filters.AbstractMagnoliaFilter;
import info.magnolia.freemarker.FreemarkerHelper;

import java.io.IOException;
import java.util.Map;
import java.util.Collections;

import javax.security.auth.login.LoginException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This filter takes care of applying access control and to invoke the Authenticator when a secure url is calles. It
 * also handles explicit login/logout requests by listening for a few predefined request parameters:
 * <ul>
 * <li>if both <code>mgnlUserId</code> and <code>mgnlUserPSWD</code> are sent and the user is not already
 * authenticated it will try to authenticate it. If a login failure will occur, the related exception will be set into
 * the <code>mgnlLoginError</code> request attribute</li>
 * <li>if the <code>mgnlLogout</code> parameter is send the session will be invalidated and the user logged out</li>
 * </ul>
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class SecurityFilter extends AbstractMagnoliaFilter {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    /**
     * filter config login form
     */
    protected static final String LOGIN_FORM = "LoginForm";

    /**
     * filter config unsecured URI
     */
    protected static final String UNSECURED_URI = "UnsecuredPath";

    /**
     * Authentication type
     */
    protected static final String AUTH_TYPE = "AuthType";

    /**
     * Authentication type Basic
     */
    protected static final String AUTH_TYPE_BASIC = "Basic";

    /**
     * Authentication type Form
     */
    protected static final String AUTH_TYPE_FORM = "Form";

    /**
     * Request parameter: logout.
     */
    protected static final String PARAMETER_LOGOUT = "mgnlLogout";

    /**
     * Request parameter: login error.
     */
    protected static final String ATTRIBUTE_LOGINERROR = "mgnlLoginError";

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        // unused
    }

    /**
     * {@inheritDoc}
     */
    public void destroy() {
        // unused
    }

    /**
     * {@inheritDoc}
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException,
        ServletException {

        handleLogout(request);

        if (isAllowed(request, response)) {
            chain.doFilter(request, response);
        }
    }

    /**
     * Check if a request parameter PARAMETER_LOGOUT is set and logout user.
     * @param request HttpServletRequest
     */
    protected void handleLogout(HttpServletRequest request) {
        if (request.getParameter(PARAMETER_LOGOUT) == null) {
            // go on, logout not requested
            return;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            log.debug("Logging out user");
        }
    }

    /**
     * Checks access from Listener / Authenticator / AccessLock.
     * @param req HttpServletRequest as received by the service method
     * @param res HttpServletResponse as received by the service method
     * @return boolean <code>true</code> if access to the resource is allowed
     * @throws IOException can be thrown when the servlet is unable to write to the response stream
     */
    protected boolean isAllowed(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (Lock.isSystemLocked()) {
            res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        }
        else if (Authenticator.isAuthenticated(req)) {
            return true;
        }

        // MAGNOLIA-1385 allow to login without having to request a secure uri
        if (req.getParameter(Authenticator.PARAMETER_USER_ID) != null
            && req.getParameter(Authenticator.PARAMETER_PSWD) != null) {
            try {
                Authenticator.authenticate(req);
            }
            catch (LoginException e) {
                // set a request parameter that can be used in page to detect an unsuccessful login attempt
                req.setAttribute(ATTRIBUTE_LOGINERROR, e);
            }
        }

        if (SecureURI.isUnsecure(Path.getURI(req))) {
            return true;
        }
        else if (SecureURI.isProtected(Path.getURI(req))) {
            // check if it has just been authenticated using form or try authentication again
            return Authenticator.isAuthenticated(req) || authenticate(req, res);
        }
        else if (!Listener.isAllowed(req)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    /**
     * Authenticate on basic headers.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return <code>true</code> if the user is authenticated
     */
    protected boolean authenticate(HttpServletRequest request, HttpServletResponse response) {

        String unsecuredUri = (String) Server.getInstance().getLoginConfig().get(UNSECURED_URI);

        if (unsecuredUri != null && Path.getURI(request).startsWith(unsecuredUri)) {
            return true;
        }

        try {
            if (!Authenticator.authenticate(request)) {
                doAuthentication(request, response);
                return false;
            }
        }
        catch (LoginException e) {
            log.warn("Login failed: " + e.getMessage(), e);
            return false;
        }

        return true;
    }

    /**
     * Prompt the user for authentication, delegating to the basic/form authentication method as configured.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    protected void doAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String authType = (String) Server.getInstance().getLoginConfig().get(AUTH_TYPE);

        HttpSession httpsession = request.getSession(false);
        if (httpsession != null) {
            httpsession.invalidate();
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        if (StringUtils.equalsIgnoreCase(authType, AUTH_TYPE_FORM)
            && !StringUtils.equals(request.getParameter(AUTH_TYPE), AUTH_TYPE_BASIC)) { // override
            doFormAuthentication(request, response);
        }
        else {
            doBasicAuthentication(response);
        }
    }

    /**
     * Display a login page, processing the configured template using freemarker.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    protected void doFormAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String loginUrl = (String) Server.getInstance().getLoginConfig().get(LOGIN_FORM);
        log.debug("Using login url: {}", loginUrl);

        // Temporary check for conpatibility between dev builds, will be removed before RC4 release
        // todo remove this check
        if (StringUtils.equalsIgnoreCase(loginUrl, "/.resources/loginForm/login.html")) {
            loginUrl = "/mgnl-resources/loginForm/login.html";
            log.error("Incorrect login form: config/server/LoginForm default value is changed to - "
                + "/mgnl-resources/loginForm/login.html. Please bootstrap new config, or change the value manually");
        }

        try {
            final Map data = Collections.singletonMap("contextPath", request.getContextPath());
            FreemarkerHelper.getInstance().render(loginUrl, data, response.getWriter());
        }
        catch (Exception e) {
            log.error("exception while writing login template", e);
        }
    }

    /**
     * Require basic authentication, the browser will take care of this.
     * @param response HttpServletResponse
     */
    protected void doBasicAuthentication(HttpServletResponse response) {
        response.setHeader("WWW-Authenticate", "BASIC realm=\"" + Server.getBasicRealm() + "\"");
    }

}
