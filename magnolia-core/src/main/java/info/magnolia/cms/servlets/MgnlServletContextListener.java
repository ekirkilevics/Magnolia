/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.PropertiesInitializer;
import info.magnolia.cms.beans.config.VersionConfig;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.license.LicenseFileExtractor;
import info.magnolia.context.MgnlContext;
import info.magnolia.logging.Log4jConfigurer;
import info.magnolia.module.ModuleManager;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * <p>
 * Magnolia main listener: reads initialization parameter from a properties file. The name of the file can be defined as
 * a context parameter in web.xml. Multiple path, comma separated, are supported (the first existing file in the list
 * will be used), and the following variables will be used:
 * </p>
 * <ul>
 * <li><b><code>${servername}</code></b>: name of the server where the webapp is running, lowercase</li>
 * <li><b><code>${webapp}</code></b>: the latest token in the webapp path (e.g. <code>magnoliaPublic</code> for a webapp
 * deployed ad <code>tomcat/webapps/magnoliaPublic</code>)</li>
 * </ul>
 * <p>
 * If no <code>magnolia.initialization.file</code> context parameter is set, the following default is assumed:
 * </p>
 *
 * <pre>
 * &lt;context-param>
 *   &lt;param-name>magnolia.initialization.file&lt;/param-name>
 *   &lt;param-value>
 *      WEB-INF/config/${servername}/${webapp}/magnolia.properties,
 *      WEB-INF/config/${servername}/magnolia.properties,
 *      WEB-INF/config/${webapp}/magnolia.properties,
 *      WEB-INF/config/default/magnolia.properties,
 *      WEB-INF/config/magnolia.properties
 *   &lt;/param-value>
 * &lt;/context-param>
 * </pre>
 * <p>
 * The ${servername} variable will be resolved to the full name obtained by using
 * InetAddress.getLocalHost().getHostName(), which may also contain the server domain, depending on your server
 * configuration/operating system. You can set the optional context parameter "magnolia.unqualified.server.name" to true
 * if you prefer using the unqualified name (the server "server.domain.com" will be simply resolved as "server")
 * </p>
 *
 * <pre>
 * &lt;context-param>
 *   &lt;param-name>magnolia.unqualified.server.name&lt;/param-name>
 *   &lt;param-value>true&lt;/param-value>
 * &lt;/context-param>
 * </pre>
 *
 * The following parameters are needed in the properties file:
 * <ul>
 * <li><b>magnolia.cache.startdir</b>:<br/>
 * directory used for cached pages</li>
 * <li><b>magnolia.upload.tmpdir</b>:<br/>
 * tmp directory for uploaded files</li>
 * <li><b>magnolia.exchange.history</b>:<br/>
 * history directory used for activation</li>
 * <li><b>magnolia.repositories.config</b>:<br/>
 * repositories configuration</li>
 * <li><b>log4j.config</b>:<br/>
 * Name of a log4j config file. Can be a .properties or .xml file. The value can be:
 * <ul>
 * <li>a full path</li>
 * <li>a path relative to the webapp root</li>
 * <li>a file name which will be loaded from the classpath</li>
 * </ul>
 * </li>
 * <li><b>magnolia.root.sysproperty</b>:<br/>
 * Name of a system variable which will be set to the webapp root. You can use this property in log4j configuration
 * files to handle relative paths, such as <code>${magnolia.root}logs/magnolia-debug.log</code>.
 * <strong>Important</strong>: if you drop multiple magnolia wars in a container which doesn't isolate system properties
 * (e.g. tomcat) you will need to change the name of the <code>magnolia.root.sysproperty</code> variable in web.xml and
 * in log4j configuration files.</li>
 * <li><b>magnolia.bootstrap.dir</b>:<br/>
 * Directory containing xml files for initialization of a blank magnolia instance. If no content is found in any of
 * the repository, they are initialized importing xml files found in this folder. If you don't want to let magnolia
 * automatically initialize repositories simply remove this parameter.</li>
 * </ul>
 * <h3>Advance use: deployment service</h3>
 * <p>
 * Using the <code>${servername}</code> and <code>${webapp}</code> properties you can easily bundle in the same webapp
 * different set of configurations which are automatically applied dependending on the server name (useful for switching
 * between development, test and production instances where the repository configuration need to be different) or the
 * webapp name (useful to bundle both the public and admin log4j/jndi/bootstrap configuration in the same war). By
 * default the initializer will try to search for the file in different location with different combination of
 * <code>servername</code> and <code>webapp</code>: the <code>default</code> fallback directory will be used if no other
 * environment-specific directory has been added.
 * </p>
 * @author Fabrizio Giustina
 */
public class MgnlServletContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(MgnlServletContextListener.class);

    /**
     * Context parameter name.
     */
    public static final String MAGNOLIA_INITIALIZATION_FILE = "magnolia.initialization.file"; //$NON-NLS-1$

    /**
     * Context parameter name. If set to true in web.xml the server name resolved by magnolia will never contain the
     * domain (the server "server.domain.com" will be simply resolved as "server").
     */
    public static final String MAGNOLIA_UNQUALIFIED_SERVER_NAME = "magnolia.unqualified.server.name"; //$NON-NLS-1$

    private ConfigLoader loader;

    public void contextDestroyed(final ServletContextEvent sce) {
        // avoid disturbing NPEs if the context has never been started (classpath problems, etc)
        ModuleManager mm = ModuleManager.Factory.getInstance();
        if (mm != null) {
            mm.stopModules();
        }

        if (loader != null) {
            MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
                public void doExec() {
                    loader.unload(sce.getServletContext());
                }
            }, true);
        }

        Log4jConfigurer.shutdownLogging();
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(final ServletContextEvent sce) {
        final ServletContext context = sce.getServletContext();

        boolean unqualifiedServerName = BooleanUtils.toBoolean(context.getInitParameter(MAGNOLIA_UNQUALIFIED_SERVER_NAME));

        String servername = initServername(unqualifiedServerName);

        String rootPath = initRootPath(context);

        String webapp = initWebappName(rootPath);

        log.debug("servername is {}, rootPath is {}, webapp is {}", new Object[]{servername, rootPath, webapp}); //$NON-NLS-1$

        String propertiesFilesString = getPropertiesFilesString(context, servername, webapp);

        PropertiesInitializer.getInstance().loadAllProperties(propertiesFilesString, rootPath);

        // expose server name as a system property, so it can be used in log4j configurations
        // rootPath and webapp are not exposed since there can be different webapps running in the same jvm
        System.setProperty("server", servername);

        Log4jConfigurer.initLogging();

        this.loader = new ConfigLoader(ModuleManager.Factory.getInstance(), LicenseFileExtractor.getInstance(), MessagesManager.getInstance(), VersionConfig.getInstance(), context);
        startServer(context);
    }

    protected void startServer(final ServletContext context) {
        MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
            public void doExec() {
                loader.load(context);
            }
        }, true);
    }

    protected String getPropertiesFilesString(ServletContext context, String servername, String webapp) {
        String propertiesFilesString = context.getInitParameter(MAGNOLIA_INITIALIZATION_FILE);
        if (StringUtils.isEmpty(propertiesFilesString)) {
            log.debug("{} value in web.xml is undefined, falling back to default: {}", MgnlServletContextListener.MAGNOLIA_INITIALIZATION_FILE, PropertiesInitializer.DEFAULT_INITIALIZATION_PARAMETER);
            propertiesFilesString = PropertiesInitializer.DEFAULT_INITIALIZATION_PARAMETER;
        } else {
            log.debug("{} value in web.xml is :'{}'", MgnlServletContextListener.MAGNOLIA_INITIALIZATION_FILE, propertiesFilesString); //$NON-NLS-1$
        }
        return PropertiesInitializer.processPropertyFilesString(context, servername, webapp, propertiesFilesString);
    }

    protected String initWebappName(String rootPath) {
        String webapp = StringUtils.substringAfterLast(rootPath, "/"); //$NON-NLS-1$
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_WEBAPP, webapp);
        return webapp;
    }

    /**
     * Initializes the application real root path.
     * @param context Servlet context.
     * @return Real path.
     */

    protected String initRootPath(final ServletContext context) {
        String realPath = StringUtils.replace(context.getRealPath(StringUtils.EMPTY), "\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        realPath = StringUtils.removeEnd(realPath, "/");
        if (realPath == null) {
            // don't use new java.io.File("x").getParentFile().getAbsolutePath() to find out real directory, could throw
            // a NPE for unexpanded war
            throw new RuntimeException("Magnolia is not configured properly and therefore unable to start: real path can't be obtained [ctx real path:" + context.getRealPath(StringUtils.EMPTY) + "]. Please refer to the Magnolia documentation for installation instructions specific to your environment.");
        }
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, realPath);
        return realPath;
    }

    protected String initServername(boolean unqualified) {
        String servername = null;

        try {
            servername = StringUtils.lowerCase(InetAddress.getLocalHost().getHostName());

            if (unqualified && StringUtils.contains(servername, ".")) {
                servername = StringUtils.substringBefore(servername, ".");
            }

            SystemProperty.setProperty(SystemProperty.MAGNOLIA_SERVERNAME, servername);
        } catch (UnknownHostException e) {
            log.error(e.getMessage());
        }
        return servername;
    }

}
