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
package info.magnolia.module.templating;

import info.magnolia.cms.module.ModuleConfig;
import info.magnolia.module.admininterface.AbstractModule;



/**
 * Module "templating" main class.
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version 2.0
 */
public class Engine extends AbstractModule {

    /**
     * @see info.magnolia.cms.module.Module#init(info.magnolia.cms.module.ModuleConfig)
     */
    public void onInit(ModuleConfig config) {

        // set local store to be accessed via admin interface classes or JSP
        Store.getInstance().setStore(config.getLocalStore());
    }
}