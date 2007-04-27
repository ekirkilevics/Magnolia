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

import info.magnolia.cms.core.Access;
import info.magnolia.cms.core.Aggregator;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This Filter protects URI as defined by ROLE(s)/GROUP(s) ACL
 * @author Sameer Charles
 */
public class URISecurityFilter extends BaseSecurityFilter {

    private static final Logger log = LoggerFactory.getLogger(URISecurityFilter.class);

    public static final String URI_REPOSITORY = "uri";

    public static final String URI_WORKSPACE = "default";

    /**
     * Checks access from Listener / Authenticator / AccessLock.
     * @param request HttpServletRequest as received by the service method
     * @param response HttpServletResponse as received by the service method
     * @return boolean <code>true</code> if access to the resource is allowed
     * @throws IOException can be thrown when the servlet is unable to write to the response stream
     */
    public boolean isAllowed(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (!Listener.isAllowed(request)) {
            // todo either deprecate this or move it to separate filter
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        if (Lock.isSystemLocked()) {
            // todo - move Lock checks to separate filter
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        }

        AccessManager accessManager = null;
        if (Authenticator.isAuthenticated(request)) {
            accessManager = MgnlContext.getAccessManager(URI_REPOSITORY, URI_WORKSPACE);
        } else {
            try {
                if (Authenticator.authenticate(request)) {
                    accessManager = MgnlContext.getAccessManager(URI_REPOSITORY, URI_WORKSPACE);
                } else {
                    // todo
                    // use AnonymousContextImpl
                    accessManager = MgnlContext.getAccessManager(URI_REPOSITORY, URI_WORKSPACE);
                }
            } catch (LoginException le) {
                log.warn(le.getMessage(), le);
            }
        }

        return isAuthorized(accessManager, request);
    }

    /**
     * validates user permissions on URI
     * */
    protected boolean isAuthorized(AccessManager accessManager, HttpServletRequest request) {
        if (null == accessManager) return false;
        long permission;
        if (request.getMethod().equalsIgnoreCase("POST")) {
            permission = Permission.WRITE;
        } else {
            permission = Permission.READ;
        }
        try {
            Access.isGranted(accessManager, Aggregator.getHandle(), permission);
            return true;
        } catch (AccessDeniedException ade) {
            log.debug(ade.getMessage(), ade);
        }
        return false;
    }


}
