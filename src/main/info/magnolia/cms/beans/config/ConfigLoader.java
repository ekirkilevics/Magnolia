/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */




package info.magnolia.cms.beans.config;


import org.apache.log4j.Logger;

import javax.jcr.*;
import javax.servlet.ServletConfig;


import java.util.Enumeration;

import info.magnolia.cms.license.License;
import info.magnolia.cms.security.SecureURI;
import info.magnolia.cms.beans.runtime.SystemProperty;
import info.magnolia.logging.Configurator;


/**
 * User: sameercharles
 * Date: Apr 28, 2003
 * Time: 11:20:59 AM
 * @author Sameer Charles
 * @version 1.1
 */


/**
 * <p>
 * This class is an entry point to all config
 *
 * </p>
 *
 *
 * */


public class ConfigLoader {


    private static Logger log = Logger.getLogger(ConfigLoader.class);
    private static boolean isConfigured = false;


    public ConfigLoader(ServletConfig config) {
        Enumeration e = config.getInitParameterNames();
        while (e.hasMoreElements()) {
            String param = (String)e.nextElement();
            String value = config.getInitParameter(param);
            SystemProperty.setProperty(param,value);
        }
        this.load();
    }



    /**
     * constructor
     */
    public ConfigLoader() {
        this.load();
    }



    private void load() {
        /* log4j configurator */
        Configurator.configure();
        /* first check for the license information, will fail if this class does not exist */
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
        } catch (ConfigurationException e) {
            setConfigured(false);
        }
        Listener.init();
        Subscriber.init();
        Cache.init();
        MIMEMapping.init();

        log.info("Configuration loaded!");

    }


    private void printVersionInfo(License license) {
        System.out.println("---------------------------------------------");
        System.out.println("MAGNOLIA LICENSE");
        System.out.println("---------------------------------------------");
        System.out.println("Version number : "+license.get(License.VERSION_NUMBER));
        System.out.println("Build          : "+license.get(License.BUILD_NUMBER));
        System.out.println("Provider       : "+license.get(License.PROVIDER)
                +" ("+license.get(License.PRIVIDER_EMAIL)+")");
    }


    /**
     * <p>reload all config info in the same objects created on the startup</p>
     *
     * @throws RepositoryException
     */
    public static void reload() throws RepositoryException {
        ContentRepository.reload();

        Template.reload();
        Paragraph.reload();
        VirtualMap.getInstance().reload();

        SecureURI.reload();
        try {
            Server.reload();
            ModuleLoader.reload();
        } catch (ConfigurationException e) {
            setConfigured(false);
        }
        Listener.reload();
        Subscriber.reload();
        Cache.reload();
        MIMEMapping.reload();
    }


    /**
     * returns true is magnolia is running with all basic configuration
     * */
    public static boolean isConfigured() {
        return ConfigLoader.isConfigured;
    }


    /**
     * @param configured
     * */
    protected static void setConfigured(boolean configured) {
        ConfigLoader.isConfigured = configured;
    }


}
