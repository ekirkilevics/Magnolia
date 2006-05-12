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
package info.magnolia.cms.exchange;

import info.magnolia.cms.core.BaseException;

import org.apache.commons.lang.StringUtils;


/**
 * @author Sameer Charles
 * $Id$
 */
public class ExchangeException extends BaseException {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    public ExchangeException() {
        super(StringUtils.EMPTY);
    }

    public ExchangeException(String message) {
        super(message);
    }

    public ExchangeException(String message, Throwable cause) {
        super(message, (cause instanceof ExchangeException) ? ((ExchangeException) cause).getCause() : cause);
    }

    public ExchangeException(Throwable root) {
        super(root);
    }
}
