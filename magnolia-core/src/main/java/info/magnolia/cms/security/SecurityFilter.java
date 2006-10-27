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
import info.magnolia.cms.util.FreeMarkerUtil;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

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
import freemarker.template.Template;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class SecurityFilter implements Filter {

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
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        // unused
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // unused
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
        ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (isAllowed(request, response)) {
            chain.doFilter(request, response);
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
        else if (SecureURI.isUnsecure(Path.getURI(req))) {
            return true;
        }
        else if (SecureURI.isProtected(Path.getURI(req))) {
            return authenticate(req, res);
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
        try {

            String unsecuredUri = (String) Server.getInstance().getLoginConfig().get(UNSECURED_URI);

            if (unsecuredUri != null) {
                if (Path.getURI(request).startsWith(unsecuredUri)) {
                    return true;
                }
            }

            if (!Authenticator.authenticate(request)) {
                // invalidate previous session

                String authType = (String) Server.getInstance().getLoginConfig().get(AUTH_TYPE);

                HttpSession httpsession = request.getSession(false);
                if (httpsession != null) {
                    httpsession.invalidate();
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                if (StringUtils.equalsIgnoreCase(authType, AUTH_TYPE_FORM)
                    && !StringUtils.equals(request.getParameter(AUTH_TYPE), AUTH_TYPE_BASIC)) { // override
                    String loginUrl = (String) Server.getInstance().getLoginConfig().get(LOGIN_FORM);
                    log.debug("Using login url: {}", loginUrl);

                    // Temporary check for conpatibility between dev builds, will be removed before RC4 release
                    // todo remove this check
                    if (StringUtils.equalsIgnoreCase(loginUrl, "/.resources/loginForm/login.html")) {
                        loginUrl = "/mgnl-resources/loginForm/login.html";
                        log.error("Incorrect login form", new Exception());
                        log.error("config/server/LoginForm default value is changed to - /mgnl-resources/loginForm/login.html");
                        log.error("Please bootstrap new config, or change the value manually ");
                    }

                    try {
                        // we cannot use FreemarketUtil.process because MgnlContext is not set yet!
                        Template tmpl = FreeMarkerUtil.getDefaultConfiguration().getTemplate(loginUrl);
                        Map data = new HashMap();
                        data.put("contextPath", request.getContextPath());
                        tmpl.process(data, response.getWriter());
                    }
                    catch (Exception e) {
                        log.error("exception while writing login template", e);
                    }
                }
                else {
                    doBasicAuthentication(response);
                }

                return false;
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    /**
     * @param response
     */
    private void doBasicAuthentication(HttpServletResponse response) {
        response.setHeader("WWW-Authenticate", "BASIC realm=\"" + Server.getBasicRealm() + "\"");
    }

}
