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
