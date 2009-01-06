/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.license.LicenseFileExtractor;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;


/**
 * This class is an entry point to all config.
 * @author Sameer Charles
 * @version 1.1
 */
public class ConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String JAAS_PROPERTYNAME = "java.security.auth.login.config";

    /**
     * Initialize a ConfigLoader instance. All the supplied parameters will be set in
     * <code>info.magnolia.cms.beans.runtime.SystemProperty</code>
     * @param context ServletContext
     * @see SystemProperty
     */
    public ConfigLoader(ServletContext context) {

        if (StringUtils.isEmpty(System.getProperty(JAAS_PROPERTYNAME))) {
            try {
                System.setProperty(JAAS_PROPERTYNAME, Path.getAbsoluteFileSystemPath("WEB-INF/config/jaas.config"));
            }
            catch (SecurityException se) {
                log.error("Failed to set {}, check application server settings", JAAS_PROPERTYNAME );
                log.error(se.getMessage(), se);
                log.info("Aborting startup");
                return;
            }
        } else {
                log.info("JAAS config file set by parent container or some other application"); //$NON-NLS-1$
                log.info("Config in use {}", System.getProperty(JAAS_PROPERTYNAME)); //$NON-NLS-1$ //$NON-NLS-2$
                log.info("Please make sure JAAS config has all necessary modules (refer config/jaas.config) configured"); //$NON-NLS-1$
        }
    }

    public void unload(ServletContext servletContext) {
        ContentRepository.shutdown();
    }

    /**
     * Load magnolia configuration from repositories.
     * @param servletContext ServletContext
     */
    public void load(ServletContext servletContext) {
        // first check for the license information, will fail if this class does not exist
        LicenseFileExtractor license = LicenseFileExtractor.getInstance();
        license.init();
        license.printVersionInfo();

        final long millis = System.currentTimeMillis();
        log.info("Initializing content repositories"); //$NON-NLS-1$

        ContentRepository.init();

        try {
            final ModuleManager moduleManager = ModuleManager.Factory.getInstance();
            moduleManager.checkForInstallOrUpdates();
            moduleManager.getUI().onStartup();

            // TODO make these regular ObservedManagers
            MessagesManager.getInstance().init(servletContext); // TODO this was done before module init??
            MIMEMapping.init();
            VersionConfig.getInstance().init();

            // finished
            log.info("Configuration loaded (took {} seconds)", Long.toString((System.currentTimeMillis() - millis) / 1000)); //$NON-NLS-1$

        } catch (ModuleManagementException e) {
            log.error("An error occurred during initialization", e); //$NON-NLS-1$
        } catch (ConfigurationException e) {
            log.error("An error occurred during initialization", e); //$NON-NLS-1$
        }

    }
}
