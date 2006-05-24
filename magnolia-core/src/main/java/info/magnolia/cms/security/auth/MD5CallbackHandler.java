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

/**
 * MD5 callback handler supporting Form based authentocation authentication
 * @author Sameer Charles
 * $Id$
 */
public class MD5CallbackHandler extends CredentialsCallbackHandler {

    /**
     * default
     * */
    public MD5CallbackHandler() {
        // do not instanciate with this constructor
    }

    /**
     * @param name
     * @param pswd
     * */
    public MD5CallbackHandler(String name, char[] pswd) {
        super(name, pswd);
    }

}
