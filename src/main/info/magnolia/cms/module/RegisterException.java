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
package info.magnolia.cms.module;

/**
 * If the ModuleFactory can't register a module
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class RegisterException extends Exception {

    /**
     * Default
     * @param arg0 message
     */
    public RegisterException(String arg0) {
        super(arg0);
    }
}
