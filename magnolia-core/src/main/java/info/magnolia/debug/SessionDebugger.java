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
package info.magnolia.debug;

import info.magnolia.cms.filters.AbstractMagnoliaFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.IOException;
import java.util.Enumeration;

/**
 * A Filter and Listener that can help debugging session issues.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SessionDebugger extends AbstractMagnoliaFilter implements HttpSessionListener, HttpSessionAttributeListener {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SessionDebugger.class);

    public boolean bypasses(HttpServletRequest request) {
        return false;
    }

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
        sb.append("Event: attribute name: ").append(event.getName()).append("\n");
        sb.append("Event: attribute value: ").append(event.getValue()).append("\n");
    }

}
