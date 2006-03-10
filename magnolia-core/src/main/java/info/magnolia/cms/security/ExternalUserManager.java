/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.security;

import java.util.Collection;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;


/**
 * Manages the JAAS users.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class ExternalUserManager implements UserManager {

    public User getUser(String name) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public Collection getAllUsers() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public User createUser(String name, String pw) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Initialize new user using JAAS authenticated/authorized subject
     * @param subject
     * @throws UnsupportedOperationException
     */
    public User getUserObject(Subject subject) throws UnsupportedOperationException {
        return new ExternalUser(subject);
    }

    /**
     * Authenticate and initialize user using jaas magnolia login module
     * @param userId
     * @param pswd
     * @throws UnsupportedOperationException
     */
    public User getUserObject(String userId, char[] pswd) throws UnsupportedOperationException, LoginException {
        CredentialsCallbackHandler callbackHandler = new CredentialsCallbackHandler(userId, pswd);
        LoginContext loginContext = new LoginContext("magnolia", callbackHandler);
        loginContext.login();
        Subject subject = loginContext.getSubject();
        return this.getUserObject(subject);
    }
}
