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
package info.magnolia.cms.util;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import org.apache.commons.lang.StringUtils;


/**
 * Stores a message in the request. This message can get alerted from the interface. This is used for example to alert
 * activation errors.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class AlertUtil {

    /**
     * Util: do not instantiate.
     */
    private AlertUtil() {
    }

    /**
     * Store the message. Does not overwrite an already existing message.
     * @param msg
     */
    public static void setMessage(String msg) {
        setMessage(msg, MgnlContext.getInstance());
    }

    public static void setMessage(String msg, Context ctx) {
        if (!isMessageSet(ctx)) {
            ctx.setAttribute(Context.ATTRIBUTE_MESSAGE, msg, Context.LOCAL_SCOPE);
        }
    }

    /**
     * create a message containing the exception message
     * @param msg
     * @param e
     */
    public static void setMessage(String msg, Exception e) {
        setMessage(msg, e, MgnlContext.getInstance());
    }

    public static void setMessage(String msg, Exception e, Context ctx) {
        setMessage(msg + " : " + getExceptionMessage(e), ctx);
    }

    public static void setException(String msg, Exception e) {
        setException(msg, e, MgnlContext.getInstance());
    }

    public static void setException(String msg, Exception e, Context ctx) {
        setMessage(msg + " : " + getExceptionMessage(e), ctx);
        setException(e, ctx);
    }

    /**
     * Checks if there is a message set
     * @param request
     * @return true if set
     */

    public static boolean isMessageSet() {
        return isMessageSet(MgnlContext.getInstance());
    }

    public static boolean isMessageSet(Context ctx) {
        return StringUtils.isNotEmpty((String) ctx.getAttribute(Context.ATTRIBUTE_MESSAGE, Context.LOCAL_SCOPE));
    }

    /**
     * Store the exception. Does not overwrite an already existing one.
     */
    public static void setException(Exception e) {
        setException(e, MgnlContext.getInstance());
    }

    public static void setException(Exception e, Context ctx) {
        if (!isExceptionSet(ctx)) {
            ctx.setAttribute(Context.ATTRIBUTE_EXCEPTION, e, Context.LOCAL_SCOPE);
            // has only an effect if not yet set
            setMessage(getExceptionMessage(e), ctx);
        }
    }

    /**
     * Checks if there is a message set
     * @return true if set
     */
    public static boolean isExceptionSet() {
        return isExceptionSet(MgnlContext.getInstance());
    }

    public static boolean isExceptionSet(Context ctx) {
        return ctx.getAttribute(Context.ATTRIBUTE_EXCEPTION, Context.LOCAL_SCOPE) != null;
    }

    /**
     * Returns the current set message
     * @param request
     * @return the message
     */
    public static String getMessage() {
        return getMessage(MgnlContext.getInstance());
    }

    public static String getMessage(Context ctx) {
        return (String) ctx.getAttribute(Context.ATTRIBUTE_MESSAGE, Context.LOCAL_SCOPE);
    }

    /**
     * Creates a string message out of an exception. Handles nested exceptions.
     * @param e
     * @return the message
     */
    public static String getExceptionMessage(Exception e) {
        String message = e.getMessage();
        if (StringUtils.isEmpty(message)) {
            if (e.getCause() != null) {
                message = e.getCause().getMessage();
            }
            if (message == null) {
                message = StringUtils.EMPTY;
            }
        }
        return message;
    }

    public static Exception getException() {
        return getException(MgnlContext.getInstance());
    }

    public static Exception getException(Context ctx) {
        return (Exception) ctx.getAttribute(Context.ATTRIBUTE_EXCEPTION, Context.LOCAL_SCOPE);
    }
}
