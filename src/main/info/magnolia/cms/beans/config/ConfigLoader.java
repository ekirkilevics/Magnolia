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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.license.License;
import info.magnolia.cms.module.ModuleFactory;
import info.magnolia.cms.security.SecureURI;

import java.util.Enumeration;

import javax.jcr.RepositoryException;
import javax.servlet.ServletConfig;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * This class is an entry point to all config.
 * @author Sameer Charles
 * @version 1.1
 */
public class ConfigLoader {

    /**
     * Logger.
     */
    protected static Logger log = Logger.getLogger(ConfigLoader.class);

    /**
     * Is this magnolia istance configured?
     */
    private static boolean isConfigured;

    /**
     * Initialize a ConfigLoader instance. All the supplied parameters will be set in
     * <code>info.magnolia.cms.beans.runtime.SystemProperty</code>
     * @param config ServletConfig
     * @see SystemProperty
     */
    public ConfigLoader(ServletConfig config) {

        String rootDir = config.getServletContext().getRealPath(StringUtils.EMPTY);
        if (log.isInfoEnabled()) {
            log.info("Assuming paths relative to " + rootDir);
        }
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, rootDir);

        Enumeration e = config.getInitParameterNames();
        while (e.hasMoreElements()) {
            String param = (String) e.nextElement();
            String value = config.getInitParameter(param);
            SystemProperty.setProperty(param, value);
        }

        if (StringUtils.isEmpty(System.getProperty("java.security.auth.login.config"))) {
            System.setProperty("java.security.auth.login.config", Path
                .getAbsoluteFileSystemPath("WEB-INF/config/jaas.config"));
        }
        else {
            if (log.isInfoEnabled()) {
                log.info("JAAS config file set by parent container or some other application");
                log.info("Config in use " + System.getProperty("java.security.auth.login.config"));
                log
                    .info("Please make sure JAAS config has all necessary modules (refer config/jaas.config) configured");
            }
        }

        this.load(config);
    }

    /**
     * Load magnolia configuration from repositories.
     */
    private void load(ServletConfig config) {
        // first check for the license information, will fail if this class does not exist
        License license = License.getInstance();
        license.init();
        printVersionInfo(license);

        log.info("Init content repositories");
        ContentRepository.init();

        // check for initialized repositories
        boolean initialized = false;

        try {
            initialized = ContentRepository.checkIfInitialized();
        }
        catch (RepositoryException re) {
            log.fatal("Unable to initialize repositories. Magnolia can't start.", re);
            return;
        }

        if (initialized) {
            log.info("Repositories are initialized (some content found). Loading configuration");
        }
        else {
            log.warn("Repositories are not initialized (no content found).");

            String bootdir = SystemProperty.getProperty(SystemProperty.MAGNOLIA_BOOTSTRAP_ROOTDIR);
            if (StringUtils.isEmpty(bootdir)) {
                enterListeningMode();
                return;
            }

            // a bootstrap directory is configured, trying to initialize repositories
            Bootstrapper.bootstrapRepositories(Path.getAbsoluteFileSystemPath(bootdir));
        }

        // now initialized in admininterface module
        // log.info("Init template");
        // Template.init();

        // now initialized in templating module
        // log.info("Init paragraph");
        // Paragraph.initParagraphs();

        log.info("Init virtualMap");
        VirtualMap.getInstance().init();
        log.info("Init i18n");
        MessagesManager.init(config);

        // now initialized in admininterface module
        // log.info("Init dialog controls");
        // DialogManager.init(config);

        log.info("Init secureURI");
        SecureURI.init();
        try {
            Server.init();
            ModuleFactory.init();
            ModuleLoader.init();
            Listener.init();
            Subscriber.init();
            Cache.init();
            MIMEMapping.init();
            setConfigured(true);
            log.info("Configuration loaded!");
        }
        catch (ConfigurationException e) {
            log.info("An error occurred during initialization, incomplete configuration found?");
            enterListeningMode();
            return;
        }

    }

    /**
     * Print version info to console.
     * @param license loaded License
     */
    private void printVersionInfo(License license) {
        System.out.println("---------------------------------------------");
        System.out.println("MAGNOLIA LICENSE");
        System.out.println("---------------------------------------------");
        System.out.println("Version number : " + license.get(License.VERSION_NUMBER));
        System.out.println("Build          : " + license.get(License.BUILD_NUMBER));
        System.out.println("Provider       : "
            + license.get(License.PROVIDER)
            + " ("
            + license.get(License.PRIVIDER_EMAIL)
            + ")");
    }

    /**
     * Returns true is magnolia is running with all basic configuration.
     * @return <code>true</code> if Magnolia is configured
     */
    public static boolean isConfigured() {
        return ConfigLoader.isConfigured;
    }

    /**
     * Set the current state of Magnolia.
     * @param configured <code>true</code> if Magnolia is configured
     */
    protected static void setConfigured(boolean configured) {
        ConfigLoader.isConfigured = configured;
    }

    /**
     * Set the configured propery to false and print out an informative message to System.out.
     */
    private void enterListeningMode() {
        System.out.println("\n-----------------------------------------------------------------");
        System.out.println("Server not configured, entering in listening mode for activation.");
        System.out.println("You can now activate content from an existing magnolia instance.");
        System.out.println("-----------------------------------------------------------------\n");
        setConfigured(false);
    }

}
