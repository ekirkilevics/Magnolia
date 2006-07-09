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

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is a utility class and does not impose any security rules Its a responsibility of the caller to set and
 * check for lock to meet specific needs
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public final class Lock {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(Lock.class);

    /**
     * Session lock attribute
     */
    private static final String SESSION_LOCK = "mgnlSessionLock"; //$NON-NLS-1$

    /**
     * System wide lock
     */
    private static boolean isSystemLocked;

    /**
     * System wide lock creation time
     */
    private static Date lockSetDate;

    /**
     * Utility class, don't instantiate.
     */
    private Lock() {
        // unused
    }

    /**
     * Set session based lock
     * @param request
     */
    public static void setSessionLock(HttpServletRequest request) {
        log.info("Session lock enabled for user ( " //$NON-NLS-1$
            + Authenticator.getUserId(request)
            + " ) on " //$NON-NLS-1$
            + (new Date()).toString());
        // @todo IMPORTANT remove use of http session
        HttpSession httpsession = request.getSession(true);
        httpsession.setAttribute(SESSION_LOCK, (new Date()).toString());
    }

    /**
     * returns true if this session is locked
     * @param request
     * @return a boolean
     */
    public static boolean isSessionLocked(HttpServletRequest request) {
        // @todo IMPORTANT remove use of http session
        if (request.getSession(true).getAttribute(Lock.SESSION_LOCK) != null) {
            return true;
        }
        return false;
    }

    /**
     * reset session lock
     * @param request
     */
    public static void resetSessionLock(HttpServletRequest request) {
        if (!Lock.isSessionLocked(request)) {
            if (log.isDebugEnabled()) {
                log.debug("No Lock found to reset"); //$NON-NLS-1$
            }
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("Resetting session lock"); //$NON-NLS-1$
            }
            Lock.isSystemLocked = false;
        }
        // @todo IMPORTANT remove use of http session
        HttpSession httpsession = request.getSession(true);
        httpsession.removeAttribute(Lock.SESSION_LOCK);
    }

    /**
     * Set system wide lock
     */
    public static void setSystemLock() {
        if (Lock.isSystemLocked()) {
            if (log.isDebugEnabled()) {
                log.debug("System lock exist, created on " + Lock.lockSetDate.toString()); //$NON-NLS-1$
            }
        }
        else {
            Lock.isSystemLocked = true;
            Lock.lockSetDate = new Date();
            if (log.isDebugEnabled()) {
                log.debug("New System lock created on " + Lock.lockSetDate.toString() + " )"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Reset system wide lock
     */
    public static void resetSystemLock() {
        if (!Lock.isSystemLocked()) {
            log.debug("No Lock found to reset"); //$NON-NLS-1$
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("Resetting system lock created on " + Lock.lockSetDate.toString()); //$NON-NLS-1$
            }
            Lock.isSystemLocked = false;
        }
    }

    /**
     * Return true if system is locked
     * @return a boolean
     */
    public static boolean isSystemLocked() {
        return Lock.isSystemLocked;
    }
}
