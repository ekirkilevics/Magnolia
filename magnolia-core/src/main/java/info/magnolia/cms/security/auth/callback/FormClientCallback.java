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

import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;

import info.magnolia.freemarker.FreemarkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sameer Charles
 * $Id$
 */
public class FormClientCallback extends AbstractHttpClientCallback {

    private static final Logger log = LoggerFactory.getLogger(FormClientCallback.class);

    private String loginForm;

    public void doCallback(HttpServletResponse response) {
        try {
            FreemarkerUtil.process(getLoginForm(), new HashMap(), response.getWriter());
        }
        catch (Throwable t) {
            log.error("exception while writing login template", t);
        }
    }

    public String getLoginForm() {
        return loginForm;
    }

    public void setLoginForm(String loginForm) {
        this.loginForm = loginForm;
    }

}
