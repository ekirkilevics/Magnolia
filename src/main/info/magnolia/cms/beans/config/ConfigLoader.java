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

import info.magnolia.cms.beans.runtime.SystemProperty;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.license.License;
import info.magnolia.cms.security.SecureURI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletConfig;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
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
    private static Logger log = Logger.getLogger(ConfigLoader.class);

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

        System.setProperty("java.security.auth.login.config", Path
            .getAbsoluteFileSystemPath("WEB-INF/config/jaas.config"));

        this.load();
    }

    /**
     * Load magnolia configuration from repositories.
     */
    private void load() {
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

            bootstrapRepositories(Path.getAbsoluteFileSystemPath(bootdir));
        }

        // todo move to appropriate module classes
        log.info("Init template");
        Template.init();
        log.info("Init paragraph");
        Paragraph.init();
        log.info("Init virtualMap");
        VirtualMap.getInstance().init();

        log.info("Init secureURI");
        SecureURI.init();
        try {
            Server.init();
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
     * Reload all config info in the same objects created on the startup.
     */
    public static void reload() {
        ContentRepository.reload();
        Template.reload();
        Paragraph.reload();
        VirtualMap.getInstance().reload();
        SecureURI.reload();
        try {
            Server.reload();
            ModuleLoader.reload();
        }
        catch (ConfigurationException e) {
            setConfigured(false);
        }
        Listener.reload();
        Subscriber.reload();
        Cache.reload();
        MIMEMapping.reload();
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

    /**
     * Repositories appears to be empty and the <code>"magnolia.bootstrap.dir</code> directory is configured in
     * web.xml. Loops over all the repositories and try to load any xml file found in a subdirectory with the same name
     * of the repository. For example the <code>config</code> repository will be initialized using all the
     * <code>*.xml</code> files found in <code>"magnolia.bootstrap.dir</code><strong>/config</strong> directory.
     * @param bootdir bootstrap dir
     */
    protected void bootstrapRepositories(String bootdir) {

        System.out.println("\n-----------------------------------------------------------------\n");
        System.out.println("Trying to initialize repositories from [" + bootdir + "]");
        System.out.println("\n-----------------------------------------------------------------\n");

        log.info("Trying to initialize repositories from [" + bootdir + "]");

        for (int j = 0; j < ContentRepository.ALL_REPOSITORIES.length; j++) {
            String repository = ContentRepository.ALL_REPOSITORIES[j];

            File xmldir = new File(bootdir, repository);

            if (!xmldir.exists() || !xmldir.isDirectory()) {
                log.info("Directory [" + repository + "] not found for repository [" + repository + "], skipping...");
                continue;
            }

            File[] files = xmldir.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            });

            if (files.length == 0) {
                log.info("No xml files found in directory [" + repository + "], skipping...");
                continue;
            }

            log.info("Trying to import content from " + files.length + " files...");

            HierarchyManager hr = ContentRepository.getHierarchyManager(repository);
            Session session = hr.getWorkspace().getSession();

            for (int k = 0; k < files.length; k++) {
                File xmlfile = files[k];

                InputStream stream;
                try {
                    stream = new FileInputStream(xmlfile);
                }
                catch (FileNotFoundException e) {
                    // should never happen
                    throw new NestableRuntimeException(e);
                }
                try {

                    log.info("Importing content from " + xmlfile.getName());
                    session.importXML("/", stream, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
                }
                catch (Exception e) {
                    log.error("Unable to load content from "
                        + xmlfile.getName()
                        + " due to a "
                        + e.getClass().getName()
                        + " Exception: "
                        + e.getMessage()
                        + ". Will try to continue.", e);
                }
                finally {
                    try {
                        stream.close();
                    }
                    catch (IOException e) {
                        // ignore
                    }
                }

            }

            log.info("Saving changes to [" + repository + "]");

            try {
                session.save();
            }
            catch (RepositoryException e) {
                log.error("Unable to save changes to the ["
                    + repository
                    + "] repository due to a "
                    + e.getClass().getName()
                    + " Exception: "
                    + e.getMessage()
                    + ". Will try to continue.", e);
                continue;
            }

            log.info("Repository [" + repository + "] has been initialized.");

            // importing big repositories can increase memory usage
            System.gc();

        }
    }
}
