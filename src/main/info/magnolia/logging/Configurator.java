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



package info.magnolia.logging;

import org.apache.log4j.PropertyConfigurator;
import info.magnolia.cms.util.Path;
import info.magnolia.cms.util.Path;


/**
 * Date: May 13, 2004
 * Time: 3:06:52 PM
 *
 * @author Sameer charles
 * @version 2.0
 */



public class Configurator {




    public static void configure() {
        PropertyConfigurator.configure(Path.getLogPropertiesFilePath());
    }


    
    public static void configure(String path) {
        PropertyConfigurator.configure(path);
    }



}
