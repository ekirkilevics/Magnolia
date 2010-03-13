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
package info.magnolia.cms.security;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import info.magnolia.context.MgnlContext;


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
            + MgnlContext.getUser().getName()
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
