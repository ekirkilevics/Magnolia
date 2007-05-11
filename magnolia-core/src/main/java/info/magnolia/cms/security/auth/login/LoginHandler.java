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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Sameer Charles
 * $Id$
 */
public interface LoginHandler {

    public static final int STATUS_SUCCEDED = 1;

    public static final int STATUS_FAILED = 2;

    public static final int STATUS_NOT_HANDLED = 3;

    public static final int STATUS_IN_PROCESS = 4;

    /**
     * Returns true if the handler login succeeds
     * */
    public int handle(HttpServletRequest request, HttpServletResponse response);

}
