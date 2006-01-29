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
package info.magnolia.module.admininterface;

/**
 * Runtime Exception thrown when a dialog handler can't be instantiated.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class InvalidDialogPageHandlerException extends RuntimeException {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Instantiates a new exception. Use this constructor when configuration is missing.
     * @param pageName missing dialog name
     */
    public InvalidDialogPageHandlerException(String pageName) {
        super("No dialogpage handler for [" + pageName + "] found"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Instantiates a new exception. Use this constructor when the dialog handler can't be instantiated due to a
     * previous exception.
     * @param pageName dialog name
     * @param cause previous exception
     */
    public InvalidDialogPageHandlerException(String pageName, Throwable cause) {
        super("Unable to instantiate a dialogpage handler for [" //$NON-NLS-1$
            + pageName
            + "] due to a " //$NON-NLS-1$
            + cause.getClass().getName()
            + " exception", cause); //$NON-NLS-1$
    }

}
