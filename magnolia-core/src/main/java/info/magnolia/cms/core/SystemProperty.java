/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.core;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;


/**
 * @author Sameer charles
 * @version 2.0 $Id$
 */
public final class SystemProperty {

    public static final String MAGNOLIA_REPOSITORIES_CONFIG = "magnolia.repositories.config"; //$NON-NLS-1$

    public static final String MAGNOLIA_EXCHANGE_HISTORY = "magnolia.exchange.history"; //$NON-NLS-1$

    public static final String MAGNOLIA_UPLOAD_TMPDIR = "magnolia.upload.tmpdir"; //$NON-NLS-1$

    public static final String MAGNOLIA_CACHE_STARTDIR = "magnolia.cache.startdir"; //$NON-NLS-1$

    public static final String MAGNOLIA_APP_ROOTDIR = "magnolia.app.rootdir"; //$NON-NLS-1$

    public static final String MAGNOLIA_BOOTSTRAP_ROOTDIR = "magnolia.bootstrap.dir"; //$NON-NLS-1$

    public static final String MAGNOLIA_BOOTSTRAP_SAMPLES = "magnolia.bootstrap.samples"; //$NON-NLS-1$

    public static final String MAGNOLIA_WEBAPP = "magnolia.webapp";

    public static final String MAGNOLIA_SERVERNAME = "magnolia.servername";

    private static Properties properties = new Properties();

    /**
     * Web app root key parameter at the servlet context level (i.e. a context-param in web.xml): "webAppRootKey".
     * This property's value is the <strong>name</strong> of the property set at System level, with its value
     * being the root directory of the webapp.
     * @deprecated since 3.5 : this was used in log4j configuration, but we know replace tokens with the Magnolia
     *             properties, effectively making this propertly useless.
     */

    /**
     * Utility class, don't instantiate.
     */
    private SystemProperty() {
        // unused
    }

    /**
     * @param name
     * @param value
     */
    public static void setProperty(String name, String value) {
        properties.put(name, value);
    }

    /**
     * @param name
     */
    public static String getProperty(String name) {
        return (String) properties.get(name);
    }

    /**
     * Returns a boolean property, returning <code>false</code> if the property is not set.
     * @param name property name
     * @return true only if the request property has a value of <code>true</code>
     */
    public static boolean getBooleanProperty(String name) {
        return "true".equals(getProperty(name));
    }

    /**
     * @param name
     * @param defaultValue
     */
    public static String getProperty(String name, String defaultValue) {
        String value = getProperty(name);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

    public static boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * @deprecated since 3.0 - use getProperties() instead
     */
    public static Map getPropertyList() {
        return properties;
    }

    public static Properties getProperties() {
        return properties;
    }

}
