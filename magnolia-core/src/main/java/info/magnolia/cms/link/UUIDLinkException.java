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
package info.magnolia.cms.link;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class UUIDLinkException extends Exception {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(UUIDLinkException.class);

    /**
     * 
     */
    public UUIDLinkException() {
    }

    /**
     * @param arg0
     */
    public UUIDLinkException(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public UUIDLinkException(Throwable arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public UUIDLinkException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
