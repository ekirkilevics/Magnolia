/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.DeprecationUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;

import java.util.Collections;
import java.util.Map;

/**
 * @author Sameer Charles
 * $Id$
 *
 * @deprecated use {@link ServerConfiguration}
 */
public class Server {
    /** @deprecated unused */
    private static final String PROPERTY_SERVER_ID = "magnolia.server.id";

    private static long uptime = System.currentTimeMillis();

    private Server() {
        // static only
    }

    /**
     * @deprecated kept for compatibility
     */
    public static Server getInstance() {
        DeprecationUtil.isDeprecated("This is not returning the proper configuration. Use ServerConfiguration.");
        return new Server();
    }

    /**
     * @deprecated since 3.5 - not used anymore, this is now an ObservedManager
     */
    public static void init() throws ConfigurationException {
    }

    /**
     * @deprecated since 3.5 - not used anymore, this is now an ObservedManager
     */
    public static void load() throws ConfigurationException {
    }

    /**
     * @deprecated since 3.5 - not used anymore, this is now an ObservedManager
     */
    public static void reload() throws ConfigurationException {
        DeprecationUtil.isDeprecated();
    }

    /**
     * @return default URL extension as configured
     * @deprecated since 3.5 - use ServerConfiguration
     */
    public static String getDefaultExtension() {
        return ServerConfiguration.getInstance().getDefaultExtension();
    }

    /**
     * @return default mail server
     * @deprecated since 3.5 - not used / moved to mail module.
     */
    public static String getDefaultMailServer() {
        DeprecationUtil.isDeprecated("Moved to the mail module, will return empty string.");
        return "";
    }

    /**
     * @return basic realm string
     * @deprecated since 3.5 - this moved to filter configuration - returns default "Magnolia" value.
     */
    public static String getBasicRealm() {
        DeprecationUtil.isDeprecated("This moved to filter configuration, will return default \"Magnolia\" value.");
        return "Magnolia";
    }

    /**
     * @return default base url (empty by default)
     * @deprecated since 3.5 - use ServerConfiguration
     */
    public static String getDefaultBaseUrl() {
        return ServerConfiguration.getInstance().getDefaultBaseUrl();
    }

    /**
     * @return true if the instance is configured as an admin server
     * @deprecated since 3.5 - use ServerConfiguration
     */
    public static boolean isAdmin() {
        return ServerConfiguration.getInstance().isAdmin();
    }

    /**
     * Time in ms since the server was started
     * @deprecated since 3.5 - this is not accurate (and not used)
     */
    public static long getUptime() {
        return System.currentTimeMillis() - uptime;
    }

    /**
     * @deprecated since 3.5 - this was moved to filters configurations - will return an empty Map.
     */
    public Map getLoginConfig() {
        DeprecationUtil.isDeprecated("This moved to filter configuration, will return an empty map.");
        return Collections.EMPTY_MAP;
    }

    /**
     * get server ID
     * @return server id as configured in magnolia.properties
     * TODO : move this to a more appropriate place ?
     * @deprecated since 3.5.4 seems unused except ConfigurationPage ?
     */
    public static String getServerId() {
        return SystemProperty.getProperty(PROPERTY_SERVER_ID);
    }

}
