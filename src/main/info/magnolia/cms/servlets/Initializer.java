/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */



package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.SystemProperty;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;


/**
 * Date: Jul 20, 2004
 * Time: 2:53:05 PM
 *
 * @author Sameer Charles
 * @version 2.0
 */



public class Initializer extends HttpServlet {



    private static Logger log = Logger.getLogger(Initializer.class);



    /**
     * <p>load config data to the servlet instance, accessable via config beans</p>
     * <p>
     * 1. Load all (website / users / admin / config) repositories<br>
     * 2. Load template config<br>
     * </p>
     */
    public void init() {
        try {
            new ConfigLoader(getServletConfig());
        } catch (Exception e) {
            log.fatal(e.getMessage(), e);
        }
    }


}
