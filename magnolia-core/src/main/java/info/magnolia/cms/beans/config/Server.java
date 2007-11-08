/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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

    public static final class Observer extends FactoryUtil.ObservedObjectFactory {
        public Observer() {
            super(ContentRepository.CONFIG, "/server", ServerConfiguration.class);
        }

        protected Object transformNode(Content node) throws Content2BeanException {
            return Content2BeanUtil.toBean(node, false, new Content2BeanTransformerImpl(){
                public Object newBeanInstance(TransformationState state, Map properties) throws Content2BeanException {
                    return new ServerConfiguration();
                }
            });
        }
    }

    /**
     * @deprecated since 3.1 - not used anymore, this is now an ObservedManager
     */
    public static void init() throws ConfigurationException {
    }

    /**
     * @deprecated since 3.1 - not used anymore, this is now an ObservedManager
     */
    public static void load() throws ConfigurationException {
    }

    /**
     * @deprecated since 3.1 - not used anymore, this is now an ObservedManager
     */
    public static void reload() throws ConfigurationException {
        DeprecationUtil.isDeprecated();
    }

    /**
     * @return default URL extension as configured
     * @deprecated since 3.1 - use ServerConfiguration
     */
    public static String getDefaultExtension() {
        return ServerConfiguration.getInstance().getDefaultExtension();
    }

    /**
     * @return default mail server
     * @deprecated since 3.1 - not used / moved to mail module.
     */
    public static String getDefaultMailServer() {
        DeprecationUtil.isDeprecated("Moved to the mail module, will return empty string.");
        return "";
    }

    /**
     * @return basic realm string
     * @deprecated since 3.1 - this moved to filter configuration - returns default "Magnolia" value.
     */
    public static String getBasicRealm() {
        DeprecationUtil.isDeprecated("This moved to filter configuration, will return default \"Magnolia\" value.");
        return "Magnolia";
    }

    /**
     * @return default base url (empty by default)
     * @deprecated since 3.1 - use ServerConfiguration
     */
    public static String getDefaultBaseUrl() {
        return ServerConfiguration.getInstance().getDefaultBaseUrl();
    }

    /**
     * @return true if the instance is configured as an admin server
     * @deprecated since 3.1 - use ServerConfiguration
     */
    public static boolean isAdmin() {
        return ServerConfiguration.getInstance().isAdmin();
    }

    /**
     * Time in ms since the server was started
     * @deprecated since 3.1 - this is not accurate (and not used)
     */
    public static long getUptime() {
        return System.currentTimeMillis() - uptime;
    }

    /**
     * @deprecated since 3.1 - this was moved to filters configurations - will return an empty Map.
     */
    public Map getLoginConfig() {
        DeprecationUtil.isDeprecated("This moved to filter configuration, will return an empty map.");
        return Collections.EMPTY_MAP;
    }

    /**
     * get server ID
     * @return server id as configured in magnolia.properties
     * TODO : move this to a more appropriate place ?
     * */
    public static String getServerId() {
        return SystemProperty.getProperty(PROPERTY_SERVER_ID);
    }

}
