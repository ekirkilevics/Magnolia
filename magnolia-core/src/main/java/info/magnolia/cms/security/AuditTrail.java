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

import javax.servlet.http.HttpServletRequest;

import info.magnolia.cms.security.auth.login.FormLogin;
import info.magnolia.cms.security.auth.login.LoginResult;
import info.magnolia.context.UserContext;

import org.slf4j.Logger;

/**
 * Audit user actions
 * @author tmiyar
 *
 */
public class AuditTrail {

    public static void logUserAccess(final Logger log, final UserContext userContext) {
        //audit trail
        log.info(userContext.getUser().getName() + ", LOGOUT" );
    }

    public static void logUserAccess(final Logger log, final LoginResult loginResult, final HttpServletRequest request ) {
        //audit trail
        if(loginResult.getStatus() == LoginResult.STATUS_SUCCEEDED
                || loginResult.getStatus() == LoginResult.STATUS_FAILED) {
            //need request as if the user is not logged yet, the id is not in the context
            String msg = "" + request.getParameter(FormLogin.PARAMETER_USER_ID)
            + ", LOGIN, ";
            if(loginResult.getStatus() == LoginResult.STATUS_SUCCEEDED) {
                msg += "Success";
            } else {
                msg += "Failure " + loginResult.getLoginException().getLocalizedMessage();
            }

            log.info(msg);

        }
    }

}
