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
package info.magnolia.cms.security;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.util.FreeMarkerUtil;
import info.magnolia.cms.filters.AbstractMagnoliaFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import freemarker.template.Template;

/**
 * Provides basic infrastructure to authenticate request using form or basic realm
 * @author Sameer Charles
 * $Id$
 */
public abstract class BaseSecurityFilter extends AbstractMagnoliaFilter {

    private static final Logger log = LoggerFactory.getLogger(BaseSecurityFilter.class);

    protected static final String LOGIN_FORM = "LoginForm";

    protected static final String AUTH_TYPE = "AuthType";

    protected static final String AUTH_TYPE_BASIC = "Basic";

    protected static final String AUTH_TYPE_FORM = "Form";

    /**
     * Continue with the magnolia defined filter chain if isAllowed returns true
     * else send an authentication request to the client as configured
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (isAllowed(request, response)) {
            chain.doFilter(request, response);
        } else {
            doAuthenticate(request, response);
        }

    }

    public abstract boolean isAllowed (HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * In most cases this will provide a standard login mechanism, override this to support
     * other login strategies
     * */
    public void doAuthenticate(HttpServletRequest request, HttpServletResponse response) {
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
    public void doFormAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String loginUrl = (String) Server.getInstance().getLoginConfig().get(LOGIN_FORM);

        try {
            // we cannot use FreemarketUtil.process because MgnlContext is not set yet!
            Template tmpl = FreeMarkerUtil.getDefaultConfiguration().getTemplate(loginUrl);
            Map data = new HashMap();
            data.put("contextPath", request.getContextPath());
            tmpl.process(data, response.getWriter());
        }
        catch (Throwable t) {
            log.error("exception while writing login template", t);
        }
    }

    /**
     * Require basic authentication, the browser will take care of this.
     * @param response HttpServletResponse
     */
    public void doBasicAuthentication(HttpServletResponse response) {
        response.setHeader("WWW-Authenticate", "BASIC realm=\"" + Server.getBasicRealm() + "\"");
    }



}
