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

import info.magnolia.cms.util.DeprecationUtil;
import info.magnolia.init.MagnoliaServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * This class is maintained for compatibility with releases prior to 5.0. Please use {@link info.magnolia.init.MagnoliaServletContextListener} instead.
 * @deprecated since 5.0 - use {@link info.magnolia.init.MagnoliaServletContextListener} instead.
 *
 * @author Fabrizio Giustina
 */
public class MgnlServletContextListener extends MagnoliaServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(MgnlServletContextListener.class);

    public static final String MAGNOLIA_INITIALIZATION_FILE = "magnolia.initialization.file";
    public static final String MAGNOLIA_UNQUALIFIED_SERVER_NAME = "magnolia.unqualified.server.name";

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        DeprecationUtil.isDeprecated("Use info.magnolia.init.MagnoliaServletContextListener instead");
        super.contextDestroyed(sce);
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        DeprecationUtil.isDeprecated("Use info.magnolia.init.MagnoliaServletContextListener instead");
        super.contextInitialized(sce);
    }

    /**
     * @deprecated since 5.0, use {@link #startServer()}, dependencies are injected.
     */
    protected void startServer(final ServletContext context) {
        startServer();
    }

    /**
     * @deprecated since 5.0, use {@link MagnoliaServletContextListener#getPropertiesFilesString(javax.servlet.ServletContext, info.magnolia.init.MagnoliaInitPaths)}
     */
    protected String getPropertiesFilesString(ServletContext context, String servername, String webapp) {
        DeprecationUtil.isDeprecated("Use info.magnolia.init.MagnoliaServletContextListener instead");
        return null;
    }

    /**
     * @deprecated since 5.0, use or subclass {@link info.magnolia.init.MagnoliaInitPaths}.
     */
    @Override
    protected String initServername(boolean unqualified) {
        DeprecationUtil.isDeprecated("Use info.magnolia.init.MagnoliaServletContextListener instead");
        return null;
    }

    /**
     * @deprecated since 5.0, use or subclass {@link info.magnolia.init.MagnoliaInitPaths}.
     */
    @Override
    protected String initRootPath(final ServletContext context) {
        DeprecationUtil.isDeprecated("Use info.magnolia.init.MagnoliaServletContextListener instead");
        return null;
    }

    /**
     * @deprecated since 5.0, use or subclass {@link info.magnolia.init.MagnoliaInitPaths}.
     */
    @Override
    protected String initWebappName(String rootPath) {
        DeprecationUtil.isDeprecated("Use info.magnolia.init.MagnoliaServletContextListener instead");
        return null;
    }

}
