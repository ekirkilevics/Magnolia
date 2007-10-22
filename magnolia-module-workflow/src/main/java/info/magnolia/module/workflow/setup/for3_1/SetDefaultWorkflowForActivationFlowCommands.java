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
package info.magnolia.module.workflow.setup.for3_1;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.module.workflow.commands.ActivationFlowCommand;

/**
 * @author philipp
 * @version $Id$
 *
 */
public class SetDefaultWorkflowForActivationFlowCommands extends AbstractRepositoryTask {

    public SetDefaultWorkflowForActivationFlowCommands() {
        super("Workflow activation", "Sets default values on workflow activation commands");
    }

    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        Collection nodes = QueryUtil.query(ContentRepository.CONFIG, "select * from nt:base where class='" + ActivationFlowCommand.class.getName() + "'");
        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            Content node = (Content) iter.next();
            if(!node.hasNodeData("workflowName")){
                node.createNodeData("workflowName").setValue(WorkflowConstants.DEFAULT_ACTIVATION_WORKFLOW);
            }
            if(!node.hasNodeData("dialogName")){
                node.createNodeData("dialogName").setValue(WorkflowConstants.DEFAULT_ACTIVATION_EDIT_DIALOG);
            }
        }
    }
}
