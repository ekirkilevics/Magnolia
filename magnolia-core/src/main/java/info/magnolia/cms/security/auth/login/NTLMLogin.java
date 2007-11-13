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
import info.magnolia.cms.security.auth.callback.PlainTextCallbackHandler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sameer Charles
 * $Id$
 */
public class NTLMLogin extends LoginHandlerBase {

    private static final Logger log = LoggerFactory.getLogger(NTLMLogin.class);

    private HttpServletResponse response;

    private String credentials;

    private boolean connectionEstablished;

    private String userId;

    public LoginResult handle(HttpServletRequest request, HttpServletResponse response) {
        this.response = response;
        // starting from NT Challenge/response Step#3
        credentials = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(credentials) && credentials.startsWith("NTLM ")) {
            try {
                talk();
            } catch (IOException ioe) {
                log.warn(ioe.getMessage(), ioe);
                return new LoginResult(LoginHandler.STATUS_FAILED);
            }

            // if connection is established after Step-4
            if (isConnectionEstablished()) {
                // todo - what if the request comes from browsers other thn IE
                CredentialsCallbackHandler callbackHandler =
                        new PlainTextCallbackHandler(getUserId(), "".toCharArray());
                    return authenticate(callbackHandler, null);
            } else {
                // not yet passed through Step-4
                return new LoginResult(LoginHandler.STATUS_IN_PROCESS);
            }
        }
        return LoginResult.NOT_HANDLED;
    }

    private void talk() throws IOException {
        byte[] msg = Base64.decodeBase64(credentials.substring(5).getBytes());
        if (msg[8] == 1) {
            // Step-3
            byte z = 0;
            byte[] msg1 = {(byte)'N', (byte)'T', (byte)'L', (byte)'M', (byte)'S', (byte)'S', (byte)'P',
            z,(byte)2, z, z, z, z, z, z, z,(byte)40, z, z, z,
            (byte)1, (byte)130, z, z,z, (byte)2, (byte)2,
            (byte)2, z, z, z, z, z, z, z, z, z, z, z, z};
            this.response.setHeader("WWW-Authenticate", "NTLM " + Base64.encodeBase64(msg1));
            this.response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else if (msg[8] == 3) {
            // Step-4
            int startPos = 30;
            int length = msg[startPos+9]*256 + msg[startPos+8];
            int offset = msg[startPos+11]*256 + msg[startPos+10];
            String userId = new String(msg, offset, length, "UTF-16LE");
            this.setUserId(userId);
            this.setConnectionEstablished(true);
        }
    }

    private boolean isConnectionEstablished() {
        return connectionEstablished;
    }

    private void setConnectionEstablished(boolean connectionEstablished) {
        this.connectionEstablished = connectionEstablished;
    }

    private String getUserId() {
        return userId;
    }

    private void setUserId(String userId) {
        this.userId = userId;
    }
}
