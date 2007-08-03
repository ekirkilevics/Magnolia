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
package info.magnolia.setup.for3_1;

import info.magnolia.module.delta.MoveAndRenamePropertyTask;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class LoginFormPropertyMovedToFilter extends MoveAndRenamePropertyTask {
    public LoginFormPropertyMovedToFilter() {
        super("Login form", "/server/login", "LoginForm", "/server/filters/uriSecurity/clientCallback", "loginForm");
    }

    protected String modifyCurrentValue(String currentValue) {
        // pre-3.0.2
        if ("/.resources/loginForm/login.html".equals(currentValue)) {
            return "/mgnl-resources/loginForm/login.html";
            // 3.0.2
        } else if ("/mgnl-resources/loginForm/login.html".equals(currentValue)) {
            return "/mgnl-resources/loginForm/login.html";
            // customized
        } else {
            return currentValue;
        }
    }
}
