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
package info.magnolia.module.ui;

import info.magnolia.module.ModuleManagementException;

import java.io.Writer;
import java.util.Map;

/**
 * User Interface for the install/update mechanism.
 * Instances are not to be reused, client code should re-instanciate for each request.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface ModuleManagerUI {

    void onStartup() throws ModuleManagementException;

    /**
     * @return a boolean indicating if the request should go through or pause until next user action.
     */
    boolean execute(Writer out, Map params) throws ModuleManagementException;

    void renderTempPage(Writer out) throws ModuleManagementException;
}
