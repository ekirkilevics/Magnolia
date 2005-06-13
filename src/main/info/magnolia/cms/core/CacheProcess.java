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
package info.magnolia.cms.core;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


/**
 * Thread responsible to start a cache process. This will only work if a valid request object has been initialized
 * before
 * @author Sameer Charles
 * @version $Revision $ ($Author $)
 */
public class CacheProcess extends Thread {

    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(CacheProcess.class);

    /**
     * request
     */
    private HttpServletRequest request;

    /**
     * This request will be used to stream data
     * @param request HttpServletRequest
     */
    public CacheProcess(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Executes CacheHandler cache method in background
     */
    public void run() {
        CacheHandler.cacheURI(this.request);
    }
}
