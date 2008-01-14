/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.core;

import info.magnolia.api.MgnlException;

import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * Base Exception class which will provide exception nesting functionalities.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public abstract class BaseException extends MgnlException {

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
