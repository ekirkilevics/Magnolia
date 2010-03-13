/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.debug;

import info.magnolia.cms.filters.AbstractMgnlFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Enumeration;

/**
 * A Filter and Listener that can help debugging session issues.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SessionDebugger extends AbstractMgnlFilter implements HttpSessionListener, HttpSessionAttributeListener {
    private static final Logger log = LoggerFactory.getLogger("info.magnolia.debug");

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            log.warn("-- No session here...");
        } else {
            final StringBuffer msg = new StringBuffer("-- Session found").append("\n");
            dumpSession(session, msg);
            log.warn(msg.toString());
        }
        chain.doFilter(request, response);
    }

    public void sessionCreated(HttpSessionEvent se) {
        logSessionEvent(se, "Session created");
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        logSessionEvent(se, "Session destroyed");
    }

    public void attributeAdded(HttpSessionBindingEvent event) {
        logSessionEvent(event, "Session attribute added");
    }

    public void attributeRemoved(HttpSessionBindingEvent event) {
        logSessionEvent(event, "Session attribute removed");
    }

    public void attributeReplaced(HttpSessionBindingEvent event) {
        logSessionEvent(event, "Session attribute replaced");
    }

    protected void logSessionEvent(HttpSessionEvent event, String s) {
        if(!isEnabled()){
            return;
        }
        final StringBuffer msg = new StringBuffer("-- ");
        msg.append(s).append("\n");
        dumpStacktrace(msg);
        dumpSession(event.getSession(), msg);
        if (event instanceof HttpSessionBindingEvent) {
            dumpSessionBindingEvent((HttpSessionBindingEvent) event, msg);
        }
        msg.append("------------------------\n");
        log.warn(msg.toString());
    }

    protected void dumpStacktrace(StringBuffer sb) {
        sb.append("-- Stack trace :").append("\n");
        final Throwable fakeException = new Throwable();
        final StackTraceElement[] elements = fakeException.getStackTrace();
        // we start at 2 since we don't need the latest elements (ie the calls to logSessionEvent() and dumpStacktrace())
        for (int i = 2; i < elements.length; i++) {
            final StackTraceElement el = elements[i];
            sb.append("    ").append(el).append("\n");
        }
        sb.append("----------").append("\n");
    }

    protected void dumpSession(HttpSession session, StringBuffer sb) {
        try {
            sb.append("-- Session attributes :").append("\n");
            final Enumeration sessAttrNames = session.getAttributeNames();
            while (sessAttrNames.hasMoreElements()) {
                final String attr = (String) sessAttrNames.nextElement();
                sb.append("    ").append(attr).append(" = ").append(session.getAttribute(attr)).append("\n");
            }
            sb.append("-- Session is new : ");
            sb.append(session.isNew()).append("\n");
        } catch (IllegalStateException e) {
            sb.append("-- ").append(e.getMessage());
        }
        sb.append("----------").append("\n");
    }

    protected void dumpSessionBindingEvent(HttpSessionBindingEvent event, StringBuffer sb) {
        sb.append("-- Session event :").append("\n");
        sb.append("  Event: attribute name: ").append(event.getName()).append("\n");
        sb.append("  Event: attribute value: ").append(event.getValue()).append("\n");
    }

}
