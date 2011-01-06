/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
