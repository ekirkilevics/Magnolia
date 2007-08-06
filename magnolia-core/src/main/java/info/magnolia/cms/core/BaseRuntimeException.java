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
package info.magnolia.cms.core;

/**
 * Base Runtime Exception class which will provide exception nesting functionalities.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 * @since 2.2
 *
 * @deprecated
 */
public abstract class BaseRuntimeException extends RuntimeException {
    public BaseRuntimeException(String message) {
        super(message);
    }

    public BaseRuntimeException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public BaseRuntimeException(Throwable rootCause) {
        super(rootCause);
    }

}
