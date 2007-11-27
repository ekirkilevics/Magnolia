/**
 * This file Copyright (c) 2003-2007 Magnolia International
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

import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
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
            return new LoginResult(LoginResult.STATUS_SUCCEEDED, user);
        }
        catch (LoginException e) {
            log.debug("can't login due to", e);
            return new LoginResult(LoginResult.STATUS_FAILED, e);
        }
   }

    protected static LoginContext createLoginContext(CredentialsCallbackHandler callbackHandler, String customLoginModule) throws LoginException {
        LoginContext loginContext = new LoginContext(
            StringUtils.defaultString(customLoginModule, "magnolia"),
            callbackHandler);
        return loginContext;
    }

}
