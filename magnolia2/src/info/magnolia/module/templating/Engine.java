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



package info.magnolia.module.templating;

import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.ModuleConfig;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.config.Paragraph;
import org.apache.log4j.Logger;


/**
 * Date: Jul 13, 2004
 * Time: 4:23:19 PM
 *
 * @author Sameer Charles
 * @version 2.0
 */



/**
 * Module "templating" main class
 *
 *
 * */

public class Engine implements Module {


    private static Logger log = Logger.getLogger(Engine.class);

    private static final String ATTRIBUTE_BASE_PATH="basePath";

    private String moduleName;
    private String basePath;


    public void init(ModuleConfig config) {
        this.moduleName = config.getModuleName();
        this.basePath = (String) config.getInitParameters().get(ATTRIBUTE_BASE_PATH);
        log.info("Module : "+this.moduleName);
        log.info("Module : updating Template list");
        Template.update(this.basePath);
        log.info("Module : updating Paragraph list");
        Paragraph.update(this.basePath);

        // set local store to be accessed via admin interface classes or JSP
        Store.getInstance().setStore(config.getLocalStore());

    }



    public void destroy() {
        // ignore
    }

    
}
