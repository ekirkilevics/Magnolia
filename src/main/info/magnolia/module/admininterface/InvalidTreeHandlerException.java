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
package info.magnolia.module.admininterface;

/**
 * Runtime Exception thrown when a tree handler can't be instantiated.
 * @author Fabrizio Giustina
 * @version $Revision: $ ($Author: $)
 */
public class InvalidTreeHandlerException extends RuntimeException {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Instantiates a new exception. Use this constructor when configuration is missing.
     * @param treeName missing tree name
     */
    public InvalidTreeHandlerException(String treeName) {
        super("No tree handler for [" + treeName + "] found");
    }

    /**
     * Instantiates a new exception. Use this constructor when the tree handler can't be instantiated due to a previous
     * exception.
     * @param treeName tree name
     * @param treeName previous exception
     */
    public InvalidTreeHandlerException(String treeName, Throwable cause) {
        super("Unable to instantiate a tree handler for ["
            + treeName
            + "] due to a "
            + cause.getClass().getName()
            + " exception", cause);
    }

}
