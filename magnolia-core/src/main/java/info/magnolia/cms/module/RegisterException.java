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
package info.magnolia.cms.module;

import org.apache.commons.lang.exception.NestableException;


/**
 * If the ModuleFactory can't register a module
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class RegisterException extends NestableException {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    public RegisterException() {
        super();
    }

    public RegisterException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public RegisterException(String arg0) {
        super(arg0);
    }

    public RegisterException(Throwable arg0) {
        super(arg0);
    }
}
