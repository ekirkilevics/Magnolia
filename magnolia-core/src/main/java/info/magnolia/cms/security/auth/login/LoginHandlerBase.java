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

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;


/**
 * @author philipp
 * @version $Id$
 */
public abstract class LoginHandlerBase implements LoginHandler {

    protected LoginResult authenticate(CredentialsCallbackHandler callbackHandler, String jaasModuleName) {
        return SecuritySupport.Factory.getInstance().authenticate(callbackHandler, jaasModuleName);
    }

}