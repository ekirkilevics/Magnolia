/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core;

import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * Base Exception class which will provide exception nesting functionalities.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public abstract class BaseException extends Exception {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * The cause for this exception.
     */
    protected Throwable rootCause;

    /**
     * Instantiate a new exception with an error message.
     * @param message error message
     */
    public BaseException(String message) {
        super(message);
    }

    /**
     * Instantiate a new exception with an error message and a previous exception.
     * @param message error message
     * @param rootCause previous exception
     */
    public BaseException(String message, Throwable rootCause) {
        super(message);
        this.rootCause = rootCause;
    }

    /**
     * Instantiate a new exception with only previous exception.
     * @param rootCause previous exception
     */
    public BaseException(Throwable rootCause) {
        this.rootCause = rootCause;
    }

    /**
     * @see java.lang.Throwable#getMessage()
     */
    public String getMessage() {
        String message = super.getMessage();
        if (rootCause == null) {
            return message;
        }

        String rootMessage = rootCause.getMessage();
        return message != null ? message + ": " + rootMessage : rootMessage; //$NON-NLS-1$

    }

    /**
     * @see java.lang.Throwable#getLocalizedMessage()
     */
    public String getLocalizedMessage() {
        String message = super.getLocalizedMessage();
        if (rootCause == null) {
            return message;
        }

        String rootMessage = rootCause.getLocalizedMessage();
        return message != null ? message + ": " + rootMessage : rootMessage; //$NON-NLS-1$

    }

    /**
     * @see java.lang.Throwable#getCause()
     */
    public Throwable getCause() {
        return rootCause;
    }

    /**
     * @see java.lang.Throwable#printStackTrace()
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
     */
    public void printStackTrace(PrintStream s) {
        synchronized (s) {
            super.printStackTrace(s);
            if (rootCause != null) {
                rootCause.printStackTrace(s);
            }
        }
    }

    /**
     * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
     */
    public void printStackTrace(PrintWriter s) {
        synchronized (s) {
            super.printStackTrace(s);
            if (rootCause != null) {
                rootCause.printStackTrace(s);
            }
        }
    }
}
