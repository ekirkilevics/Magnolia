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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * Stores a message in the request. This message can get alerted from the interface. This is used for example to alert
 * activation errors.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class AlertUtil {

    /**
     * The key to store the
     */
    public static String MESSAGE_ATTRIBUTE = "mgnlAlertMsg";

    /**
     * Util: do not instantiate.
     */
    private AlertUtil() {
    }

    /**
     * Store the message. Does not overwrite an already existing message.
     * @param msg
     * @param request
     */
    public static void setMessage(String msg, HttpServletRequest request) {
        if (!isMessageSet(request)) {
            request.setAttribute(MESSAGE_ATTRIBUTE, msg);
        }
    }

    /**
     * Checks if there is a message set
     * @param request
     * @return true if set
     */
    public static boolean isMessageSet(HttpServletRequest request) {
        return StringUtils.isNotEmpty((String) request.getAttribute(MESSAGE_ATTRIBUTE));
    }

    /**
     * Returns the current set message
     * @param request
     * @return the message
     */
    public static String getMessage(HttpServletRequest request) {
        return (String) request.getAttribute(MESSAGE_ATTRIBUTE);
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
}