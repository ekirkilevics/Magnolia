/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.security.auth.login;

import info.magnolia.cms.security.User;

import javax.security.auth.login.LoginException;


/**
 * @author philipp
 * @version $Id$
 */
public class LoginResult {
    
    public static LoginResult NOT_HANDLED = new LoginResult(LoginHandler.STATUS_NOT_HANDLED);

    private int status;

    private User user;

    private LoginException loginException;

    /**
     * @param status
     */
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

    public LoginException getLoginException() {
        return this.loginException;
    }
}
