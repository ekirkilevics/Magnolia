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
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.license.LicenseFileExtractor;
import info.magnolia.cms.module.Module;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleManagementException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is an entry point to all config.
 * @author Sameer Charles
 * @version 1.1
 */
public class ConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String JAAS_PROPERTYNAME = "java.security.auth.login.config";

    /**
     * @deprecated only used in bootstrap(), which is deprecated and should be reimplemented.
     */
    public final class BootstrapFileFilter implements Bootstrapper.BootstrapFilter {
        // do not import modules configuration files yet. the module will do it after the registration process
        public boolean accept(String filename) {
            return !filename.startsWith("config.modules");
        }
    }

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
     * @see SystemProperty
     */
    public ConfigLoader(ServletContext context) {

        if (StringUtils.isEmpty(System.getProperty(JAAS_PROPERTYNAME))) {
            try {
                System.setProperty(JAAS_PROPERTYNAME, Path.getAbsoluteFileSystemPath("WEB-INF/config/jaas.config"));
            }
            catch (SecurityException se) {
                log.error("Failed to set " + JAAS_PROPERTYNAME + ", check application server settings");
                log.error(se.getMessage(), se);
                log.info("Aborting startup");
                return;
            }
        }
        else {
            if (log.isInfoEnabled()) {
                log.info("JAAS config file set by parent container or some other application"); //$NON-NLS-1$
                log.info("Config in use " + System.getProperty(JAAS_PROPERTYNAME)); //$NON-NLS-1$ //$NON-NLS-2$
                log.info("Please make sure JAAS config has all necessary modules (refer config/jaas.config) configured"); //$NON-NLS-1$
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
        license.printVersionInfo();

        long millis = System.currentTimeMillis();
        log.info("Initializing content repositories"); //$NON-NLS-1$
        ContentRepository.init();

        log.info("Setting system context"); //$NON-NLS-1$
        MgnlContext.setInstance(MgnlContext.getSystemContext());

        try {
            final ModuleManager moduleManager = ModuleManager.Factory.getInstance();
            moduleManager.checkForInstallOrUpdates();
            moduleManager.getUI().onStartup();

            // TODO make these regular ObservedManagers
            log.info("Init i18n"); //$NON-NLS-1$
            MessagesManager.init(context); // TODO this was done before module init??
            Server.init();

            MIMEMapping.init();
            VersionConfig.getInstance().init();

            // finished
            setConfigured(true);
            log.info("Configuration loaded (took " + ((System.currentTimeMillis() - millis) / 1000) + " seconds)"); //$NON-NLS-1$

            // TODO >> this is now in MagnoliaMainFilter
            //if (ModuleRegistration.getInstance().isRestartNeeded()) {
//            if (moduleManager.isRestartNeeded()) {
//                printSystemRestartInfo();
//            }

        } catch (ModuleManagementException e) {
            log.error("An error occurred during initialization", e); //$NON-NLS-1$
            enterListeningMode();
        } catch (ConfigurationException e) {
            log.error("An error occurred during initialization", e); //$NON-NLS-1$
            enterListeningMode();
        }

    }

    /**
     * Bootstrap the system
     * @deprecated See WebappDelta !
     */
    protected boolean bootstrap() {
        // check for initialized repositories
        boolean initialized;

        try {
            initialized = ContentRepository.checkIfInitialized();
        }
        catch (RepositoryException re) {
            log.error("Unable to initialize repositories. Magnolia can't start.", re); //$NON-NLS-1$
            return false;
        }

        String[] bootDirs = Bootstrapper.getBootstrapDirs();

        if (initialized) {
            // define the system property to (re)bootstrap a singel repository
            String bootstrapIfEmpty = StringUtils.defaultString(SystemProperty.getProperty(SystemProperty.BOOTSTRAP_IF_EMPTY));
            String bootstrapForce = StringUtils.defaultString(SystemProperty.getProperty(SystemProperty.BOOTSTRAP_FORCE));

            if (StringUtils.isNotEmpty(bootstrapIfEmpty) || StringUtils.isNotEmpty(bootstrapForce)) {
                Set repositories = new HashSet();
                String[] ifEmptyRepositories = StringUtils.split(bootstrapIfEmpty,", ");
                String[] forceRepositories = StringUtils.split(bootstrapForce,", ");

                repositories.addAll(Arrays.asList(ifEmptyRepositories));
                repositories.addAll(Arrays.asList(forceRepositories));

                for (Iterator iter = repositories.iterator(); iter.hasNext();) {
                    String repository = (String) iter.next();

                    try {
                        if (ArrayUtils.contains(forceRepositories, repository)) {
                            log.info("will clean and bootstrap the repository {} because the property {} is set",
                                    repository, SystemProperty.BOOTSTRAP_FORCE);
                            Content root = MgnlContext.getHierarchyManager(repository).getRoot();
                            for (Iterator iterator = ContentUtil.getAllChildren(root).iterator(); iterator.hasNext();) {
                                Content node = (Content) iterator.next();
                                node.delete();
                            }
                            root.save();

                            Bootstrapper.bootstrapRepository(repository, new BootstrapFileFilter(), bootDirs);
                        }

                        else if (ArrayUtils.contains(ifEmptyRepositories, repository) && !ContentRepository.checkIfInitialized(repository)) {
                            log.info("will bootstrap the repository {} because the property {} is set",
                                    repository, SystemProperty.BOOTSTRAP_IF_EMPTY);
                            Bootstrapper.bootstrapRepository(repository, new BootstrapFileFilter(), bootDirs);
                        }
                    }
                    catch (Exception e) {
                        // should never be the case since initialized was true
                        log.error("can't bootstrap repository " + repository, e);
                    }
                }
            }
        }
        else {
            log.warn("Repositories are not initialized (no content found)."); //$NON-NLS-1$

            if (bootDirs.length == 0) {
                enterListeningMode();
                return false;
            }

            bootstrapping = true;

            // a bootstrap directory is configured, trying to initialize repositories
            Bootstrapper.bootstrapRepositories(bootDirs, new BootstrapFileFilter());
        }
        return true;
    }

    /**
     * Print the list of modules needing a restart of the container.
     */
    private void printSystemRestartInfo() {
        ModuleLoader loader = ModuleLoader.getInstance();
        System.out.println("-----------------------------------------------------"); //$NON-NLS-1$
        System.out.println("One or more modules need a restart of the webapp:"); //$NON-NLS-1$
        for (Iterator iter = loader.getModuleInstances().keySet().iterator(); iter.hasNext();) {
            String moduleName = (String) iter.next();
            Module module = loader.getModuleInstance(moduleName);
            if (module.isRestartNeeded()) {
                System.out.println(" - " + module.getName() + " (" + module.getModuleDefinition().getVersion() + ")");
            }
        }
        System.out.println("-----------------------------------------------------"); //$NON-NLS-1$
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
        System.out.println("\n-----------------------------------------------------------------"); //$NON-NLS-1$
        System.out.println("Server not configured, entering in listening mode for activation."); //$NON-NLS-1$
        System.out.println("You can now activate content from an existing magnolia instance."); //$NON-NLS-1$
        System.out.println("-----------------------------------------------------------------\n"); //$NON-NLS-1$
        setConfigured(false);
    }

}
