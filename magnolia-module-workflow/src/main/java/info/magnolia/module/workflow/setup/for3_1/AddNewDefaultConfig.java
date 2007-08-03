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

import info.magnolia.cms.core.Content;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AddNewDefaultConfig extends AbstractTask {
    //TODO why not extend PropertyValuesTask ??

    public AddNewDefaultConfig() {
        super("New config items", "Adding some new configuration items with their default values.");
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        try {
            final Content configNode = ctx.getOrCreateCurrentModuleConfigNode();

            final Content flowDefinitionManagerCfg = configNode.createContent("flowDefinitionManager");
            flowDefinitionManagerCfg.createNodeData("class").setValue("info.magnolia.module.workflow.flows.DefaultFlowDefinitionManager");

            // these two should not be necessary, associated code should be removed
            flowDefinitionManagerCfg.createNodeData("flowDefinitionURLPattern").setValue("auto");
            flowDefinitionManagerCfg.createNodeData("saveWorkflowDefinitionInWorkItem").setValue("true");

            // TODO the following two properties should not be made public - untested code
            configNode.createNodeData("backupWorkItems").setValue(false);
            configNode.createNodeData("deferredExpressionStorage").setValue(false);
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Could not add new default workflow configuration.", e);
        }
    }
}
