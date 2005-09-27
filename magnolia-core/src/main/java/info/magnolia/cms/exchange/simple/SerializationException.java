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
package info.magnolia.cms.exchange.simple;

import info.magnolia.cms.core.BaseException;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public class SerializationException extends BaseException {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Exception cause) {
        super(message, (cause instanceof SerializationException) ? ((SerializationException) cause).getCause() : cause);
    }

    public SerializationException(Exception root) {
        super(root);
    }
}
