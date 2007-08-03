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
package info.magnolia.module.workflow.setup.for3_1;

import info.magnolia.module.delta.BootstrapResourcesTask;
import info.magnolia.module.InstallContext;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BootstrapDefaultWorkflowDef extends BootstrapResourcesTask {

    public BootstrapDefaultWorkflowDef() {
        // TODO : this currently bootstraps many other resources !
        super("Default workflow definition", "Bootstraps the default workflow definition");
    }

    // TODO : check if nodes were already there, since we're bootstrapping stuff that was existing with EE !
    protected String[] getResourcesToBootstrap(InstallContext installContext) {
        return new String[]{
                "/mgnl-bootstrap/workflow/config.modules.workflow.config.flows.activation.xml",
                "/mgnl-bootstrap/workflow/config.modules.adminInterface.config.menu.config.workflows.xml",
                "/mgnl-bootstrap/workflow/config.modules.workflow.pages.flows.xml",
                // TODO : we might want to rename this class since this is just a new feature
                "/mgnl-bootstrap/workflow/config.modules.workflow.pages.inboxSubPages.xml",
                "/mgnl-bootstrap/workflow/config.modules.workflow.dialogs.editActivationWorkItem.xml"
        };
    }


}
