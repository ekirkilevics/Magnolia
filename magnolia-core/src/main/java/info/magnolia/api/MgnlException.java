/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.api;

/**
 * @author philipp
 * @version $Id$
 *
 */
public class MgnlException extends Exception {

    public MgnlException() {
        super();
    }

    public MgnlException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public MgnlException(String arg0) {
        super(arg0);
    }

    public MgnlException(Throwable arg0) {
        super(arg0);
    }

}
