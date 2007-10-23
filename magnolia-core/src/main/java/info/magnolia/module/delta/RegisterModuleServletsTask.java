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
package info.magnolia.module.delta;

import info.magnolia.cms.module.ServletDefinition;
import info.magnolia.module.InstallContext;
import info.magnolia.module.model.ModuleDefinition;

import java.util.Iterator;

/**
 * Reads the servlets to register from the module descriptor.
 * @author philipp
 * @version $Id$
 *
 */
public class RegisterModuleServletsTask extends ArrayDelegateTask {

    public RegisterModuleServletsTask() {
        super("Register module servlets");
    }
    
    public void execute(InstallContext installContext) throws TaskExecutionException {
        ModuleDefinition moduleDefinition = installContext.getCurrentModuleDefinition();
        
        // register servlets
        for (Iterator iter = moduleDefinition.getServlets().iterator(); iter.hasNext();) {
            ServletDefinition servletDefinition = (ServletDefinition) iter.next();
            addTask(new RegisterServletTask(servletDefinition));
        }
        
        super.execute(installContext);
    }
}
