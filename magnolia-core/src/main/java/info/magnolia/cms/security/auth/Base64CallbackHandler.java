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
package info.magnolia.cms.security.auth;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;


/**
 * Base 64 callback handler supporting Basic authentication
 * @author Sameer Charles $Id$
 */
public class Base64CallbackHandler extends CredentialsCallbackHandler {

    /**
     * default
     */
    public Base64CallbackHandler() {
        // do not instanciate with this constructor
    }

    /**
     * @param credentials Base64 encoded string
     */
    public Base64CallbackHandler(String credentials) {
        credentials = getDecodedCredentials(credentials.substring(6).trim());
        this.name = StringUtils.substringBefore(credentials, ":");
        this.pswd = StringUtils.substringAfter(credentials, ":").toCharArray();
    }

    /**
     * @param credentials to be decoded
     * @return String decoded credentials <b>name:password </b>
     */
    private static String getDecodedCredentials(String credentials) {
        return (new String(Base64.decodeBase64(credentials.getBytes())));
    }

}
