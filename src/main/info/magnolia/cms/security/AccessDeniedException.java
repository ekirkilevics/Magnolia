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
package info.magnolia.cms.security;

import javax.jcr.RepositoryException;


/**
 * @author Sameer Charles
 * @version 2.01
 */
public class AccessDeniedException extends RepositoryException {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    public AccessDeniedException() {
        super();
    }

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Exception cause) {
        super(message, (cause instanceof AccessDeniedException) ? ((AccessDeniedException) cause).getCause() : cause);
    }

    public AccessDeniedException(Exception root) {
        super(root);
    }

}
