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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.context.AnonymousContext;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;

import java.io.IOException;
import java.util.Iterator;

import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina $Id$
 */
public class LogoutFilter extends BaseSecurityFilter {

    protected static final String PARAMETER_LOGOUT = "mgnlLogout";

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(LogoutFilter.class);

    public boolean isAllowed(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return handleLogout(request);
    }

    /**
     * Check if a request parameter PARAMETER_LOGOUT is set and logout user.
     * @param request HttpServletRequest
     */
    protected boolean handleLogout(HttpServletRequest request) {
        if (null == request.getParameter(PARAMETER_LOGOUT)) {
            return true;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            // FIXME we check if this is an anonymous context. if so we do not close the sessions
            // this shoulg be replaced by a ctx.logout() call
            if (!(MgnlContext.getInstance() instanceof SystemContext) && !(MgnlContext.getInstance() instanceof AnonymousContext)) {
                Iterator configuredStores = ContentRepository.getAllRepositoryNames();
                while (configuredStores.hasNext()) {
                    String store = (String) configuredStores.next();
                    try {
                        Session jcrSession = MgnlContext.getHierarchyManager(store).getWorkspace().getSession();
                        if (jcrSession.isLive()) {
                            jcrSession.logout();
                        }
                    }
                    catch (Throwable t) {
                        log.debug("Failed to close JCR session", t);
                    }
                }
            }
            session.invalidate();
        }
        return false;
    }

}
