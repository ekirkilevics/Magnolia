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
package info.magnolia.cms.security;

import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.login.LoginHandler;
import info.magnolia.cms.security.auth.login.LoginResult;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public abstract class SecuritySupportBase implements SecuritySupport {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SecuritySupportBase.class);
    
    public LoginResult authenticate(CredentialsCallbackHandler callbackHandler, String customLoginModule) {
        Subject subject;
        try {
            LoginContext loginContext = createLoginContext(callbackHandler, customLoginModule);
            loginContext.login();
            subject = loginContext.getSubject();
            User user = callbackHandler.getUser();
            // not all jaas modules will support magnolia users
            if(user == null){
                user = SecuritySupport.Factory.getInstance().getUserManager().getUser(subject);
            }
            user.setSubject(subject);
            return new LoginResult(LoginHandler.STATUS_SUCCEDED, user);
        }
        catch (LoginException e) {
            return new LoginResult(LoginHandler.STATUS_FAILED, e);
        }
   }

    protected static LoginContext createLoginContext(CredentialsCallbackHandler callbackHandler, String customLoginModule) throws LoginException {
        LoginContext loginContext = new LoginContext(
            StringUtils.defaultString(customLoginModule, "magnolia"),
            callbackHandler);
        return loginContext;
    }

}
