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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.security.auth.login.LoginException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.callback.PlainTextCallbackHandler;
import info.magnolia.cms.security.Authenticator;

import java.io.IOException;

/**
 * @author Sameer Charles
 * $Id$
 */
public class NTLMLogin implements LoginHandler {

    private static final Logger log = LoggerFactory.getLogger(NTLMLogin.class);

    private HttpServletResponse response;

    private String credentials;

    private boolean connectionEstablished;

    private String userId;

    public int handle(HttpServletRequest request, HttpServletResponse response) {
        this.response = response;
        // starting from NT Challenge/response Step#3
        credentials = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(credentials) && credentials.startsWith("NTLM ")) {
            try {
                talk();
            } catch (IOException ioe) {
                log.warn(ioe.getMessage(), ioe);
                return LoginHandler.STATUS_FAILED;
            }

            // if connection is established after Step-4
            if (isConnectionEstablished()) {
                // todo - what if the request comes from browsers other thn IE
                CredentialsCallbackHandler callbackHandler =
                        new PlainTextCallbackHandler(getUserId(), "".toCharArray());
                try {
                    if (Authenticator.authenticate(request, callbackHandler, null)) {
                        return LoginHandler.STATUS_SUCCEDED;
                    }
                } catch (LoginException le) {
                    log.warn(le.getMessage(), le);
                }
            } else {
                // not yet passed through Step-4
                return LoginHandler.STATUS_IN_PROCESS;
            }
            return LoginHandler.STATUS_FAILED;
        }
        return LoginHandler.STATUS_NOT_HANDLED;
    }

    private void talk() throws IOException {
        byte[] msg = new BASE64Decoder().decodeBuffer(credentials.substring(5));
        if (msg[8] == 1) {
            // Step-3
            byte z = 0;
            byte[] msg1 = {(byte)'N', (byte)'T', (byte)'L', (byte)'M', (byte)'S', (byte)'S', (byte)'P',
            z,(byte)2, z, z, z, z, z, z, z,(byte)40, z, z, z,
            (byte)1, (byte)130, z, z,z, (byte)2, (byte)2,
            (byte)2, z, z, z, z, z, z, z, z, z, z, z, z};
            this.response.setHeader("WWW-Authenticate", "NTLM " + new BASE64Encoder().encodeBuffer(msg1));
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
