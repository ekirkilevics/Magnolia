/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.security;

import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.security.auth.PrincipalCollection;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.Session;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This Filter protects URI as defined by ROLE(s)/GROUP(s) ACL.
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
    @Override
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
     * Validates user permissions on URI.
     */
    protected boolean isAuthorized(AccessManager accessManager, HttpServletRequest request) {
        String permission;
        if (request.getMethod().equalsIgnoreCase("POST")) {
            permission = Session.ACTION_ADD_NODE;
        } else {
            permission = Session.ACTION_READ;
        }
        final String uri = MgnlContext.getAggregationState().getCurrentURI();
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.warn("no session == running as anonymous");
            return false;
        }
        List<Permission> permissions = null;
        Subject sbj = (Subject) session.getAttribute(Subject.class.getName());
        Set<PrincipalCollection> allPermissions = sbj.getPrincipals(PrincipalCollection.class);
        for (PrincipalCollection principal : allPermissions) {
            Iterator<Principal> iter = principal.iterator();
            while (iter.hasNext()) {
                Principal maybeAcl = iter.next();
                if (maybeAcl instanceof ACL) {
                    ACL acl = ((ACL) maybeAcl);
                    if ("uri".equals(acl.getWorkspace())) {
                        permissions = acl.getList();
                        break;
                    }
                }
            }
        }
        if (permissions == null) {
            log.warn("no permissions found for " + MgnlContext.getUser().getName());
            return false;
        }
        AccessManagerImpl ami = new AccessManagerImpl();
        ami.setPermissionList(permissions);
        boolean grant = ami.isGranted(uri, ami.convertPermissions(permission));
        // MgnlContext.getJCRSession("uri").checkPermission(uri, permission);
        log.debug("user {} has " + (grant ? "" : "NOT ") + "been granted permission {} to access uri {}", new Object[] { MgnlContext.getUser().getName(), permission, uri });
        return grant;
    }
}
