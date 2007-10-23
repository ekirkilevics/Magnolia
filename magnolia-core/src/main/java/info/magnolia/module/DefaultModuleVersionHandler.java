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
package info.magnolia.module;

import info.magnolia.module.delta.ModuleBootstrapTask;
import info.magnolia.module.delta.ModuleFilesExtraction;
import info.magnolia.module.delta.BootstrapEmptyRepositoriesTask;
import info.magnolia.module.delta.RegisterModuleServletsTask;

import java.util.ArrayList;
import java.util.List;

/**
 * @author philipp
 * @version $Id$
 *
 */
public class DefaultModuleVersionHandler extends AbstractModuleVersionHandler {

    /**
     * Returns the most common installation tasks.
     */
    protected List getBasicInstallTasks(InstallContext installContext) {
        final List basicInstallTasks = new ArrayList();
        basicInstallTasks.add(new BootstrapEmptyRepositoriesTask());
        basicInstallTasks.add(new ModuleBootstrapTask());
        basicInstallTasks.add(new ModuleFilesExtraction());
        basicInstallTasks.add(new RegisterModuleServletsTask());
        return basicInstallTasks;
    }

}
