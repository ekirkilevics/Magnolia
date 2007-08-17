/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.module.admininterface.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.BasicDelta;
import info.magnolia.module.delta.BootstrapSingleResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class AdminModuleVersionHandler extends DefaultModuleVersionHandler {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(AdminModuleVersionHandler.class);

    public AdminModuleVersionHandler() {
        register("3.1", BasicDelta.createBasicDelta("Update to 3.1", "", new BootstrapSingleResource("New ACL configuration", "Bootstraps the new configuration for the ACL dialogs", "/mgnl-bootstrap/adminInterface/config.module.adminInterface.config.securityConfiguration.xml")));
    }
}
