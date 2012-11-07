/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.init;

import info.magnolia.cms.core.Path;
import info.magnolia.init.properties.FileSystemPropertySource;
import info.magnolia.init.properties.ServletContextPropertySource;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static info.magnolia.cms.beans.config.PropertiesInitializer.processPropertyFilesString;

/**
 * Resolves the paths of the properties files to load. The name of the file can be defined as a context parameter in 
 * web.xml. Multiple paths, comma separated, are supported (the first existing file in the list will be used), and the
 * following variables will be used:
 *
 * <ul>
 * <li><b><code>${servername}</code></b>: name of the server where the webapp is running, lowercase</li>
 * <li><b><code>${webapp}</code></b>: the last token in the webapp path (e.g. <code>magnoliaPublic</code> for a webapp
 * deployed at <code>tomcat/webapps/magnoliaPublic</code>)</li>
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
 *
 * <h3>Advanced usage: deployment service</h3>
 *
 * <p>
 * Using the <code>${servername}</code> and <code>${webapp}</code> properties you can easily bundle in the same webapp
 * different set of configurations which are automatically applied dependending on the server name (useful for switching
 * between development, test and production instances where the repository configuration need to be different) or the
 * webapp name (useful to bundle both the public and admin log4j/jndi/bootstrap configuration in the same war). By
 * default the initializer will try to search for the file in different location with different combination of
 * <code>servername</code> and <code>webapp</code>: the <code>default</code> fallback directory will be used if no other
 * environment-specific directory has been added.
 * </p>
 *
 * <p>The variables are provided by {@link MagnoliaInitPaths}.</p>
 * 
 * @version $Id$
 */
@Singleton
public class DefaultMagnoliaPropertiesResolver implements MagnoliaPropertiesResolver {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMagnoliaPropertiesResolver.class);

    /**
     * Context parameter name. Value should be a comma-separated list of paths to look for properties files.
     * Defaults to {@value #DEFAULT_INITIALIZATION_PARAMETER}
     */
    protected static final String MAGNOLIA_INITIALIZATION_FILE = "magnolia.initialization.file";

    /**
     * Default value for the MAGNOLIA_INITIALIZATION_FILE parameter.
     */
    protected static final String DEFAULT_INITIALIZATION_PARAMETER =
                      "WEB-INF/config/${servername}/${contextPath}/magnolia.properties,"
                    + "WEB-INF/config/${servername}/${webapp}/magnolia.properties,"
                    + "WEB-INF/config/${servername}/magnolia.properties,"
                    + "WEB-INF/config/${contextPath}/magnolia.properties,"
                    + "WEB-INF/config/${webapp}/magnolia.properties,"
                    + "WEB-INF/config/default/magnolia.properties,"
                    + "WEB-INF/config/magnolia.properties";

    private final ServletContext context;
    private final List<String> locations;

    @Inject
    public DefaultMagnoliaPropertiesResolver(ServletContext context, MagnoliaInitPaths initPaths) {
        this.context = context;
        String propertiesFilesString = getInitParameter(context, MAGNOLIA_INITIALIZATION_FILE, DEFAULT_INITIALIZATION_PARAMETER);

        // Use ROOT for the default (root) context, otherwise trim the leading slash to prevent double slashes
        String contextPath = initPaths.getContextPath();
        if (contextPath.length() == 0) {
            contextPath = "ROOT";
        } else {
            contextPath = contextPath.substring(1);
        }

        final String propertiesLocationString = processPropertyFilesString(context, initPaths.getServerName(), initPaths.getWebappFolderName(), propertiesFilesString, contextPath);
        final String[] propertiesLocation = propertiesLocationString.trim().split("[,]+",0);
        this.locations = new ArrayList<String>(propertiesLocation.length);
        // TODO - order ?
        // trim all:
        for (String loc : propertiesLocation) {
            locations.add(loc.trim());
        }
    }

    protected String getInitParameter(ServletContext ctx, String name, String defaultValue) {
        final String propertiesFilesString = ctx.getInitParameter(name);
        if (StringUtils.isEmpty(propertiesFilesString)) {
            log.debug("{} value in web.xml is undefined, falling back to default: {}", name, defaultValue);
            return defaultValue;
        }
        log.debug("{} value in web.xml is :'{}'", name, propertiesFilesString);
        return propertiesFilesString;
    }

    /**
     * Used in tests, potentially in subclasses.
     */
    protected List<String> getLocations() {
        return locations;
    }

    @Override
    public List<PropertySource> getSources() {
        final List<PropertySource> sources = new ArrayList<PropertySource>();
        boolean foundFiles = false;
        for (String location : getLocations()) {
            try {
                if (Path.isAbsolute(location)) {
                    sources.add(new FileSystemPropertySource(location));
                } else {
                    sources.add(new ServletContextPropertySource(context, location));
                }
                foundFiles = true;
            } catch (FileNotFoundException e) {
                log.debug("Configuration file not found with path [{}]", location);
            } catch (IOException e) {
                throw new RuntimeException(e); // TODO
            }
        }
        if (!foundFiles) {
            log.warn("No configuration files found using location list {}.", getLocations());
        }

        return sources;
    }
}
