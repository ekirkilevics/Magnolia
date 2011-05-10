/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.cms.util.DeprecationUtil;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A simple wrapper about the few initial variables used to resolve {@link PropertySource}.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DefaultMagnoliaInitPaths implements MagnoliaInitPaths {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMagnoliaInitPaths.class);

    /**
     * Context parameter name. If set to true in web.xml the server name resolved by magnolia will never contain the
     * domain (the server "server.domain.com" will be simply resolved as "server").
     */
    protected static final String MAGNOLIA_UNQUALIFIED_SERVER_NAME = "magnolia.unqualified.server.name";

    /**
     * @deprecated since 5.0 - only used here for retro-compatibility with potential subclasses of MgnlServletContextListener
     */
    private final MagnoliaServletContextListener magnoliaServletContextListener;
    private final String serverName;
    private final String rootPath;
    private final String webappFolderName;
    private final String contextPath;

    public DefaultMagnoliaInitPaths(MagnoliaServletContextListener magnoliaServletContextListener, final ServletContext servletContext) {
        this.magnoliaServletContextListener = magnoliaServletContextListener;
        this.serverName = determineServerName(servletContext);
        this.rootPath = determineRootPath(servletContext);
        this.webappFolderName = determineWebappFolderName(rootPath, servletContext);
        this.contextPath = determineContextPath(servletContext);
        log.debug("servername is {}, rootPath is {}, webapp is {}, contextPath is {}", new Object[]{serverName, rootPath, webappFolderName, contextPath});
    }

    /**
     * Figures out the local host name, makes sure it's lowercase, and use its unqualified name if the {@value #MAGNOLIA_UNQUALIFIED_SERVER_NAME} init parameter is set to true.
     */
    protected String determineServerName(ServletContext context) {
        final boolean unqualifiedServerName = BooleanUtils.toBoolean(context.getInitParameter(MAGNOLIA_UNQUALIFIED_SERVER_NAME));
        final String retroCompatMethodCall = magnoliaServletContextListener.initServername(unqualifiedServerName);
        if (retroCompatMethodCall != null) {
            DeprecationUtil.isDeprecated("You should update your code and override determineServerName(ServletContext) instead of initServername(String)");
            return retroCompatMethodCall;
        }

        try {
            String serverName = StringUtils.lowerCase(InetAddress.getLocalHost().getHostName());

            if (unqualifiedServerName && StringUtils.contains(serverName, ".")) {
                serverName = StringUtils.substringBefore(serverName, ".");
            }
            return serverName;
        } catch (UnknownHostException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * Figures out the root path where the webapp is deployed.
     */
    protected String determineRootPath(ServletContext context) {
        final String retroCompatMethodCall = magnoliaServletContextListener.initRootPath(context);
        if (retroCompatMethodCall != null) {
            DeprecationUtil.isDeprecated("You should update your code and override determineRootPath(ServletContext) instead of initRootPath(ServletContext)");
            return retroCompatMethodCall;
        }

        String realPath = StringUtils.replace(context.getRealPath(StringUtils.EMPTY), "\\", "/");
        realPath = StringUtils.removeEnd(realPath, "/");
        if (realPath == null) {
            // don't use new java.io.File("x").getParentFile().getAbsolutePath() to find out real directory, could throw
            // a NPE for unexpanded war
            throw new RuntimeException("Magnolia is not configured properly and therefore unable to start: real path can't be obtained [ctx real path:" + context.getRealPath(StringUtils.EMPTY) + "]. Please refer to the Magnolia documentation for installation instructions specific to your environment.");
        }
        return realPath;
    }

    protected String determineWebappFolderName(String determinedRootPath, ServletContext context) {
        final String retroCompatMethodCall = magnoliaServletContextListener.initWebappName(determinedRootPath);
        if (retroCompatMethodCall != null) {
            DeprecationUtil.isDeprecated("You should update your code and override determineWebappFolderName(String, ServletContext) instead of initWebappName(String)");
            return retroCompatMethodCall;
        }

        return StringUtils.substringAfterLast(determinedRootPath, "/");
    }

    protected String determineContextPath(ServletContext context) {
        // Getting the contextPath via reflection, until we can depend on servlet 2.5 : See MAGNOLIA-3094
        try {
            final Method getContextPath = context.getClass().getMethod("getContextPath", null);
            return (String) getContextPath.invoke(context);
        } catch (NoSuchMethodException e) {
            log.info("Magnolia appears to be running on a server using a Servlet API version older than 2.5, so we can not know the contextPath at startup.");
            return null;
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public String getRootPath() {
        return rootPath;
    }

    @Override
    public String getWebappFolderName() {
        return webappFolderName;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

}
