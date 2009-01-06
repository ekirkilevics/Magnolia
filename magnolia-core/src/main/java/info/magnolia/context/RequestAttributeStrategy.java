/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.context;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RequestAttributeStrategy implements AttributeStrategy {

    private static final Logger log = LoggerFactory.getLogger(RequestAttributeStrategy.class);

    private static final long serialVersionUID = 222L;

    private HttpServletRequest request;

    private WebContext ctx;

    public RequestAttributeStrategy() {
    }

    public RequestAttributeStrategy(WebContext ctx) {
        this.ctx = ctx;
    }

    public HttpServletRequest getRequest() {
        return ctx.getRequest();
    }

    public Object getAttribute(String name, int scope) {
        switch (scope) {
            case Context.LOCAL_SCOPE:
                Object obj = getRequest().getAttribute(name);
                if (obj == null) {
                    obj = getRequest().getParameter(name);
                }
                if (obj == null) {
                    // we also expose some of the request properties as attributes
                    if (WebContext.ATTRIBUTE_REQUEST_CHARACTER_ENCODING.equals(name)) {
                        obj = getRequest().getCharacterEncoding();
                    }
                    else if (WebContext.ATTRIBUTE_REQUEST_URI.equals(name)) {
                        obj = getRequest().getRequestURI();
                    }
                }
                return obj;
            case Context.SESSION_SCOPE:
                HttpSession httpsession = getRequest().getSession(false);
                if (httpsession == null) {
                    return null;
                }
                return httpsession.getAttribute(name);
            case Context.APPLICATION_SCOPE:
                return MgnlContext.getSystemContext().getAttribute(name, Context.APPLICATION_SCOPE);
            default:
                log.error("illegal scope passed");
                return null;
        }
    }

    public Map getAttributes(int scope) {
        Map map = new HashMap();
        Enumeration keysEnum;
        switch (scope) {
            case Context.LOCAL_SCOPE:
                // add parameters
                Enumeration paramEnum = getRequest().getParameterNames();
                while (paramEnum.hasMoreElements()) {
                    final String name = (String) paramEnum.nextElement();
                    map.put(name, getRequest().getParameter(name));
                }
                // attributes have higher priority
                keysEnum = getRequest().getAttributeNames();
                while (keysEnum.hasMoreElements()) {
                    String key = (String) keysEnum.nextElement();
                    Object value = getAttribute(key, scope);
                    map.put(key, value);
                }
                return map;
            case Context.SESSION_SCOPE:
                HttpSession httpsession = getRequest().getSession(false);
                if (httpsession == null) {
                    return map;
                }
                keysEnum = httpsession.getAttributeNames();
                while (keysEnum.hasMoreElements()) {
                    String key = (String) keysEnum.nextElement();
                    Object value = getAttribute(key, scope);
                    map.put(key, value);
                }
                return map;
            case Context.APPLICATION_SCOPE:
                return MgnlContext.getSystemContext().getAttributes(Context.APPLICATION_SCOPE);
            default:
                log.error("no illegal scope passed");
                return map;
        }
    }

    public void removeAttribute(String name, int scope) {
        switch (scope) {
            case Context.LOCAL_SCOPE:
                getRequest().removeAttribute(name);
                break;
            case Context.SESSION_SCOPE:
                HttpSession httpsession = getRequest().getSession(false);
                if (httpsession != null) {
                    httpsession.removeAttribute(name);
                }
                break;
            case Context.APPLICATION_SCOPE:
                MgnlContext.getSystemContext().removeAttribute(name, Context.APPLICATION_SCOPE);
                break;
            default:
                log.error("no illegal scope passed");
        }
    }

    public void setAttribute(String name, Object value, int scope) {
        if (value == null) {
            removeAttribute(name, scope);
            return;
        }

        switch (scope) {
            case Context.LOCAL_SCOPE:
                getRequest().setAttribute(name, value);
                break;
            case Context.SESSION_SCOPE:
                if (!(value instanceof Serializable)) {
                    log.warn("Trying to store a non-serializable attribute in session: " + name + ". Object type is " + value.getClass().getName(), new Throwable("This stacktrace has been added to provide debugging information"));
                    return;
                }

                HttpSession httpsession = getRequest().getSession(false);
                if (httpsession == null) {
                    log.debug("Session initialized in order to set attribute '{}' to '{}'. You should avoid using session when possible!", name, value);
                    httpsession = getRequest().getSession(true);
                }

                httpsession.setAttribute(name, value);
                break;
            case Context.APPLICATION_SCOPE:
                MgnlContext.getSystemContext().setAttribute(name, value, Context.APPLICATION_SCOPE);
                break;
            default:
                getRequest().setAttribute(name, value);
                log.debug("Undefined scope, setting attribute [{}] in request scope", name);
        }
    }

}
