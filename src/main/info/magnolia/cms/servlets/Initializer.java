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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;


/**
 * Magnolia default initializer: initialize logging, reads any parameter specified as context-param in web.xml and calls
 * ConfigLoader. Users are free to implement custom loaders which read parameters from other sources. Parameters should
 * be defined in <code>web.xml</code> using <code>context-param</code> elements:
 *
 * <pre>
 * &lt;context-param>
 *   &lt;param-name>&lt;/param-name>
 *   &lt;param-value>&lt;/param-value>
 * &lt;/context-param>
 * </pre>
 *
 * The following parameters are needed:
 * <dl>
 * <dt>magnolia.cache.startdir</dt>
 * <dd>directory used for cached pages</dd>
 * <dt>magnolia.upload.tmpdir</dt>
 * <dd>tmp directory for uploaded files</dd>
 * <dt>magnolia.exchange.history</dt>
 * <dd>history directory used for activation</dd>
 * <dt>magnolia.repositories.config</dt>
 * <dd>repositories configuration</dd>
 * <dt>log4j.config</dt>
 * <dd>Name of a log4j config file. Can be a .properties or .xml file. The value can be:
 * <ul>
 * <li>a full path</li>
 * <li>a path relative to the webapp root</li>
 * <li> a file name which will be loaded from the classpath</li>
 * </ul>
 * </dd>
 * <dt>magnolia.root.sysproperty</dt>
 * <dd>Name of a system variable which will be set to the webapp root. You can use this property in log4j configuration
 * files to handle relative paths, such as <code>${magnolia.root}logs/magnolia-debug.log</code>. <strong>Important</strong>:
 * if you drop multiple magnolia wars in a container which doesn't isolate system properties (e.g. tomcat) you will need
 * to change the name of the <code>magnolia.root.sysproperty</code> variable in web.xml and in log4j configuration
 * files.</dd>
 * <dt>magnolia.bootstrap.dir</dt>
 * <dd> Directory containing xml files for initialization of a blank magnolia instance. If no content is found in any of
 * the repository, they are initialized importing xml files found in this folder. If you don't want to let magnolia
 * automatically initialize repositories simply remove this parameter.</dd>
 * </dl>
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class Initializer implements ServletContextListener {

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
     * load configuration parameters from servlet context, then:
     * </p>
     * <ol>
     * <li>Initialize Log4j</li>
     * <li>Instantiate a <code>info.magnolia.cms.beans.config.ConfigLoader</code> instance</li>
     * </ol>
     * @see ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     * @see ConfigLoader
     */
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        // copy all the initialization parameters in a Map, so that ConfigLoader is not tied to a ServletConfig instance
        Map parameters = new HashMap();
        Enumeration configParams = context.getInitParameterNames();
        while (configParams.hasMoreElements()) {
            String paramName = (String) configParams.nextElement();
            parameters.put(paramName, context.getInitParameter(paramName));
        }

        Log4jConfigurer.initLogging(context, parameters);

        try {
            new ConfigLoader(context, parameters);
        }
        catch (Exception e) {
            log.fatal(e.getMessage(), e);
        }
    }

    /**
     * Shutdown logging.
     * @see ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        // copy all the initialization parameters in a Map
        Map parameters = new HashMap();
        Enumeration configParams = context.getInitParameterNames();
        while (configParams.hasMoreElements()) {
            String paramName = (String) configParams.nextElement();
            parameters.put(paramName, context.getInitParameter(paramName));
        }

        Log4jConfigurer.shutdownLogging(parameters);
    }

}
