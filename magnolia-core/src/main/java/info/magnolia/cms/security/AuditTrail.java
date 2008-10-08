/**
 * This file Copyright (c) 2007-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia.info/mna.html
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
