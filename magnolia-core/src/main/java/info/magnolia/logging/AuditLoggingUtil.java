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
package info.magnolia.logging;

import javax.servlet.http.HttpServletRequest;

import info.magnolia.cms.security.auth.login.FormLogin;
import info.magnolia.cms.security.auth.login.LoginResult;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.UserContext;

/**
 * Should be used to log 'auditory actions'
 * @author tmiyar
 *
 */
public class AuditLoggingUtil {

    public static final String ACTION_CREATED = "created";
    public static final String ACTION_MODIFIED = "modified";
    public static final String ACTION_DELETED = "deleted";
    public static final String ACTION_COPIED = "copied";
    public static final String ACTION_MOVED = "moved";
    public static final String ACTION_ACTIVATED = "activated";
    public static final String ACTION_DEACTIVATED = "deactivated";
    public static final String ACTION_LOGGEDIN = "loggedin";
    public static final String ACTION_LOGGEDOUT = "loggedout";

    /**
     * log created, modified, deleted, activated, activated
     */
    public static void log(String action, String workspaceName, String nodePath) {
        AuditLoggingUtil.log( action, workspaceName, nodePath, null );
    }

    /**
     * log copied, moved
     */
    public static void log(String action, String workspaceName, String nodePathFrom, String nodePathTo ) {
        AuditLoggingUtil.log(action, new String[]{AuditLoggingUtil.getUser(), workspaceName, nodePathFrom, nodePathTo});
    }

    /**
     * log user logout
     */
    public static void log(final UserContext userContext ) {
        AuditLoggingUtil.log(AuditLoggingUtil.ACTION_LOGGEDOUT, null, null, null);
    }

    /**
     * log user login
     */
    public static void log(final LoginResult loginResult, final HttpServletRequest request ) {
        String userid = "";
        String result = "";
        if(loginResult.getStatus() == LoginResult.STATUS_SUCCEEDED
                || loginResult.getStatus() == LoginResult.STATUS_FAILED) {
            //need request as if the user is not logged yet, the id is not in the context
            userid = request.getParameter(FormLogin.PARAMETER_USER_ID);

            if(loginResult.getStatus() == LoginResult.STATUS_SUCCEEDED) {
                result = "Success";
            } else {
                result = "Failure " + loginResult.getLoginException().getLocalizedMessage();
            }
            AuditLoggingUtil.log(AuditLoggingUtil.ACTION_LOGGEDIN, new String[]{userid, result});
        }

    }

    private static void log(String action, String[] data) {
        AuditLoggingManager manager = AuditLoggingManager.getInstance();
        if(manager != null) {
            manager.log(action, data);
        }
    }

    private static String getUser() {
        try {
            if(MgnlContext.getInstance() != null && MgnlContext.getUser() != null) {
                return MgnlContext.getUser().getName();
            }
        }catch (Exception e) {
            return "system user";
        }
        return "user not set";
    }

}
