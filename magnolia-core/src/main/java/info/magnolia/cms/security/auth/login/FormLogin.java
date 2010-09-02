/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.callback.PlainTextCallbackHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses the the {@value #PARAMETER_USER_ID} and  {@value #PARAMETER_PSWD} parameters to login.
 * $Id$
 */
public class FormLogin extends LoginHandlerBase implements LoginHandler {

    private static final Logger log = LoggerFactory.getLogger(FormLogin.class);

    public static final String PARAMETER_USER_ID = "mgnlUserId";

    public static final String PARAMETER_PSWD = "mgnlUserPSWD";

    public static final String PARAMETER_REALM = "mgnlRealm";

    /**
     * The JAAS chain/module to use.
     */

    private String jaasChain = "magnolia";

    public LoginResult handle(HttpServletRequest request, HttpServletResponse response) {
        String userid = request.getParameter(PARAMETER_USER_ID);
        if (StringUtils.isNotEmpty(userid)) {
            String pswd = StringUtils.defaultString(request.getParameter(PARAMETER_PSWD));
            String realm = StringUtils.defaultString(request.getParameter(PARAMETER_REALM));

            CredentialsCallbackHandler callbackHandler = new PlainTextCallbackHandler(userid, pswd.toCharArray(), realm);
            return authenticate(callbackHandler, getJaasChain());
        }
        return LoginResult.NOT_HANDLED;
    }

    public String getJaasChain() {
        return this.jaasChain;
    }


    public void setJaasChain(String jaasChain) {
        this.jaasChain = jaasChain;
    }

}
