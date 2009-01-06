/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.security;

import info.magnolia.cms.core.Access;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

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
        // todo MAGNOLIA-1617 move this to separate filter
        final IPSecurityManager ipSecurityManager = IPSecurityManager.Factory.getInstance();
        if (!ipSecurityManager.isAllowed(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        if (Lock.isSystemLocked()) {
            // todo - move Lock checks to separate filter
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        }

        AccessManager accessManager = MgnlContext.getAccessManager(URI_REPOSITORY, URI_WORKSPACE);
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
            final String handle = MgnlContext.getAggregationState().getCurrentURI();
            Access.isGranted(accessManager, handle, permission);
            return true;
        } catch (AccessDeniedException ade) {
            log.debug(ade.getMessage());
        }
        return false;
    }


}
