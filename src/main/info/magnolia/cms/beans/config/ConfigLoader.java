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
import info.magnolia.cms.license.LicenseFileExtractor;
import info.magnolia.cms.module.ModuleFactory;
import info.magnolia.cms.security.SecureURI;

import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;

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
    private static boolean configured;

    /**
     * Set to true when bootstrapping is in progress or if it has failed.
     */
    private static boolean bootstrapping;

    /**
     * Initialize a ConfigLoader instance. All the supplied parameters will be set in
     * <code>info.magnolia.cms.beans.runtime.SystemProperty</code>
     * @param context ServletContext
     * @param config contains initialization parameters
     * @see SystemProperty
     */
    public ConfigLoader(ServletContext context, Map config) {

        String rootDir = context.getRealPath(StringUtils.EMPTY);

        if (log.isInfoEnabled()) {
            log.info("Assuming paths relative to " + rootDir);
        }
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, rootDir);

        Iterator it = config.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry param = (Map.Entry) it.next();
            SystemProperty.setProperty((String) param.getKey(), (String) param.getValue());
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

        this.load(context);
    }

    /**
     * Load magnolia configuration from repositories.
     * @param context ServletContext
     */
    private void load(ServletContext context) {
        // first check for the license information, will fail if this class does not exist
        LicenseFileExtractor license = LicenseFileExtractor.getInstance();
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

            bootstrapping = true;

            // a bootstrap directory is configured, trying to initialize repositories
            Bootstrapper.bootstrapRepositories(Path.getAbsoluteFileSystemPath(bootdir));
        }

        log.info("Init virtualMap");
        VirtualMap.init();
        log.info("Init i18n");
        MessagesManager.init(context);
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
    private void printVersionInfo(LicenseFileExtractor license) {
        System.out.println("---------------------------------------------");
        System.out.println("MAGNOLIA LICENSE");
        System.out.println("---------------------------------------------");
        System.out.println("Version number : " + license.get(LicenseFileExtractor.VERSION_NUMBER));
        System.out.println("Build          : " + license.get(LicenseFileExtractor.BUILD_NUMBER));
        System.out.println("Provider       : "
            + license.get(LicenseFileExtractor.PROVIDER)
            + " ("
            + license.get(LicenseFileExtractor.PRIVIDER_EMAIL)
            + ")");
    }

    /**
     * Returns true is magnolia is running with all basic configuration.
     * @return <code>true</code> if Magnolia is configured
     */
    public static boolean isConfigured() {
        return ConfigLoader.configured;
    }

    /**
     * Returns true if repository bootstrapping has started but the configuration has not been loaded successfully.
     * @return <code>true</code> if repository bootstrapping has started but the configuration has not been loaded
     * successfully
     */
    public static boolean isBootstrapping() {
        return bootstrapping;
    }

    /**
     * Set the current state of Magnolia.
     * @param configured <code>true</code> if Magnolia is configured
     */
    protected static void setConfigured(boolean configured) {
        ConfigLoader.configured = configured;

        // if we are here, bootstrapping has completed or never started
        ConfigLoader.bootstrapping = false;
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
