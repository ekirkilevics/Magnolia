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
package info.magnolia.module.workflow.setup;

import info.magnolia.module.AbstractModuleVersionHandler;
import info.magnolia.module.workflow.setup.for3_1.*;
import info.magnolia.module.model.Version;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WorkflowModuleVersionHandler extends AbstractModuleVersionHandler {

    protected WorkflowModuleVersionHandler(Version latestVersion) {
        super(latestVersion);

        final Version version3_1 = new Version(3, 1, 0);
        register(version3_1, new I18nMenuPoint());
        register(version3_1, new AddNewDefaultConfig());
        register(version3_1, new BootstrapDefaultWorkflowDef());
        register(version3_1, new RemoveMetadataFromExpressionsWorkspace());
        
    }
}
