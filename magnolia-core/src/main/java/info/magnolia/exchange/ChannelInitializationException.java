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
package info.magnolia.exchange;

/**
 * Date: May 4, 2004 Time: 5:40:21 PM
 * @author Sameer Charles
 */
public class ChannelInitializationException extends ChannelException {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    public ChannelInitializationException() {
        super();
    }

    public ChannelInitializationException(String message) {
        super(message);
    }

    public ChannelInitializationException(String message, Exception root) {
        super(message, root);
    }

    public ChannelInitializationException(Exception root) {
        this(null, root);
    }
}
