/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.security.auth.login;

import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.callback.Base64CallbackHandler;
import info.magnolia.cms.security.Authenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sameer Charles
 * $Id$
 */
public class BasicLogin extends LoginHandlerBase {

    private static final Logger log = LoggerFactory.getLogger(BasicLogin.class);

    public LoginResult handle(HttpServletRequest request, HttpServletResponse response) {
        String credentials = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(credentials) && !credentials.startsWith("NTLM ")) {
            // its a basic authentication request
            CredentialsCallbackHandler callbackHandler = new Base64CallbackHandler(credentials);
            return authenticate(callbackHandler, null);
        }
        return LoginResult.NOT_HANDLED;
    }

}
