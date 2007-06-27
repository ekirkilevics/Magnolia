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
import info.magnolia.module.delta.Delta;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AddNewDefaultConfig implements Delta {
    public void apply(InstallContext ctx) throws RepositoryException {
        final Content configNode = ctx.getModuleConfigNode();

        final Content flowDefinitionManagerCfg = configNode.createContent("flowDefinitionManager");
        flowDefinitionManagerCfg.createNodeData("class").setValue("info.magnolia.module.workflow.flows.DefaultFlowDefinitionManager");

        // these two should not be necessary, associated code should be removed
        flowDefinitionManagerCfg.createNodeData("flowDefinitionURLPattern").setValue("auto");
        flowDefinitionManagerCfg.createNodeData("saveWorkflowDefinitionInWorkItem").setValue("true");

        // the following two properties should not be made public - untested code
        configNode.createNodeData("backupWorkItems").setValue(false);
        configNode.createNodeData("deferredExpressionStorage").setValue(false);
    }
}
