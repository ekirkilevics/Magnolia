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
import info.magnolia.cms.license.License;
import info.magnolia.cms.security.SecureURI;

import java.util.Enumeration;

import javax.servlet.ServletConfig;

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
        Enumeration e = config.getInitParameterNames();
        while (e.hasMoreElements()) {
            String param = (String) e.nextElement();
            String value = config.getInitParameter(param);
            SystemProperty.setProperty(param, value);
        }
        this.load();
    }

    /**
     * Constructor.
     */
    public ConfigLoader() {
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
        ContentRepository.init();

        // todo move to appropriate module classes
        Template.init();
        Paragraph.init();
        VirtualMap.getInstance().init();
        // -----
        SecureURI.init();
        try {
            Server.init();
            ModuleLoader.init();
            setConfigured(true);
        }
        catch (ConfigurationException e) {
            setConfigured(false);
        }
        Listener.init();
        Subscriber.init();
        Cache.init();
        MIMEMapping.init();

        log.info("Configuration loaded!");
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
}
