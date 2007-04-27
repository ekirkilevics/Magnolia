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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * $Id$
 */
public class LogoutFilter extends BaseSecurityFilter {

    protected static final String PARAMETER_LOGOUT = "mgnlLogout";

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
            session.invalidate();
        }
        return false;
    }


}
