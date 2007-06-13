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
package info.magnolia.cms.security.auth.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Sameer Charles
 * $Id$
 */
public class NTLMClientCallback extends AbstractHttpClientCallback {

    private static final Logger log = LoggerFactory.getLogger(NTLMClientCallback.class);

    // NTLM Step 2
    public void doCallback(HttpServletRequest request, HttpServletResponse response) {
        response.setContentLength(0);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "NTLM");
        try {
            response.flushBuffer();
        } catch (IOException ioe) {
            log.error(ioe.getMessage());
            log.debug("Failed to flush response buffer", ioe);
        }
    }
}
