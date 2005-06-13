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
package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.logging.Log4jConfigurer;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class Initializer extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Initializer.class);

    /**
     * <p>
     * load config data to the servlet instance, accessable via config beans.
     * </p>
     * <ol>
     * <li>Initialize Log4j</li>
     * <li>Load all (website / users / admin / config) repositories</li>
     * <li>Load template config</li>
     * </ol>
     */
    public void init() {

        ServletConfig config = getServletConfig();

        // copy all the initialization parameters in a Map, so that ConfigLoader is not tied to a ServletConfig instance
        Map parameters = new HashMap();
        Enumeration configParams = config.getInitParameterNames();
        while (configParams.hasMoreElements()) {
            String paramName = (String) configParams.nextElement();
            parameters.put(paramName, config.getInitParameter(paramName));
        }

        Log4jConfigurer.initLogging(config.getServletContext(), parameters);

        try {
            new ConfigLoader(config.getServletContext(), parameters);
        }
        catch (Exception e) {
            log.fatal(e.getMessage(), e);
        }
    }

    /**
     * @see javax.servlet.Servlet#destroy()
     */
    public void destroy() {

        ServletConfig config = getServletConfig();

        // copy all the initialization parameters in a Map
        Map parameters = new HashMap();
        Enumeration configParams = config.getInitParameterNames();
        while (configParams.hasMoreElements()) {
            String paramName = (String) configParams.nextElement();
            parameters.put(paramName, config.getInitParameter(paramName));
        }

        Log4jConfigurer.shutdownLogging(parameters);
        super.destroy();
    }
}
