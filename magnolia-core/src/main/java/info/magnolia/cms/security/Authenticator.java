/**
 * This file Copyright (c) 2003-2008 Magnolia International
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

import info.magnolia.context.MgnlContext;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Id$
 * @deprecated please
 */
public final class Authenticator {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Authenticator.class);

    /**
     * Utility class, don't instantiate.
     */
    private Authenticator() {
        // unused
    }

    /**
     * @param request current HttpServletRequest
     * @return String , current logged in user
     * @deprecated
     */
    public static String getUserId(HttpServletRequest request) {
        return getUserId();
    }

    public static String getUserId() {
        return MgnlContext.getUser().getName();
    }

    /**
     * @param request current HttpServletRequest
     * @return credentials , as received from the servlet request
     */
    public static String getCredentials(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    /**
     * checks user session for attribute "user node"
     * @param request current HttpServletRequest
     * @return <code>true</code> if the user is authenticated, <code>false</code> otherwise
     * @deprecated
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        return isAuthenticated();
    }

    public static boolean isAuthenticated() {
        return !getUserId().equals(UserManager.ANONYMOUS_USER);
    }

    /**
     * @deprecated Use {@link Security#getAnonymousUser()} instead
     */
    public static User getAnonymousUser() {
        return Security.getAnonymousUser();
    }

}
