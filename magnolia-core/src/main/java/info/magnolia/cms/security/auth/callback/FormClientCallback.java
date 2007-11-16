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
package info.magnolia.cms.security.auth.callback;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.security.auth.login.LoginException;

import java.util.HashMap;
import java.util.Map;

import info.magnolia.freemarker.FreemarkerUtil;
import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.security.auth.login.LoginResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.ClassUtils;

/**
 * @author Sameer Charles
 * $Id$
 */
public class FormClientCallback extends AbstractHttpClientCallback {

    private static final Logger log = LoggerFactory.getLogger(FormClientCallback.class);

    public static final String ERROR_STRING = "errorString";

    private String loginForm;

    public void doCallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (!response.isCommitted()) {
                response.setContentType("text/html");
                if (null == request.getCharacterEncoding()) {
                    response.setCharacterEncoding(MIMEMapping.getContentEncodingOrDefault("text/html"));
                }
            }
            FreemarkerUtil.process(getLoginForm(), getMessages(), response.getWriter());
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

    /**
     * simply sets "errorString" in case of login exception.
     * override this to pass more objects to the freemarker template.
     * @return an empty map
     */
    protected Map getMessages() {
        LoginResult loginResult = LoginResult.getCurrentLoginResult();
        LoginException exception = loginResult.getLoginException();
        Map messages = new HashMap();
        if (null != exception) {
            final String exName = ClassUtils.getShortClassName(exception, null);
            final Messages mm = MessagesManager.getMessages();
            final String defaultMessage = mm.get("login.defaultError");
            messages.put(ERROR_STRING, mm.getWithDefault("login." + exName, defaultMessage));
        }
        return messages;
    }

}
