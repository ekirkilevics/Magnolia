/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.security.auth.login;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;

import javax.security.auth.login.LoginException;


/**
 * @author philipp
 * @version $Id$
 */
public class LoginResult {
    
    public static final int STATUS_NO_LOGIN = 0;

    public static LoginResult NOT_HANDLED = new LoginResult(LoginResult.STATUS_NOT_HANDLED);

    public static LoginResult NO_LOGIN = new LoginResult(STATUS_NO_LOGIN);

    /**
     * request attribute holding the login exception
     */
    private static final String ATTRIBUTE_LOGINERROR = "mgnlLoginError";

    
    private int status;

    private User user;

    private LoginException loginException;

    public static final int STATUS_IN_PROCESS = 4;

    public static final int STATUS_NOT_HANDLED = 3;

    public static final int STATUS_FAILED = 2;

    public static final int STATUS_SUCCEEDED = 1;

    public LoginResult(int status) {
        this.status = status;
    }

    public LoginResult(int status, LoginException loginException) {
        this.status = status;
        this.loginException = loginException;
    }

    public LoginResult(int status, User user) {
        this.status = status;
        this.user = user;
    }

    public int getStatus() {
        return this.status;
    }
    
    public User getUser() {
        return this.user;
    }

    /**
     * @return an instance of {@link LoginException}. Warning: it can be null. 
     */
    public LoginException getLoginException() {
        return this.loginException;
    }

    /**
     * Used by the login filter to depose the login result
     */
    public static void setCurrentLoginResult(LoginResult loginResult) {
        MgnlContext.setAttribute(ATTRIBUTE_LOGINERROR, loginResult);
    }

    public static LoginResult getCurrentLoginResult() {
        LoginResult loginResult =  (LoginResult) MgnlContext.getAttribute(LoginResult.ATTRIBUTE_LOGINERROR);
        if(loginResult == null){
            loginResult = NO_LOGIN;
        }
        return loginResult;
    }
}
