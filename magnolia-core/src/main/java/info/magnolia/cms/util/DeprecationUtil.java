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
package info.magnolia.cms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A silly utility class to help marking stuff as deprecated even if they're not used directly
 * (taglib attributes for instance).
 * Logs in std out, std err, the caller's logger, and this class' logger too.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DeprecationUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeprecationUtil.class);
    private static final int MAX_STACKTRACE_ELEMENTS = 3;

    public static void isDeprecated() {
        internal_isDeprecated("No reason given");
    }

    public static void isDeprecated(String reason) {
        internal_isDeprecated(reason);
    }

    private static void internal_isDeprecated(String reason) {
        final StringBuffer out = new StringBuffer("A deprecated class or method was used: ");
        out.append(reason);
        out.append(". Check the following trace: ");
        final Throwable fakeException = new Throwable();
        final StackTraceElement[] elements = fakeException.getStackTrace();
        StackTraceElement el = null;
        // we start at 2 since we don't need the latest elements (ie the call to DeprecationUtil)
        for (int i = 2; (i < elements.length && i <= MAX_STACKTRACE_ELEMENTS); i++) {
            el = elements[i];
            out.append(el);
            out.append(", ");
        }
        out.append("the full stracktrace will be logged in debug mode in the ");
        out.append(log.getName());
        out.append(" category.");
        final String outStr = out.toString();
        if (el != null) {
            final Logger theClassLog = LoggerFactory.getLogger(el.getClassName());
            theClassLog.warn(outStr);
        }
        log.warn(outStr);
        log.debug(outStr, fakeException);
        System.err.println(outStr);
        System.out.println(outStr);
    }
}
