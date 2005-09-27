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
package info.magnolia.exchange;

/**
 * Date: May 6, 2004 Time: 6:17:12 PM
 * @author Sameer Charles
 */
public class PacketIOException extends ExchangeException {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    public PacketIOException() {
        super();
    }

    public PacketIOException(String message) {
        super(message);
    }

    public PacketIOException(String message, Exception root) {
        super(message, root);
    }

    public PacketIOException(Exception root) {
        this(null, root);
    }
}
