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
package info.magnolia.cms.security;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 * Listener could be used to check for the request source/types and match it with the allowed sources and types This is
 * helpful in restricting any authoring or publishing from unwanted sources.
 * @author Sameer Charles
 * @version 1.1
 *
 * @deprecated rename+merge with info.magnolia.cms.beans.config.Listener
 */
public final class Listener {

    /**
     * Utility class, don't instantiate.
     */
    private Listener() {
        // unused
    }

    /**
     * Weather current request have proper access of the method requested.
     * @return boolean
     */
    public static boolean isAllowed(HttpServletRequest req) {
        if (Listener.isIPAllowed(req)) {
            return true;
        }
        return false;
    }

    /**
     * weather IP is in the available listener list OR "*".
     * @return boolean
     */
    private static boolean isIPAllowed(HttpServletRequest req) {
        try {
            Map access = info.magnolia.cms.beans.config.Listener.getInfo(req.getRemoteAddr());
            return (access.get(req.getMethod().toLowerCase()) != null);
        }
        catch (Exception e) {
        }
        try {
            /* probably there is a mapping for "*" */
            Map access = info.magnolia.cms.beans.config.Listener.getInfo("*"); //$NON-NLS-1$
            return (access.get(req.getMethod().toLowerCase()) != null);
        }
        catch (Exception e) {
            return false;
        }
    }
}
